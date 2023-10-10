package com.complexsoft.ketnote.domain.usecases

import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.complexsoft.ketnote.data.repository.MongoDBAPP
import com.complexsoft.ketnote.ui.screen.login.LoginScreen
import com.complexsoft.ketnote.utils.Constants
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginUseCase {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val TAG = "tag"
    private lateinit var auth: FirebaseAuth

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
        auth = Firebase.auth
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
                    CoroutineScope(Dispatchers.IO).launch {
                        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(activity.requireActivity()) { task ->
                                if (task.isSuccessful) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        runCatching {
                                            MongoDBAPP.app.login(
                                                Credentials.google(
                                                    token.toString(), GoogleAuthType.ID_TOKEN
                                                )
                                            )
                                        }.onSuccess {
                                            Log.d(
                                                TAG,
                                                "you logged in mongo with this account ${MongoDBAPP.app.currentUser?.id}!"
                                            )
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                                }
                            }
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