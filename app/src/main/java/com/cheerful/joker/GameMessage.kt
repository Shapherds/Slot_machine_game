package com.cheerful.joker

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


class GameMessage(val isWin: Boolean) : DialogFragment (){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title: String
        val body: String
        if (isWin) {
            title = "You won!"
            body = "You won ! Congratulate ! "
        } else {
            title = "You lose"
            body = "You lose , try next time"
        }
        val builder = AlertDialog.Builder(
            context!!
        )
        builder.setTitle(title)
            .setMessage(body)
            .setPositiveButton("ОК") { dialog: DialogInterface, id: Int -> dialog.cancel() }
            .setCancelable(false)
        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        GameActivity.isEnd = true
        super.onCancel(dialog)
    }
}