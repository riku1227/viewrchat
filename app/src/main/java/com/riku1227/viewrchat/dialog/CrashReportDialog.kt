package com.riku1227.viewrchat.dialog

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.riku1227.viewrchat.R

class CrashReportDialog(private val stackTrace: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(context!!)
        dialogBuilder.setTitle(R.string.dialog_crash_report_title)
            .setPositiveButton(R.string.dialog_crash_report_positive) {
                    _, _ ->
                val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("crash_log", stackTrace))
            }
            .setNegativeButton(R.string.dialog_crash_report_negative) {
                    _, _ -> dismiss()
            }
        this.isCancelable = false

        return dialogBuilder.create()
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}