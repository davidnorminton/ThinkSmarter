package com.example.thinksmarter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinksmarter.data.auth.AuthState
import com.example.thinksmarter.data.auth.UserAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.activity.result.ActivityResultLauncher
import android.content.Intent

data class UserProfileUiState(
    val authState: AuthState = AuthState.NotAuthenticated,
    val error: String? = null
)

sealed class UserProfileUiEvent {
    object SignInWithGoogle : UserProfileUiEvent()
    object SignOut : UserProfileUiEvent()
    object ClearError : UserProfileUiEvent()
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userAuthManager: UserAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userAuthManager.authState.collect { authState ->
                _uiState.value = _uiState.value.copy(authState = authState)
            }
        }
    }

    fun handleEvent(event: UserProfileUiEvent) {
        when (event) {
            is UserProfileUiEvent.SignInWithGoogle -> {
                // This is handled by the Activity
            }
            is UserProfileUiEvent.SignOut -> {
                userAuthManager.signOut()
            }
            is UserProfileUiEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    fun signInWithGoogle(launcher: ActivityResultLauncher<Intent>) {
        userAuthManager.signInWithGoogle(launcher)
    }
}