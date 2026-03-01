package com.luviio.upialert

import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.luviio.upialert.database.AppDatabase
import com.luviio.upialert.service.UPIListenerService
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var btnStart: Button
    private lateinit var btnSettings: Button
    private lateinit var switchHindi: SwitchMaterial
    private lateinit var switchEnglish: SwitchMaterial
    private lateinit var tvTodayTotal: TextView
    private lateinit var tvWeekTotal: TextView
    private lateinit var tvMonthTotal: TextView
    private lateinit var recyclerView: RecyclerView
    
    private lateinit var tts: TextToSpeech
    private lateinit var db: AppDatabase
    private lateinit var audioManager: AudioManager
    
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initTTS()
        initDatabase()
        setupRecyclerView()
        setupListeners()
        checkPermissions()
        loadStats()
    }

    private fun initViews() {
        statusText = findViewById(R.id.statusText)
        btnStart = findViewById(R.id.btnStart)
        btnSettings = findViewById(R.id.btnSettings)
        switchHindi = findViewById(R.id.switchHindi)
        switchEnglish = findViewById(R.id.switchEnglish)
        tvTodayTotal = findViewById(R.id.tvTodayTotal)
        tvWeekTotal = findViewById(R.id.tvWeekTotal)
        tvMonthTotal = findViewById(R.id.tvMonthTotal)
        recyclerView = findViewById(R.id.recyclerView)
        
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        
        val savedLanguage = getSharedPreferences("luviio_settings", MODE_PRIVATE)
            .getString("language", "hindi")
        
        if (savedLanguage == "hindi") {
            switchHindi.isChecked = true
            switchEnglish.isChecked = false
        } else {
            switchHindi.isChecked = false
            switchEnglish.isChecked = true
        }
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
            }
        }
    }

    private fun initDatabase() {
        db = AppDatabase.getInstance(this)
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter
        
        lifecycleScope.launch {
            db.transactionDao().getRecentTransactions().collect { transactions ->
                transactionAdapter.submitList(transactions)
            }
        }
    }

    private fun setupListeners() {
        btnStart.setOnClickListener {
            when {
                !isNotificationServiceEnabled() -> {
                    requestNotificationPermission()
                }
                else -> {
                    startUPIService()
                }
            }
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        switchHindi.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchEnglish.isChecked = false
                tts.language = Locale("hi", "IN")
                saveLanguagePreference("hindi")
                Toast.makeText(this, "हिन्दी भाषा चुनी गई", Toast.LENGTH_SHORT).show()
            }
        }

        switchEnglish.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchHindi.isChecked = false
                tts.language = Locale.ENGLISH
                saveLanguagePreference("english")
                Toast.makeText(this, "English language selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        when {
            !isNotificationServiceEnabled() -> {
                statusText.text = getString(R.string.status_permission_needed)
                btnStart.text = getString(R.string.btn_grant_permission)
            }
            else -> {
                statusText.text = getString(R.string.status_ready)
                btnStart.text = getString(R.string.btn_start)
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
    }

    private fun requestNotificationPermission() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        Toast.makeText(this, "🔔 Enable notification access for UPI alerts", Toast.LENGTH_LONG).show()
    }

    private fun startUPIService() {
        val intent = Intent(this, UPIListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        statusText.text = getString(R.string.status_running)
        btnStart.text = "✅ Service Active"
        btnStart.isEnabled = false
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val today = db.transactionDao().getTodayTotal(getStartOfDay())
                val week = db.transactionDao().getWeekTotal(getStartOfWeek())
                val month = db.transactionDao().getMonthTotal(getStartOfMonth())
                
                runOnUiThread {
                    tvTodayTotal.text = "₹${formatAmount(today)}"
                    tvWeekTotal.text = "₹${formatAmount(week)}"
                    tvMonthTotal.text = "₹${formatAmount(month)}"
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun saveLanguagePreference(lang: String) {
        getSharedPreferences("luviio_settings", MODE_PRIVATE)
            .edit()
            .putString("language", lang)
            .apply()
    }
    
    private fun formatAmount(amount: Double): String {
        return if (amount == 0.0) "0" else String.format("%.0f", amount)
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        loadStats()
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}