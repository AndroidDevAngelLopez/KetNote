package com.complexsoft.ketnote.ui.screen.utils

import android.speech.tts.TextToSpeech
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import com.complexsoft.ketnote.utils.PasswordsConstants
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

object UIConstants {
    var UPLOADTASK: StorageReference = Firebase.storage.reference.child("/")
    lateinit var PICKMEDIA: ActivityResultLauncher<PickVisualMediaRequest>
    var STORAGE: FirebaseStorage = Firebase.storage
    var STORAGEREF: StorageReference = STORAGE.reference
    val SIGNINREQUEST: BeginSignInRequest =
        BeginSignInRequest.builder().setPasswordRequestOptions(
            BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build()
        ).setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setServerClientId(PasswordsConstants.WEB_CLIENT).setFilterByAuthorizedAccounts(false)
                .build()
        ).setAutoSelectEnabled(true).build()
    val TAG = "tag"
    val AUTH = Firebase.auth
    lateinit var TEXTTOSPEECH: TextToSpeech
}