package com.complexsoft.ketnote.domain.usecases

import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.complexsoft.ketnote.ui.screen.utils.UIConstants
import java.io.File

class ShareToInstagramUseCase {
    operator fun invoke(image: String, fragment: Fragment) {
        val currentImage = UIConstants.STORAGE.getReferenceFromUrl(image)
        val localPath =
            fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val temporalImagePath = File.createTempFile("temp", ".jpg", localPath)
        currentImage.getFile(temporalImagePath).addOnSuccessListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.setPackage("com.instagram.android")
            intent.type = "image/*"
            val imageToSend = FileProvider.getUriForFile(
                fragment.requireContext(),
                fragment.requireContext().packageName + ".fileprovider",
                temporalImagePath
            )
            intent.putExtra(Intent.EXTRA_STREAM, imageToSend)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (fragment.requireContext()
                    .let { it1 -> intent.resolveActivity(it1.packageManager) } != null
            ) {
                fragment.requireContext().startActivity(intent)
            } else {
                // Instagram is not installed, handle accordingly
                // You may redirect the user to the Play Store to download Instagram
                // or show a message suggesting to install Instagram
            }
        }.addOnFailureListener {

        }
    }
}