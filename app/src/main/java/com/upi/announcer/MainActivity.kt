package com.upi.announcer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        // 🚀 Jetpack Compose UI Start
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2563EB), // Smooth UI jaisa Blue
                    background = Color(0xFFF8FAFC)
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppScreen()
                }
            }
        }
    }

    @Composable
    fun AppScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "UPI Announcer Pro",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Keep running in background",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Step 1: Notification Permission Card
            ActionCard(
                title = "1. Notification Access",
                description = "Required to read UPI messages.",
                buttonText = "Enable Access",
                onClick = {
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    Toast.makeText(this@MainActivity, "Allow UPI Announcer", Toast.LENGTH_LONG).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Battery Optimization Fix (Background Kill roke ga)
            ActionCard(
                title = "2. Stop App Killing",
                description = "Prevent phone from killing this app.",
                buttonText = "Allow Background",
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Test Button
            Button(
                onClick = {
                    tts.speak("Received ₹500 from Rahul on Paytm", TextToSpeech.QUEUE_FLUSH, null, "")
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)) // Green Color
            ) {
                Text("Test Voice Announcement", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modern Card Component
    @Composable
    fun ActionCard(title: String, description: String, buttonText: String, onClick: () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(text = description, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    Text(buttonText)
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}