package com.upi.announcer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(saved(InstanceState: Bundle?)) {
        super.onCreate(savedInstanceState)
        
        val button = Button(this).apply {
            text = "Enable Notification Access for App"
            setOnClickListener {
                // User ko notification settings me bhejte hain
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                Toast.makeText(context, "Please allow 'UPI Announcer' here", Toast.LENGTH_LONG).show()
            }
        }
        setContentView(button)
    }
}