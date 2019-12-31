package com.example.blogposts.di.main

import com.example.blogposts.api.main.OpenApiMainService
import com.example.blogposts.persistence.AccountPropertiesDao
import com.example.blogposts.persistence.AppDatabase
import com.example.blogposts.persistence.BlogPostDao
import com.example.blogposts.repository.main.AccountRepository
import com.example.blogposts.repository.main.BlogRepository
import com.example.blogposts.repository.main.CreateBlogRepository
import com.example.blogposts.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit


@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder
            .build()
            .create(OpenApiMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
        openApiMainService: OpenApiMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(openApiMainService, accountPropertiesDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository{
        return BlogRepository(openApiMainService, blogPostDao, sessionManager)
    }

    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository{
        return CreateBlogRepository(openApiMainService, blogPostDao, sessionManager)
    }


}

