package com.luviio.upialert.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.luviio.upialert.MainActivity
import com.luviio.upialert.R
import com.luviio.upialert.database.AppDatabase
import com.luviio.upialert.database.Transaction
import com.luviio.upialert.utils.NotificationParser
import kotlinx.coroutines.*

class UPIListenerService : NotificationListenerService() {

    private val TAG = "LU VIIO Service"
    private lateinit var tts: TextToSpeech
    private lateinit var db: AppDatabase
    private lateinit var parser: NotificationParser
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val processedNotifications = mutableSetOf<String>()
    
    private val upiPackages = listOf(
        "net.one97.paytm",
        "com.phonepe.app",
        "com.google.android.apps.nbu.paisa.user",
        "com.bhim",
        "in.amazon.mShop.android.shopping",
        "com.whatsapp",
        "com.freecharge.android",
        "com.mobikwik"
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 LU VIIO Service Started")
        
        db = AppDatabase.getInstance(this)
        parser = NotificationParser()
        
        initTTS()
        startForegroundService()
    }

    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val lang = getSharedPreferences("luviio_settings", MODE_PRIVATE)
                    .getString("language", "hindi")
                
                tts.language = if (lang == "hindi") 
                    Locale("hi", "IN") 
                else 
                    Locale.ENGLISH
                
                tts.setPitch(1.0f)
                tts.setSpeechRate(1.0f)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notificationId = "${packageName}_${sbn.postTime}"
        
        if (processedNotifications.contains(notificationId)) {
            return
        }
        
        processedNotifications.add(notificationId)
        
        if (processedNotifications.size > 100) {
            processedNotifications.remove(processedNotifications.first())
        }

        if (packageName in upiPackages) {
            processUPINotification(sbn.notification, packageName, sbn.postTime)
        }
    }

    private fun processUPINotification(
        notification: Notification, 
        packageName: String,
        timestamp: Long
    ) {
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE, "")
        val text = extras.getString(Notification.EXTRA_TEXT, "")

        val transaction = parser.parseUPINotification(text, title, packageName)
        
        if (transaction != null) {
            processTransaction(transaction, timestamp)
        }
    }

    private fun processTransaction(transaction: Transaction, timestamp: Long) {
        serviceScope.launch {
            try {
                transaction.timestamp = timestamp
                db.transactionDao().insert(transaction)

                delay(500)
                announceTransaction(transaction)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

    private fun announceTransaction(transaction: Transaction) {
        val amount = transaction.amount
        val sender = transaction.sender
        
        val message = when (getSharedPreferences("luviio_settings", MODE_PRIVATE)
            .getString("language", "hindi")) {
            "hindi" -> "$amount रुपये प्राप्त हुए"
            "english" -> "$amount rupees received"
            else -> "$amount rupees received"
        }

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        
        if (currentVolume < maxVolume * 0.6) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                (maxVolume * 0.8).toInt(),
                0
            )
        }

        val fullMessage = "🔊 LU VIIO: $message"
        tts.speak(fullMessage, TextToSpeech.QUEUE_FLUSH, null, transaction.transactionId)

        Handler(Looper.getMainLooper()).postDelayed({
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
        }, 3000)
    }

    private fun startForegroundService() {
        val channelId = "LU_VIIO_SERVICE_CHANNEL"
        val notificationId = 1001

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "LU VIIO UPI Alert Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Listening for UPI payment notifications"
                setSound(null, null)
                enableVibration(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("🔊 LU VIIO UPI Alert")
            .setContentText("Active - Listening for UPI payments")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(notificationId, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        tts.stop()
        tts.shutdown()
    }
}