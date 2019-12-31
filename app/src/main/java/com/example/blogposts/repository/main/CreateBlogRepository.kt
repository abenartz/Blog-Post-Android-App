package com.example.blogposts.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.blogposts.api.main.OpenApiMainService
import com.example.blogposts.api.main.responses.BlogCreateUpdateResponse
import com.example.blogposts.models.AuthToken
import com.example.blogposts.models.BlogPost
import com.example.blogposts.persistence.BlogPostDao
import com.example.blogposts.repository.JobManager
import com.example.blogposts.repository.NetworkBoundResource
import com.example.blogposts.session.SessionManager
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.Response
import com.example.blogposts.ui.ResponseType
import com.example.blogposts.ui.main.blog.state.BlogViewState
import com.example.blogposts.ui.main.create_blog.state.CreateBlogViewState
import com.example.blogposts.util.AbsentLiveData
import com.example.blogposts.util.ApiSuccessResponse
import com.example.blogposts.util.DateUtils
import com.example.blogposts.util.GenericApiResponse
import com.example.blogposts.util.SuccessHandling.Companion.RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogRepository
@Inject
constructor(
    private val openApiMainService: OpenApiMainService,
    private val blogPostDao: BlogPostDao,
    private val sessionManager: SessionManager
): JobManager("CreateBlogRepository")
{

    private val TAG: String = "AppDebug"


    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part
    ): LiveData<DataState<CreateBlogViewState>> {
        return object : NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            // Not applicable
            override suspend fun createCacheRequestAndReturn() {
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {

                // if they don't have a paid membership account it will still return 200
                // need an account for that
                if (!response.body.response.equals(RESPONSE_MUST_BECOME_CODINGWITHMITCH_MEMBER)) {
                    val updateBlogPost = BlogPost(
                        response.body.pk,
                        response.body.title,
                        response.body.slug,
                        response.body.body,
                        response.body.image,
                        DateUtils.convertServerStringDateToLong(response.body.date_updated),
                        response.body.username
                    )

                    updateLocalDb(updateBlogPost)
                }
                withContext(Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Dialog())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return openApiMainService.createBlog(
                    authorization = "Token ${authToken.token}",
                    title = title,
                    body = body,
                    image = image
                )
            }

            // Not applicable
            override fun loadFromCache(): LiveData<CreateBlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: BlogPost?) {
                cacheObject?.let {
                    blogPostDao.insert(it)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewBlogPost", job)
            }


        }.asLiveData()

    }

}