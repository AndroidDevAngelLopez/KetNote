package com.complexsoft.ketnote.ui.screen.login

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.data.repository.MongoDBAPP
import com.complexsoft.ketnote.databinding.LoginScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.components.switchConnectivityObserverLayoutColor
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.AuthenticationChange
import io.realm.kotlin.mongodb.LoggedIn
import io.realm.kotlin.mongodb.LoggedOut
import io.realm.kotlin.mongodb.Removed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginScreen : Fragment(R.layout.login_screen_layout) {

    private lateinit var binding: LoginScreenLayoutBinding
    private val viewModel by activityViewModels<LoginViewModel>()
    private lateinit var activityForResult: ActivityResultLauncher<IntentSenderRequest>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LoginScreenLayoutBinding.bind(view)
        activityForResult = viewModel.getActivityForResult(this)
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginConstraint) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
        context?.let { Glide.with(it).load(R.drawable.google).into(binding.loginLogo) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.newConnectivityObserver.collectLatest {
                        when (it) {
                            ConnectivityObserver.Status.Unavailable -> {
                                binding.loginConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.loginConnectivityLayout
                                )
                                binding.loginConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.no_internet_signal)
                                loginButtonStatus(false)
                            }

                            ConnectivityObserver.Status.Losing -> {
                                binding.loginConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.loginConnectivityLayout
                                )
                                binding.loginConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.losing_internet_signal)
                                delay(2000)
                                binding.loginConnectivityLayout.root.visibility = View.GONE
                                loginButtonStatus(false)
                            }

                            ConnectivityObserver.Status.Available -> {
                                binding.loginConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), true, binding.loginConnectivityLayout
                                )
                                binding.loginConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.signal_restored)
                                delay(2000)
                                binding.loginConnectivityLayout.root.visibility = View.GONE
                                loginButtonStatus(true)
                            }

                            ConnectivityObserver.Status.Lost -> {
                                binding.loginConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.loginConnectivityLayout
                                )
                                binding.loginConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.you_lost_signal)
                                loginButtonStatus(false)
                            }
                        }
                    }
                }
                launch {
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
        }

        binding.loginButton.setOnClickListener {
            viewModel.startLoggingWithGoogle(this, activityForResult)
        }
    }

    private fun loginButtonStatus(isAvailable: Boolean) {
        if (isAvailable) {
            binding.loginButton.visibility = View.VISIBLE
            binding.loginSubtitle.text = getString(R.string.please_sign_in)
        } else {
            binding.loginButton.visibility = View.GONE
            binding.loginSubtitle.text = getString(R.string.get_connection)
        }
    }
}