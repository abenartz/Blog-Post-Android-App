package com.example.blogposts.ui.main.account.state

import android.os.Parcelable
import com.example.blogposts.models.AccountProperties
import kotlinx.android.parcel.Parcelize

const val ACCOUNT_VIEW_STATE_BUNDLE_KEY = "com.example.blogposts.ui.main.blog.state.AccountViewState"

@Parcelize
class AccountViewState (
    var accountProperties: AccountProperties? = null
) : Parcelable