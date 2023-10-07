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
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.LoginScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.onboarding.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

        val isLoggedCompleted: Flow<Boolean>? = this.context?.dataStore?.data?.map { preferences ->
            preferences[isLoggedCompleted] ?: false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                isLoggedCompleted?.collectLatest {
                    if (it) {
                        findNavController().navigate(R.id.action_loginScreen_to_homeScreen)
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