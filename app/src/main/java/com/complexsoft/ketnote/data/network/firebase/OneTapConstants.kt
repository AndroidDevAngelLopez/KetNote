package com.complexsoft.ketnote.data.network.firebase

import com.complexsoft.ketnote.utils.PasswordsConstants.WEB_CLIENT
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object OneTapConstants {
    val signInRequest: BeginSignInRequest =
        BeginSignInRequest.builder().setPasswordRequestOptions(
            BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build()
        ).setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder().setSupported(true)
                .setServerClientId(WEB_CLIENT).setFilterByAuthorizedAccounts(false)
                .build()
        ).setAutoSelectEnabled(true).build()
    val TAG = "tag"
    val auth = Firebase.auth
}