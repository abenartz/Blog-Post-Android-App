package com.example.blogposts.ui.main.account.state

sealed class AccountStateEvent {

    object GetAccountPropertiesEvent : AccountStateEvent() // if empty should be an object and not a class

    data class UpdateAccountPropertiesEvent(
        val email:String,
        val username: String
    ): AccountStateEvent()

    data class ChangePasswordEvent(
        val currentPassword: String,
        val newPassword: String,
        val confirmNewPassword: String
    ): AccountStateEvent()

    object None : AccountStateEvent()
}