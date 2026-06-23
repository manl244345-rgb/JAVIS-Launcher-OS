package com.javis.launcher.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.javis.launcher.JavisApplication
import com.javis.launcher.R
import com.javis.launcher.databinding.ActivityHomeBinding
import com.javis.launcher.models.OrbAnimationState
import com.javis.launcher.services.JavisService
import com.javis.launcher.ui.allapps.AllAppsActivity
import com.javis.launcher.ui.chat.ChatActivity
import com.javis.launcher.ui.memory.MemoryActivity
import com.javis.launcher.ui.missioncontrol.MissionControlActivity
import com.javis.launcher.voice.VoiceState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var gestureDetector: GestureDetector

    private val requiredPermissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* proceed regardless — JAVIS degrades gracefully */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissions()
        startJavisService()
        setupGestures()
        setupOrb()
        setupSearch()
        observeViewModel()
        viewModel.refreshGreeting()
    }

    private fun requestPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) permissionLauncher.launch(missing.toTypedArray())
    }

    private fun startJavisService() {
        val intent = Intent(this, JavisService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
    }

    private fun setupOrb() {
        binding.orbView.setOnClickListener { viewModel.toggleListening() }
        binding.orbView.setOnLongClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
            true
        }
    }

    private fun setupSearch() {
        binding.searchBar.setOnClickListener { startActivity(Intent(this, AllAppsActivity::class.java)) }
        binding.searchBar.setOnEditorActionListener { v, _, _ ->
            val query = v.text.toString().trim()
            if (query.isNotBlank()) viewModel.handleTextInput(query)
            true
        }
    }

    private fun setupGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 120
            private val SWIPE_VELOCITY = 100

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                val dX = e2.x - (e1?.x ?: 0f)
                val dY = e2.y - (e1?.y ?: 0f)
                return when {
                    abs(dX) > abs(dY) && abs(dX) > SWIPE_THRESHOLD && abs(vX) > SWIPE_VELOCITY -> {
                        if (dX < 0) { startActivity(Intent(this@HomeActivity, ChatActivity::class.java)) }
                        else { startActivity(Intent(this@HomeActivity, MemoryActivity::class.java)) }
                        true
                    }
                    abs(dY) > abs(dX) && abs(dY) > SWIPE_THRESHOLD && abs(vY) > SWIPE_VELOCITY -> {
                        if (dY < 0) { startActivity(Intent(this@HomeActivity, AllAppsActivity::class.java)) }
                        else { startActivity(Intent(this@HomeActivity, MissionControlActivity::class.java)) }
                        true
                    }
                    else -> false
                }
            }
        })
        binding.root.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event); false }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.greeting.collectLatest { binding.tvGreeting.text = it }
        }
        lifecycleScope.launch {
            viewModel.orbState.collectLatest { state ->
                binding.orbView.setState(state.state)
                when (state.state) {
                    OrbAnimationState.LISTENING -> binding.tvStatus.text = "Listening..."
                    OrbAnimationState.THINKING -> binding.tvStatus.text = "Thinking..."
                    OrbAnimationState.SPEAKING -> binding.tvStatus.text = state.message
                    OrbAnimationState.EXECUTING -> binding.tvStatus.text = "Executing..."
                    OrbAnimationState.COMPLETED -> binding.tvStatus.text = "Done"
                    else -> binding.tvStatus.text = "Tap to speak"
                }
            }
        }
        lifecycleScope.launch {
            viewModel.voiceText.collectLatest { if (it.isNotBlank()) binding.tvStatus.text = it }
        }
        lifecycleScope.launch {
            viewModel.favoriteApps.collectLatest { apps ->
                binding.favoritesContainer.removeAllViews()
                apps.take(6).forEach { app ->
                    // Add favorite app icons dynamically
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onBackPressed() { /* Launcher — don't go back */ }
}
