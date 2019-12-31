package com.example.blogposts.di.auth


import androidx.lifecycle.ViewModel
import com.example.blogposts.di.ViewModelKey
import com.example.blogposts.ui.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

}