package com.example.blogposts.ui.main.account

import androidx.lifecycle.LiveData
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.repository.main.AccountRepository
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.Loading
import com.example.blogposts.ui.auth.state.AuthStateEvent
import com.example.blogposts.ui.main.account.state.AccountStateEvent
import com.example.blogposts.ui.main.account.state.AccountStateEvent.*
import com.example.blogposts.ui.main.account.state.AccountViewState
import com.example.blogposts.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
): BaseViewModel<AccountStateEvent, AccountViewState>()
{
    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        return when (stateEvent) {
            is GetAccountPropertiesEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                accountRepository.getAccountProperties(authToken)
                }?: AbsentLiveData.create()
            }
            is UpdateAccountPropertiesEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { account_pk ->
                        accountRepository.saveAccountProperties(
                            authToken,
                            AccountProperties(
                                account_pk,
                                stateEvent.email,
                                stateEvent.username
                            )
                        )
                    }
                }?: AbsentLiveData.create()
            }
            is ChangePasswordEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.updatePassword(
                        authToken,
                        stateEvent.currentPassword,
                        stateEvent.newPassword,
                        stateEvent.confirmNewPassword
                    )
                }?: AbsentLiveData.create()
            }
            is None -> {
                object : LiveData<DataState<AccountViewState>>() {
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

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties != accountProperties) {
            update.accountProperties = accountProperties
            _viewState.value = update
        }
    }

    fun logout() = sessionManager.logout()

    fun cancelActiveJobs() {
        handlePendingData()
        accountRepository.cancelActiveJobs()
    }

    private fun handlePendingData() {
        setStateEvent(None)
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}