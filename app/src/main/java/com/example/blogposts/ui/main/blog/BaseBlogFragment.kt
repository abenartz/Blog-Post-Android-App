package com.example.blogposts.ui.main.blog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.blogposts.R
import com.example.blogposts.di.Injectable
import com.example.blogposts.ui.DataStateChangeListener
import com.example.blogposts.ui.UICommunicationListener
import com.example.blogposts.ui.main.MainDependencyProvider
import com.example.blogposts.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.example.blogposts.ui.main.blog.state.BlogViewState
import com.example.blogposts.ui.main.blog.viewmodel.BlogViewModel
import java.lang.Exception

abstract class BaseBlogFragment : Fragment(), Injectable {

    val TAG: String = "AppDebug"

    lateinit var viewModel: BlogViewModel

    lateinit var dependencyProvider: MainDependencyProvider

    lateinit var stateChangeListener: DataStateChangeListener

    lateinit var uiCommunicationListener: UICommunicationListener

    /**
     * Process death:
     *
     * we use onCreate to restore the savedInstanceState to the viewModel:
     * - we need to initialized the data before onViewCreated so by that time the data will be ready to shown
     * - we use MainDependencyProvider interface b/c the dependency injection of ViewModelProvider and SessionManager
     *   wouldn't work yet when we call onCreate.
     *   that why we inject it at the MainActivity and pass that interface
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = activity?.run {
            ViewModelProvider(this, dependencyProvider.getViewModelProviderFactory())
                .get(BlogViewModel::class.java)
        }?: throw Exception("Invalid Activity")

        cancelActiveJobs()

        // restore state after process death
        savedInstanceState?.let { inState ->
            (inState[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState?)?.let { blogViewState ->
                viewModel.setViewState(blogViewState)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupActionbarWithNavController(R.id.blogFragment, activity as AppCompatActivity)
    }

    private fun isViewModelInitialized() = ::viewModel.isInitialized

    override fun onSaveInstanceState(outState: Bundle) {
        if (isViewModelInitialized()) {

            /**
             * Do not save large data at saveInstanceState bundle - aka lists and so..
             * we put empty list at the viewState and at the BlogFragment onResume() method we
             * will make cache request to restore the list
             */
            val viewState = viewModel.viewState.value
            viewState?.blogFields?.blogList = ArrayList()

            outState.putParcelable(
                BLOG_VIEW_STATE_BUNDLE_KEY,
                viewState
            )
        }
        super.onSaveInstanceState(outState)
    }

    fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    private fun setupActionbarWithNavController(fragmentId: Int, activity: AppCompatActivity) {
        // exclude the back arrow only from this fragment id
        // asi: unnecessary - work the same without passing AppBarConfiguration
        val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
        NavigationUI.setupActionBarWithNavController(
            activity,
            findNavController(),
            appBarConfiguration
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try{
            stateChangeListener = context as DataStateChangeListener
        }catch(e: ClassCastException){
            Log.e(TAG, "$context must implement DataStateChangeListener" )
        }

        try {
            uiCommunicationListener = context as UICommunicationListener
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener" )
        }

        try {
            dependencyProvider = context as MainDependencyProvider
        } catch (e: ClassCastException) {
            Log.e(TAG, "$context must implement UICommunicationListener" )
        }
    }
}