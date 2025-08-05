package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.auth.AuthState
import com.example.thinksmarter.data.auth.UserAuthManager
import com.example.thinksmarter.data.auth.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val authState: AuthState = AuthState.NotAuthenticated,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class UserProfileUiEvent {
    object SignInWithGoogle : UserProfileUiEvent()
    object SignOut : UserProfileUiEvent()
    object ClearError : UserProfileUiEvent()
}

class UserProfileViewModel(
    private val userAuthManager: UserAuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            userAuthManager.authState.collect { authState ->
                _uiState.value = _uiState.value.copy(
                    authState = authState,
                    isLoading = authState is AuthState.Loading,
                    error = if (authState is AuthState.Error) authState.message else null
                )
            }
        }
    }
    
    fun handleEvent(event: UserProfileUiEvent) {
        when (event) {
            is UserProfileUiEvent.SignInWithGoogle -> {
                // Set loading state to trigger sign-in
                _uiState.value = _uiState.value.copy(authState = AuthState.Loading)
            }
            is UserProfileUiEvent.SignOut -> {
                userAuthManager.signOut()
            }
            is UserProfileUiEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }
    
    fun signInWithGoogle(launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>) {
        userAuthManager.signInWithGoogle(launcher)
    }
    
    fun handleSignInResult(data: android.content.Intent?) {
        userAuthManager.handleSignInResult(data)
    }
} 