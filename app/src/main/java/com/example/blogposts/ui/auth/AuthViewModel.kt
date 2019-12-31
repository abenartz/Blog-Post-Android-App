package com.example.blogposts.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.blogposts.models.AuthToken
import com.example.blogposts.repository.auth.AuthRepository
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.Loading
import com.example.blogposts.ui.auth.state.AuthStateEvent
import com.example.blogposts.ui.auth.state.AuthStateEvent.*
import com.example.blogposts.ui.auth.state.AuthViewState
import com.example.blogposts.ui.auth.state.LoginFields
import com.example.blogposts.ui.auth.state.RegistrationFields
import com.example.blogposts.ui.main.account.state.AccountViewState
import com.example.blogposts.util.AbsentLiveData
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository

): BaseViewModel<AuthStateEvent, AuthViewState>() {


    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        when(stateEvent) {
            is LoginAttemptEvent -> {
                return authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password)
            }
            is RegistrationAttemptEvent -> {
                Log.d(TAG, "handleStateEvent: RegistrationAttemptEvent")
                return authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirm_password
                )
            }
            is CheckPreviousAuthEvent -> {
                return authRepository.checkPreviousAuthUser()
            }
            is None -> {
                return object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            error = null,
                            loading = Loading(false),
                            data = null
                        )
                    }
                }
            }
        }
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationField == registrationFields) {
            return
        }
        update.registrationField = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    fun setAuthToken (authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        setViewState(update)
    }

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    fun cancelActiveJobs() {
        handlePendingData()
        authRepository.cancelActiveJobs()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}