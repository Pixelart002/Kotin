package com.luviio.upialert.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.luviio.upialert.service.UPIListenerService

class BootReceiver : BroadcastReceiver() {
    
    private val TAG = "LU VIIO BootReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "📱 Phone rebooted - Restarting UPI Listener Service")
            
            val serviceIntent = Intent(context, UPIListenerService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            Log.d(TAG, "✅ Service restarted successfully")
        }
    }
}
