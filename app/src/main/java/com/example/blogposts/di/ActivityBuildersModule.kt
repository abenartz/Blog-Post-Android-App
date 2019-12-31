package com.example.blogposts.di


import com.example.blogposts.di.auth.AuthFragmentBuildersModule
import com.example.blogposts.di.auth.AuthModule
import com.example.blogposts.di.auth.AuthScope
import com.example.blogposts.di.auth.AuthViewModelModule
import com.example.blogposts.di.main.MainFragmentBuildersModule
import com.example.blogposts.di.main.MainModule
import com.example.blogposts.di.main.MainScope
import com.example.blogposts.di.main.MainViewModelModule
import com.example.blogposts.ui.auth.AuthActivity
import com.example.blogposts.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {


    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity


    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity

}