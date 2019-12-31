package com.example.blogposts.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.example.blogposts.api.GenericResponse
import com.example.blogposts.api.main.OpenApiMainService
import com.example.blogposts.models.AccountProperties
import com.example.blogposts.models.AuthToken
import com.example.blogposts.persistence.AccountPropertiesDao
import com.example.blogposts.repository.JobManager
import com.example.blogposts.repository.NetworkBoundResource
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.Response
import com.example.blogposts.ui.ResponseType
import com.example.blogposts.ui.main.account.state.AccountViewState
import com.example.blogposts.util.AbsentLiveData
import com.example.blogposts.util.ApiSuccessResponse
import com.example.blogposts.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepository
    @Inject
    constructor(
        val openApiMainService: OpenApiMainService,
        val accountPropertiesDao: AccountPropertiesDao,
        val sessionManager: SessionManager
    ): JobManager("AccountRepository")
{

    private val TAG: String = "AppDebug"

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {

                updateLocalDb(response.body)

               createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return openApiMainService.getAccountProperties("Token ${authToken.token!!}")
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                        object: LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }
            }

            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {

                    // Finish by viewing the db cache
                    result.addSource(loadFromCache()) { accountViewState ->
                        onCompleteJob(
                            DataState.data(
                                data = accountViewState,
                                response = null
                            )
                        )
                    }
                }
            }

            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)
            }

        }.asLiveData()
    }

    fun saveAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any , AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null)

                withContext(Main) {

                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.saveAccountProperties(
                    authorization = "Token ${authToken.token!!}",
                    email = accountProperties.email,
                    username = accountProperties.username
                )
            }

            // not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }

        }.asLiveData()
    }

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            // not applicable
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.updatePassword(
                        "Token ${authToken.token!!}",
                        currentPassword,
                        newPassword,
                        confirmNewPassword
                    )
            }

            // not applicable in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            // not applicable in this case
            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)
            }

        }.asLiveData()
    }

}