package com.complexsoft.ketnote.ui.screen.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.repository.MongoDBAPP
import com.complexsoft.ketnote.databinding.LoginScreenLayoutBinding
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.AuthenticationChange
import io.realm.kotlin.mongodb.LoggedIn
import io.realm.kotlin.mongodb.LoggedOut
import io.realm.kotlin.mongodb.Removed
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginScreen : Fragment(R.layout.login_screen_layout) {

    private lateinit var binding: LoginScreenLayoutBinding
    val viewModel by activityViewModels<LoginViewModel>()
    private lateinit var activityForResult: ActivityResultLauncher<IntentSenderRequest>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityForResult = viewModel.getActivityForResult(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LoginScreenLayoutBinding.inflate(layoutInflater)
        context?.let { Glide.with(it).load(R.drawable.google).into(binding.loginLogo) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (MongoDBAPP.app.currentUser?.loggedIn == true) {
                    findNavController().navigate(R.id.action_loginScreen_to_homeScreen)
                } else {
                    MongoDBAPP.app.authenticationChangeAsFlow()
                        .collect { change: AuthenticationChange ->
                            when (change) {
                                is LoggedIn -> findNavController().navigate(R.id.action_loginScreen_to_homeScreen)
                                is LoggedOut -> {
                                }

                                is Removed -> {}
                            }
                        }
                }
            }
        }

        binding.loginButton.setOnClickListener {
            viewModel.startLoggingWithGoogle(this, activityForResult)
        }
        return binding.root
    }
}