package com.javis.launcher.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.javis.launcher.R
import com.javis.launcher.databinding.ActivityHomeBinding
import com.javis.launcher.models.OrbAnimationState
import com.javis.launcher.services.JavisService
import com.javis.launcher.ui.allapps.AllAppsActivity
import com.javis.launcher.ui.chat.ChatActivity
import com.javis.launcher.ui.memory.MemoryActivity
import com.javis.launcher.ui.missioncontrol.MissionControlActivity
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val vm: HomeViewModel by viewModels()

    private var touchStartX = 0f; private var touchStartY = 0f

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        requestPermissions()
        setupObservers()
        setupGestures()
        vm.initialize()
        JavisService.start(this)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            vm.orbState.collect { state ->
                binding.orbView.setState(state.animState)
                binding.tvStatus.text = state.statusText
            }
        }
        lifecycleScope.launch {
            vm.greeting.collect { binding.tvGreeting.text = it }
        }
        lifecycleScope.launch {
            vm.statusText.collect { binding.tvStatus.text = it }
        }
    }

    private fun setupGestures() {
        binding.root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { touchStartX = event.x; touchStartY = event.y; true }
                MotionEvent.ACTION_UP -> {
                    val dx = event.x - touchStartX; val dy = event.y - touchStartY
                    if (abs(dx) > 80 || abs(dy) > 80) {
                        handleSwipe(dx, dy); true
                    } else false
                }
                else -> false
            }
        }
        binding.orbView.setOnClickListener { vm.toggleListening() }
        binding.searchBar.setOnEditorActionListener { _, _, _ ->
            val text = binding.searchBar.text.toString().trim()
            if (text.isNotEmpty()) { vm.handleInput(text); binding.searchBar.text?.clear() }
            true
        }
    }

    private fun handleSwipe(dx: Float, dy: Float) {
        val absDx = abs(dx); val absDy = abs(dy)
        if (absDx > absDy) {
            if (dx > 0) navigate(MemoryActivity::class.java) else navigate(ChatActivity::class.java)
        } else {
            if (dy < 0) navigate(AllAppsActivity::class.java) else navigate(MissionControlActivity::class.java)
        }
    }

    private fun <T> navigate(cls: Class<T>) {
        startActivity(Intent(this, cls))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun requestPermissions() {
        val needed = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE, Manifest.permission.POST_NOTIFICATIONS)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            .toTypedArray()
        if (needed.isNotEmpty()) permissionLauncher.launch(needed)
    }

    override fun onBackPressed() { /* swallow — launcher */ }
    override fun onResume() { super.onResume(); vm.initialize() }
}
