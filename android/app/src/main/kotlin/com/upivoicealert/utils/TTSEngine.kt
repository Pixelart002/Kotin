package com.upivoicealert.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

object TTSEngine {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val TAG = "TTSEngine"

    fun initialize(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isReady = true
                Log.d(TAG, "TTS Engine initialized successfully")
            } else {
                Log.e(TAG, "TTS Engine initialization failed")
            }
        }
    }

    fun speak(text: String) {
        if (isReady && tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        } else {
            Log.w(TAG, "TTS Engine not ready")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking ?: false
}
