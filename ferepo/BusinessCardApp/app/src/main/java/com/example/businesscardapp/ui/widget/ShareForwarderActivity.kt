package com.example.businesscardapp.ui.widget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.businesscardapp.MainActivity

class ShareForwarderActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                putExtra("route", "mycard/share")
            }
        )
        finish()
    }
}
