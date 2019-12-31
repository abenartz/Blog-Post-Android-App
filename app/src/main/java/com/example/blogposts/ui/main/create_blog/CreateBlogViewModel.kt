package com.example.blogposts.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.blogposts.repository.main.CreateBlogRepository
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.BaseViewModel
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.Loading
import com.example.blogposts.ui.main.create_blog.state.CreateBlogStateEvent
import com.example.blogposts.ui.main.create_blog.state.CreateBlogStateEvent.*
import com.example.blogposts.ui.main.create_blog.state.CreateBlogViewState
import com.example.blogposts.ui.main.create_blog.state.CreateBlogViewState.*
import com.example.blogposts.util.AbsentLiveData
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val createBlogRepository: CreateBlogRepository
): BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {


    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    override fun handleStateEvent(stateEvent: CreateBlogStateEvent): LiveData<DataState<CreateBlogViewState>> {

        when(stateEvent) {

            is CreateNewBlogEvent -> {
                val title = RequestBody.create(
                    MediaType.parse("text/plain"),
                    stateEvent.title
                )

                val body = RequestBody.create(
                    MediaType.parse("text/plain"),
                    stateEvent.body
                )


                return sessionManager.cachedToken.value?.let {  authToken ->
                    createBlogRepository.createNewBlogPost(
                        authToken = authToken,
                        title = title,
                        body = body,
                        image = stateEvent.image
                    )
                }?: AbsentLiveData.create()
            }

            is None -> {
                return liveData {
                    emit(
                        DataState(
                            null,
                            Loading(false),
                            null
                        )
                    )
                }
            }
        }
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let { newBlogFields.newBlogTitle = it }
        body?.let { newBlogFields.newBlogBody = it }
        uri?.let { newBlogFields.newBlogUri = it }
        update.blogFields = newBlogFields
        setViewState(update)
    }

    fun getNewImageUri(): Uri? {
        getCurrentViewStateOrNew().let {
            it.blogFields.let { newBlogFields ->
                return newBlogFields.newBlogUri
            }
        }
    }

    fun clearNewBlogFields() {
        val update = getCurrentViewStateOrNew()
        update.blogFields = NewBlogFields()
        setViewState(update)
    }

    fun cancelActiveJobs() {
        createBlogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}