package com.complexsoft.ketnote.ui.screen.components

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun createDialog(
    context: Context?,
    title: String,
    message: String,
    negativeText: String,
    positiveText: String,
    onConfirmAction: () -> Unit
) = context?.let {
    MaterialAlertDialogBuilder(it).setTitle(title).setMessage(message)
        .setNegativeButton(negativeText) { dialog, witch ->
            dialog.dismiss()
        }.setPositiveButton(positiveText) { dialog, witch ->
            onConfirmAction()
            dialog.dismiss()
        }
}