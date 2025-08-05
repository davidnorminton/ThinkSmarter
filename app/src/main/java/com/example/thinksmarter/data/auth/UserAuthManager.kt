package com.example.thinksmarter.data.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?
)

sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}

class UserAuthManager(private val context: Context) {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private lateinit var googleSignInClient: GoogleSignInClient
    
    init {
        setupGoogleSignIn()
        checkCurrentUser()
    }
    
    private fun setupGoogleSignIn() {
        println("DEBUG: Setting up Google Sign-In")
        val clientId = "1023192935469-8ghnvlgrue8a6j5nbp83e1ave52v53he.apps.googleusercontent.com"
        println("DEBUG: Using client ID: $clientId")
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(clientId)
            .build()
        
        println("DEBUG: GoogleSignInOptions built successfully")
        googleSignInClient = GoogleSignIn.getClient(context, gso)
        println("DEBUG: Google Sign-In client created")
        
        // Check if Google Play Services is available
        val googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        println("DEBUG: Google Play Services availability: $resultCode")
    }
    
    private fun checkCurrentUser() {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            _authState.value = AuthState.Authenticated(account.toUserProfile())
        } else {
            _authState.value = AuthState.NotAuthenticated
        }
    }
    
    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        println("DEBUG: signInWithGoogle called")
        _authState.value = AuthState.Loading
        
        // Check if user is already signed in
        val currentUser = GoogleSignIn.getLastSignedInAccount(context)
        if (currentUser != null) {
            println("DEBUG: User already signed in: ${currentUser.email}")
            _authState.value = AuthState.Authenticated(currentUser.toUserProfile())
            return
        }
        
        val signInIntent = googleSignInClient.signInIntent
        println("DEBUG: Launching Google Sign-In intent")
        launcher.launch(signInIntent)
    }
    
    fun handleSignInResult(data: Intent?) {
        println("DEBUG: handleSignInResult called with data: $data")
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            println("DEBUG: GoogleSignIn task created")
            val account = task.getResult(ApiException::class.java)
            println("DEBUG: GoogleSignIn successful, account: ${account.email}")
            _authState.value = AuthState.Authenticated(account.toUserProfile())
        } catch (e: ApiException) {
            println("DEBUG: GoogleSignIn failed with ApiException: ${e.statusCode} - ${e.message}")
            val errorMessage = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign in was cancelled"
                GoogleSignInStatusCodes.NETWORK_ERROR -> "Network error occurred"
                GoogleSignInStatusCodes.INVALID_ACCOUNT -> "Invalid account"
                GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> "Sign in required"
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Sign in failed"
                GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Sign in already in progress"
                else -> "Sign in failed with error code: ${e.statusCode}"
            }
            _authState.value = AuthState.Error(errorMessage)
        } catch (e: Exception) {
            println("DEBUG: GoogleSignIn failed with general exception: ${e.message}")
            _authState.value = AuthState.Error("Sign in failed: ${e.message}")
        }
    }
    
    fun signOut() {
        println("DEBUG: Signing out user")
        googleSignInClient.signOut().addOnCompleteListener {
            println("DEBUG: Sign out completed")
            _authState.value = AuthState.NotAuthenticated
        }
    }
    
    fun revokeAccess() {
        println("DEBUG: Revoking access")
        googleSignInClient.revokeAccess().addOnCompleteListener {
            println("DEBUG: Access revoked")
            _authState.value = AuthState.NotAuthenticated
        }
    }
    
    private fun GoogleSignInAccount.toUserProfile(): UserProfile {
        return UserProfile(
            id = id ?: "",
            name = displayName ?: "",
            email = email ?: "",
            photoUrl = photoUrl?.toString()
        )
    }
} 