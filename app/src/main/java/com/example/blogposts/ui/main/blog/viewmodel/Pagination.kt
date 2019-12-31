package com.example.blogposts.ui.main.blog.viewmodel

import android.util.Log
import com.example.blogposts.ui.main.blog.state.BlogStateEvent.*
import com.example.blogposts.ui.main.blog.state.BlogViewState

fun BlogViewModel.refreshFromCache() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    setStateEvent(RestoreBlogListFromCache())
}

fun BlogViewModel.loadFirstPage() {
    setQueryInProgress(true)
    setQueryExhausted(false)
    resetPage()
    setStateEvent(BlogSearchEvent())
}

fun BlogViewModel.nextPage() {
    Log.d(TAG, "BlogViewModel: nextPage: getIsQueryExhausted = ${getIsQueryExhausted()}; getIsQueryInProgress = ${getIsQueryInProgress()}")
    if (!getIsQueryExhausted() && !getIsQueryInProgress()) {
        Log.d(TAG, "BlogViewModel: nextPage: Attempting tp load next page...")
        incrementPageNumber()
        setQueryInProgress(true)
        setStateEvent(BlogSearchEvent())
    }
}


fun BlogViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

fun BlogViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    /**
     * according to Mitch the reason to make this copy is for not referencing the page that actually stored
     * in the viewState. We don't want that this new page will be the same location in memory as the page we are about to increment.
     * By that, we wont disturbed the memory location of the view state
     */
    val page = update.copy().blogFields.page
    update.blogFields.page = page + 1
    setViewState(update)
}

fun BlogViewModel.handleIncomingBlogListData(viewState: BlogViewState) {
    setQueryExhausted(viewState.blogFields.isQueryExhausted)
    setQueryInProgress(viewState.blogFields.isQueryInProgress)
    setBlogListData(viewState.blogFields.blogList)
//    viewState.blogFields.blogList.let { blogList ->
//        if (blogList.isEmpty()) {
//            loadFirstPage()
//        } else {
//        }
//    }

}