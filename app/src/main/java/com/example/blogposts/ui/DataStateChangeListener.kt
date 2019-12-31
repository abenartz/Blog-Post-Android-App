package com.example.blogposts.ui

interface DataStateChangeListener {

    fun onDataStateChange(dataState: DataState<*>?)

    fun expandAppbar()

    fun hideSoftKeyboard()

    fun isStoragePermissionGranted(): Boolean
}