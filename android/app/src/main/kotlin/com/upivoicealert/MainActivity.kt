package com.upivoicealert

import android.os.Bundle
import com.getcapacitor.BridgeActivity

class MainActivity : BridgeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize TTS engine
        TTSEngine.initialize(this)
    }

    override fun onDestroy() {
        TTSEngine.shutdown()
        super.onDestroy()
    }
}
