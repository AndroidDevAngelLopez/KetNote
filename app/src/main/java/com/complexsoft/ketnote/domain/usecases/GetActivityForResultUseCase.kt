package com.complexsoft.ketnote.domain.usecases

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.complexsoft.ketnote.data.repository.MongoDBAPP
import com.complexsoft.ketnote.ui.screen.login.LoginScreen
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GetActivityForResultUseCase {
    operator fun invoke(
        oneTapClient: SignInClient, auth: FirebaseAuth, activity: LoginScreen, tag: String
    ): ActivityResultLauncher<IntentSenderRequest> {
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
                                                tag,
                                                "you logged in mongo with this account ${MongoDBAPP.app.currentUser?.id}!"
                                            )
                                        }
                                    }
                                } else {
                                    Log.w(tag, "signInWithCredential:failure", task.exception)
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