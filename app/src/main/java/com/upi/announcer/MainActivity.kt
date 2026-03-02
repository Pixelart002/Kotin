package com.upi.announcer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import java.util.Locale

// Yahan humne TextToSpeech ko bhi implement kar liya test karne ke liye
class MainActivity : Activity(), TextToSpeech.OnInitListener {
    
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test button ke liye Offline TTS initialize kar rahe hain
        tts = TextToSpeech(this, this)

        // Screen ka layout banate hain (Upar-neeche buttons ke liye)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 50)
        }
        
        // Pehla Button: Permission ke liye
        val permissionButton = Button(this).apply {
            text = "1. Enable Notification Access"
            textSize = 18f
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                Toast.makeText(context, "Please allow 'UPI Announcer' here", Toast.LENGTH_LONG).show()
            }
        }
        
        // Dusra Button: Test Voice ke liye
        val testButton = Button(this).apply {
            text = "2. Test Voice Announcement"
            textSize = 18f
            // Thoda gap dene ke liye
            setPadding(0, 20, 0, 20)
            
            setOnClickListener {
                val samplePayment = "Received ₹500 from Rahul on Paytm"
                // Button dabate hi phone dummy payment bolega
                tts.speak(samplePayment, TextToSpeech.QUEUE_FLUSH, null, "")
                Toast.makeText(context, "Testing voice...", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Dono buttons ko layout me daal do
        layout.addView(permissionButton)
        layout.addView(testButton)
        
        // Layout ko screen par set kar do
        setContentView(layout)
    }

    // TTS ko Hindi/Indian English me set karna
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
        }
    }

    // Jab app band ho toh TTS memory clear kar de
    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}