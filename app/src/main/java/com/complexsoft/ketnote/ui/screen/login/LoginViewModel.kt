package com.complexsoft.ketnote.ui.screen.login

import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.network.MongoDBAPP
import com.complexsoft.ketnote.utils.Constants
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import io.realm.kotlin.mongodb.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class LoginViewModel : ViewModel() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val TAG = "tag"


    fun startLoggingWithGoogle(
        activity: LoginScreen, activityForResult: ActivityResultLauncher<IntentSenderRequest>
    ) {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(activity.requireActivity()) { result ->
                try {
                    val intentSender =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    activityForResult.launch(intentSender)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }.addOnFailureListener(activity.requireActivity()) { e ->
                e.localizedMessage?.let { it1 -> Log.d(TAG, it1) }
            }
    }

    fun getActivityForResult(activity: LoginScreen): ActivityResultLauncher<IntentSenderRequest> {
        oneTapClient = activity.let { Identity.getSignInClient(it.requireActivity()) }
        signInRequest = BeginSignInRequest.builder().setPasswordRequestOptions(
            BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build()
        ).setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setServerClientId(Constants.WEB_CLIENT).setFilterByAuthorizedAccounts(false)
                .build()
        ).setAutoSelectEnabled(true).build()
        return activity.registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            val task: SignInCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
            try {
                if (task.googleIdToken != null) {
                    val token = task.googleIdToken
                    viewModelScope.launch {
                        val user = AtomicReference<User?>()
                        user.set(MongoDBAPP.app.currentUser)
                        val googleCredentials = Credentials.google(
                            token.toString(), GoogleAuthType.ID_TOKEN
                        )
                        MongoDBAPP.app.login(
                            googleCredentials
                        )
                        delay(800)
                    }
                } else {
                    Log.e("AUTH", "Google Auth failed: ${task.id}")
                }
            } catch (e: ApiException) {
                Log.e("AUTH", "Failed to authenticate using Google OAuth: " + e.message)
            }
        }
    }
}
