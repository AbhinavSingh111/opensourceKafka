package org.rekhta.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.kafka.analytics.Analytics
import org.kafka.auth.R
import org.kafka.base.domain.InvokeSuccess
import org.kafka.base.extensions.stateInDefault
import org.kafka.common.ObservableLoadingCounter
import org.kafka.common.collectStatus
import org.kafka.common.snackbar.SnackbarManager
import org.kafka.common.snackbar.UiMessage
import org.kafka.domain.interactors.LoginUser
import org.kafka.domain.interactors.LogoutUser
import org.kafka.domain.interactors.RegisterUser
import org.kafka.domain.interactors.ResetPassword
import org.kafka.domain.observers.ObserveUser
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUser: LoginUser,
    private val registerUser: RegisterUser,
    private val resetPassword: ResetPassword,
    private val snackbarManager: SnackbarManager,
    private val logoutUser: LogoutUser,
    private val analytics: Analytics,
    observeUser: ObserveUser
) : ViewModel() {
    private val loadingCounter = ObservableLoadingCounter()

    val state: StateFlow<AuthViewState> = combine(
        observeUser.flow,
        loadingCounter.observable,
    ) { user, isLoading ->
        AuthViewState(currentUser = user, isLoading = isLoading)
    }.stateInDefault(
        scope = viewModelScope,
        initialValue = AuthViewState(),
    )

    init {
        observeUser(Unit)
    }

    fun login(email: String, password: String) {
        when {
            !email.isValidEmail() ->
                snackbarManager.addMessage(UiMessage(R.string.invalid_email_message))

            !password.isValidPassword() ->
                snackbarManager.addMessage(UiMessage(R.string.invalid_password_message))

            else -> {
                viewModelScope.launch {
                    loginUser(LoginUser.Params(email, password))
                        .collectStatus(loadingCounter, snackbarManager)
                }
            }
        }
    }

    fun signup(email: String, password: String, name: String) {
        viewModelScope.launch {
            when {
                !email.isValidEmail() ->
                    snackbarManager.addMessage(UiMessage(R.string.invalid_email_message))

                !password.isValidPassword() ->
                    snackbarManager.addMessage(UiMessage(R.string.invalid_password_message))

                else -> {
                    registerUser(RegisterUser.Params(email, password, name))
                        .collectStatus(loadingCounter, snackbarManager)
                }
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isValidEmail()) {
            viewModelScope.launch {
                resetPassword(ResetPassword.Params(email))
                    .collectStatus(loadingCounter, snackbarManager) { status ->
                        if (status == InvokeSuccess) {
                            snackbarManager.addMessage(UiMessage(R.string.password_reset_link_sent))
                        }
                    }
            }
        } else {
            viewModelScope.launch {
                snackbarManager.addMessage(UiMessage(R.string.invalid_email_message))
            }
        }
    }

    fun loginByGmail() {

    }

    fun logout() {
        viewModelScope.launch {
            analytics.log { this.logoutClicked() }
            logoutUser(Unit).collect { status ->
                if (status == InvokeSuccess) {
                    snackbarManager.addMessage(UiMessage(R.string.logged_out))
                }
            }
        }
    }

    private fun CharSequence.isValidEmail() =
        isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun CharSequence.isValidPassword() = length > 4

}
