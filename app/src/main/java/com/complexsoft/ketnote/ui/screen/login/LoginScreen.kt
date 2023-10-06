package com.complexsoft.ketnote.ui.screen.login

import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.LoginScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.MainViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException

class LoginScreen : Fragment(R.layout.login_screen_layout) {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val TAG = "tag"
    private val WEB_CLIENT =
        "584951843971-pl6j9brdsqo2160tn3798oubc8126le7.apps.googleusercontent.com"
    private lateinit var binding: LoginScreenLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LoginScreenLayoutBinding.inflate(layoutInflater)
        val viewModel by activityViewModels<MainViewModel>()

        oneTapClient = activity?.let { Identity.getSignInClient(it) }!!
        signInRequest = BeginSignInRequest.builder().setPasswordRequestOptions(
            BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build()
        ).setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setServerClientId(WEB_CLIENT).setFilterByAuthorizedAccounts(false).build()
        ).setAutoSelectEnabled(true).build()
        val activityForResult = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                val username = credential.id
                val userImage = credential.profilePictureUri
                when {
                    idToken != null -> {
                        val newUser = User(idToken, username, userImage.toString())
                        viewModel.userLoggedCompleted(newUser)
                        findNavController().navigate(R.id.action_loginScreen_to_homeScreen)
                    }

                    else -> {
                        Log.d(TAG, "No ID token or password!")
                    }
                }
            } catch (e: ApiException) {
                println(e)
            }

        }
        binding.loginButton.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(requireActivity()) { result ->
                    try {
                        val intentSender =
                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        activityForResult.launch(intentSender)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }.addOnFailureListener(requireActivity()) { e ->
                    e.localizedMessage?.let { it1 -> Log.d(TAG, it1) }
                }

        }
        return binding.root
    }
}