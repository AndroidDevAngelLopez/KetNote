package com.complexsoft.ketnote.domain.usecases

import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.complexsoft.ketnote.ui.screen.utils.UIConstants
import com.google.firebase.auth.FirebaseAuth

class OpenImageChooserUseCase {
    operator fun invoke(
        fragment: Fragment, onUriReturn: (Uri) -> Unit
    ): ActivityResultLauncher<PickVisualMediaRequest> {
        return fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                if (uri.toString().isNotEmpty()) {
                    val remoteImagePath =
                        "images/${FirebaseAuth.getInstance().currentUser?.uid}/" + "${uri.lastPathSegment}-${System.currentTimeMillis()}.jpg"
                    UIConstants.UPLOADTASK = UIConstants.STORAGEREF.child(remoteImagePath)
                    onUriReturn(uri)
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    }
}