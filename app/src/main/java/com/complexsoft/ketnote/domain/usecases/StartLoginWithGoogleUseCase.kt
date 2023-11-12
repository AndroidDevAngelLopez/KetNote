package com.complexsoft.ketnote.domain.usecases

import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.complexsoft.ketnote.ui.screen.login.LoginScreen
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient

class StartLoginWithGoogleUseCase {

    operator fun invoke(
        activity: LoginScreen,
        oneTapClient: SignInClient,
        signInRequest: BeginSignInRequest,
        activityForResult: ActivityResultLauncher<IntentSenderRequest>,
        tag: String
    ) {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(activity.requireActivity()) { result ->
                try {
                    val intentSender =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    activityForResult.launch(intentSender)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(tag, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }.addOnFailureListener(activity.requireActivity()) { e ->
                e.localizedMessage?.let { it1 -> Log.d(tag, it1) }
            }
    }
}