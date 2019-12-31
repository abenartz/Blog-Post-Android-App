package com.example.blogposts.ui.auth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import com.example.blogposts.R
import com.example.blogposts.ui.auth.state.AuthStateEvent
import com.example.blogposts.ui.auth.state.AuthStateEvent.*
import com.example.blogposts.ui.auth.state.RegistrationFields
import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()

        register_button.setOnClickListener {
            register()
        }
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.registrationField?.let {
                it.registration_email?.let { input_email.setText(it) }
                it.registration_username?.let { input_username.setText(it) }
                it.registration_password?.let { input_password.setText(it) }
                it.registration_confirm_password?.let { input_password_confirm.setText(it) }
            }
        })
    }

    fun register() {
        viewModel.setStateEvent(
            RegistrationAttemptEvent(
              input_email.text.toString(),
              input_username.text.toString(),
              input_password.text.toString(),
              input_password_confirm.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setRegistrationFields(
            RegistrationFields(
                input_email.text.toString(),
                input_username.text.toString(),
                input_password.text.toString(),
                input_password_confirm.text.toString()
            )
        )
    }


}
