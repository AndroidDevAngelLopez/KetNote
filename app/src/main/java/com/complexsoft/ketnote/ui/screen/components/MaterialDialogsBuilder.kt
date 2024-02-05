package com.complexsoft.ketnote.ui.screen.components

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun createSimpleDialog(
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

fun createRadioButtonDialog(
    context: Context?,
    title: String,
    ok: String,
    spanish: String,
    english: String,
    onSpanishAction: () -> Unit,
    onEnglishAction: () -> Unit,
    getCheckedItem: Int
) = context?.let { MaterialAlertDialogBuilder(it) }?.setTitle(title)
    ?.setPositiveButton(ok) { dialog, which ->
        dialog.dismiss()
    }?.setSingleChoiceItems(
        arrayOf(english, spanish), getCheckedItem
    ) { _, which ->
        when (which) {
            0 -> {
                onEnglishAction()
            }

            1 -> {
                onSpanishAction()
            }
        }
    }?.create()

