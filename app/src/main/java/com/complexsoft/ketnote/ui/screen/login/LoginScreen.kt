package com.complexsoft.ketnote.ui.screen.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
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
        activityForResult = viewModel.getActivityForResult(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LoginScreenLayoutBinding.inflate(layoutInflater)
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
                                switchConnectivityObserverLayoutColor(false)
                                binding.loginConnectivityLayout.connectivityLayoutMessage.text =
                                    "No tienes internet!"
                                loginButtonStatus(false)
                            }

                            ConnectivityObserver.Status.Losing -> {
                                binding.loginConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(false)
                                binding.loginConnectivityLayout.connectivityLayoutMessage.text =
                                    "Estas perdiendo conexion!"
                                delay(2000)
                                binding.loginConnectivityLayout.root.visibility = View.GONE
                                loginButtonStatus(false)
                            }

                            ConnectivityObserver.Status.Available -> {
                                binding.loginConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(true)
                                binding.loginConnectivityLayout.connectivityLayoutMessage.text =
                                    "Conexion establecida!"
                                delay(2000)
                                binding.loginConnectivityLayout.root.visibility = View.GONE
                                loginButtonStatus(true)
                            }

                            ConnectivityObserver.Status.Lost -> {
                                binding.loginConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(false)
                                binding.loginConnectivityLayout.connectivityLayoutMessage.text =
                                    "Perdiste la conexion!"
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
        return binding.root
    }

    private fun loginButtonStatus(isAvailable: Boolean) {
        if (isAvailable) {
            binding.loginButton.visibility = View.VISIBLE
            binding.loginSubtitle.text = getString(R.string.please_sign_in)
        } else {
            binding.loginButton.visibility = View.GONE
            binding.loginSubtitle.text = "Conectate a internet para comenzar!"
        }
    }

    private fun switchConnectivityObserverLayoutColor(isAvailable: Boolean) {
        if (isAvailable) {
            this@LoginScreen.context?.let { it1 ->
                ContextCompat.getColor(
                    it1, R.color.md_theme_light_tertiary
                )
            }?.let { it2 ->
                binding.loginConnectivityLayout.connectivityLayout.setBackgroundColor(
                    it2
                )
            }
        } else {
            this@LoginScreen.context?.let { it1 ->
                ContextCompat.getColor(
                    it1, R.color.md_theme_light_error
                )
            }?.let { it2 ->
                binding.loginConnectivityLayout.connectivityLayout.setBackgroundColor(
                    it2
                )
            }
        }
    }
}