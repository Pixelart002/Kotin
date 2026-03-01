package com.luviio.upialert.utils

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

class TTSUtils(private val context: Context) {
    
    private lateinit var tts: TextToSpeech
    private var isInitialized = false
    private val TAG = "LU VIIO TTS"
    
    fun initialize(listener: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                setLanguage("hindi")
                listener()
            }
        }
        
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "Started speaking")
            }
            
            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "Finished speaking")
            }
            
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Error speaking")
            }
        })
    }
    
    fun setLanguage(language: String) {
        if (!::tts.isInitialized) return
        
        val locale = when (language.lowercase()) {
            "hindi" -> Locale("hi", "IN")
            "tamil" -> Locale("ta", "IN")
            "telugu" -> Locale("te", "IN")
            "kannada" -> Locale("kn", "IN")
            else -> Locale.ENGLISH
        }
        
        tts.setLanguage(locale)
    }
    
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (!isInitialized) return
        
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        
        if (currentVolume < maxVolume * 0.6) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                (maxVolume * 0.7).toInt(),
                0
            )
        }
        
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
        
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params)
    }
    
    fun setPitch(pitch: Float) {
        if (::tts.isInitialized) {
            tts.setPitch(pitch)
        }
    }
    
    fun setSpeechRate(rate: Float) {
        if (::tts.isInitialized) {
            tts.setSpeechRate(rate)
        }
    }
    
    fun stop() {
        if (::tts.isInitialized) {
            tts.stop()
        }
    }
    
    fun shutdown() {
        if (::tts.isInitialized) {
            tts.shutdown()
        }
    }
}