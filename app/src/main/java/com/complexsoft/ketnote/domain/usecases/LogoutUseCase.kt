package com.complexsoft.ketnote.domain.usecases

import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.complexsoft.ketnote.data.network.MongoDBAPP
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class LogoutUseCase {
    fun logoutUser(activity: FragmentActivity) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                MongoDBAPP.app.currentUser?.remove()
            }.onSuccess {
                Firebase.auth.signOut()
                delay(800)
                val intent = activity.baseContext.packageManager.getLaunchIntentForPackage(
                    activity.baseContext.packageName
                )
                intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
                Process.killProcess(Process.myPid())
                exitProcess(0)
            }.onFailure {
                Log.d("LOGOUT FAILED", it.message.toString())
            }
        }
    }
}