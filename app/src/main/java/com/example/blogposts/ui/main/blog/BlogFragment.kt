package com.example.blogposts.ui.main.blog


import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.example.blogposts.R
import com.example.blogposts.models.BlogPost
import com.example.blogposts.persistence.BlogQueryUtils.Companion.BLOG_FILTER_DATE_UPDATED
import com.example.blogposts.persistence.BlogQueryUtils.Companion.BLOG_FILTER_USERNAME
import com.example.blogposts.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.example.blogposts.persistence.BlogQueryUtils.Companion.BLOG_ORDER_DESC
import com.example.blogposts.ui.DataState
import com.example.blogposts.ui.main.blog.state.BlogViewState
import com.example.blogposts.ui.main.blog.viewmodel.*
import com.example.blogposts.util.ErrorHandling
import com.example.blogposts.util.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_blog.*

class BlogFragment : BaseBlogFragment(),
    BlogListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener
{

    private lateinit var recyclerAdapter: BlogListAdapter
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        swipe_refresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()
        Log.d(TAG, "BlogFragment: onViewCreated: savedInstanceState = ${savedInstanceState}")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "BlogFragment: onResume: refreshFromCache()")
        viewModel.refreshFromCache()
    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    private fun saveLayoutManagerState() {
        blog_post_recyclerview.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }

    private fun onBlogSearchOrFilter() {
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        blog_post_recyclerview.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        focusable_view.requestFocus()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            if (dataState != null) {
                handlePagination(dataState)
                stateChangeListener.onDataStateChange(dataState)
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            Log.d(TAG, "BlogFragment, viewState: ${viewState}  ")
            if (viewState != null) {
                recyclerAdapter.apply {
                    preloadGlideImages(
                        dependencyProvider.getGlideRequestManager(),
                        viewState.blogFields.blogList
                    )

                    Log.d(TAG, "# List items: ${viewState.blogFields.blogList.size} ")
                    submitList(
                        list = viewState.blogFields.blogList,
                        isQueryExhausted = viewState.blogFields.isQueryExhausted
                    )
                }
            }
        })
    }

    private fun handlePagination(dataState: DataState<BlogViewState>) {
        // handle incoming data from dataState
        dataState.data?.let {
            it.data?.let {
                it.getContentIfNotHandled()?.let { blogViewState ->
                    viewModel.handleIncomingBlogListData(blogViewState)
                }
            }
        }

        // check for pagination end
        // must do this b/c server will return ApiErrorResponse if page is not valid
        // -> meaning there is no more data!
        dataState.error?.let { event ->
            event.peekContent().response.message?.let {
                if (ErrorHandling.isPaginationDone(it)) {

                    // handle the error message event so it doesn't play on UI
                    event.getContentIfNotHandled()

                    // set query exhausted to update RecyclerView with
                    // "No more results..." list item
                    viewModel.setQueryExhausted(true)
                }
            }
        }
    }

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE // to get width as much as we can
            searchView.setIconifiedByDefault(true) // from search icon to 'x' icon
            searchView.isSubmitButtonEnabled = true // adding submit icon button to the side (arrow)
        }

        // case 1: Enter on computer keyboard or arrow on virtual keyboard
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchQuery = textView.text.toString()
                Log.e(TAG, "SearchView: (keyboard or arrow) executing search... $searchQuery")
                viewModel.setQuery(searchQuery).let {
                    onBlogSearchOrFilter()
                }
            }
            true
        }

        // case 2: Search button clicked (in toolbar)
        (searchView.findViewById(R.id.search_go_btn) as View).setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            Log.e(TAG, "SearchView: (Button) executing search... $searchQuery")
            viewModel.setQuery(searchQuery).let {
                onBlogSearchOrFilter()
            }
        }

    }

    private fun initRecyclerView() {
        blog_post_recyclerview.apply {
            layoutManager = LinearLayoutManager(this@BlogFragment.context)

            val topSpacingItemDecoration = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingItemDecoration) // in case we initiate twice the recycler view or something
            addItemDecoration(topSpacingItemDecoration)

            recyclerAdapter = BlogListAdapter(
                interaction = this@BlogFragment,
                requestManager = dependencyProvider.getGlideRequestManager()
            )
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Log.d(TAG, "BlogFragment: Attempting to load next page...")
                        viewModel.nextPage()
                    }
                }
            })

            adapter = recyclerAdapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_filter_settings -> {
                showFilterOptions()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(position: Int, item: BlogPost) {
        viewModel.setBlogPost(item)
        findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)
    }

    override fun restoreListPosition() {
        viewModel.viewState.value?.blogFields?.layoutManagerState?.let { lmState ->
            blog_post_recyclerview?.layoutManager?.onRestoreInstanceState(lmState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear references (can leak memory)
        blog_post_recyclerview.adapter = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onRefresh() {
        onBlogSearchOrFilter()
        swipe_refresh.isRefreshing = false
    }

    private fun showFilterOptions() {
        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_blog_filter)

            val view = dialog.getCustomView()

            // 1: highlight the previous filter options
            highlightPrevFilterOptions(view)

            // 2: listen for new applied filters
            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: applying filter.")
                applyFilterClicked(view, dialog)
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "Filtering dialog: canceling Filter")
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun applyFilterClicked(
        view: View,
        dialog: MaterialDialog
    ) {
        val selectedFilter = view.findViewById<RadioButton>(
            view.findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
        )
        val selectedOrder = view.findViewById<RadioButton>(
            view.findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
        )

        var filter = BLOG_FILTER_DATE_UPDATED
        if (selectedFilter.text.toString().equals(getString(R.string.filter_author))) {
            filter = BLOG_FILTER_USERNAME
        }

        var order = BLOG_ORDER_DESC
        if (selectedOrder.text.toString().equals(getString(R.string.order_asc))) {
            order = BLOG_ORDER_ASC
        }

        // 3: set the filter and order in the viewModel
        // 4: save to sharedPreferences
        viewModel.saveFilterOptions(filter, order).let {
            viewModel.setBlogFilter(filter)
            viewModel.setBlogOrder(order)
            onBlogSearchOrFilter()
        }
        dialog.dismiss()
    }

    private fun highlightPrevFilterOptions(view: View) {
        val filter = viewModel.getFilter()
        if (filter.equals(BLOG_FILTER_DATE_UPDATED)) {
            view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_date)
        } else {
            view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_author)
        }

        val order = viewModel.getOrder()
        if (order.equals(BLOG_ORDER_ASC)) {
            view.findViewById<RadioGroup>(R.id.order_group).check(R.id.order_asc)
        } else {
            view.findViewById<RadioGroup>(R.id.order_group).check(R.id.order_desc)
        }
    }
}