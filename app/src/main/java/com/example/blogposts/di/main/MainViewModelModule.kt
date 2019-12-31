package com.example.blogposts.di.main


import androidx.lifecycle.ViewModel
import com.example.blogposts.di.ViewModelKey
import com.example.blogposts.ui.main.account.AccountViewModel
import com.example.blogposts.ui.main.blog.viewmodel.BlogViewModel
import com.example.blogposts.ui.main.create_blog.CreateBlogViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel::class)
    abstract fun bindBlogViewModel(blogViewModel: BlogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateBlogViewModel::class)
    abstract fun bindCreatBlogViewModel(createBlogViewModel: CreateBlogViewModel): ViewModel


}