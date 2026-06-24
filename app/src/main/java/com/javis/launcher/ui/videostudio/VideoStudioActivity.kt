package com.javis.launcher.ui.videostudio

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.javis.launcher.databinding.ActivityVideoStudioBinding

class VideoStudioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoStudioBinding
    private var videoUri: Uri? = null

    private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        videoUri = uri
        binding.videoView.setVideoURI(uri)
        binding.videoView.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Video Studio"
        binding.btnPickVideo.setOnClickListener { pickVideo.launch("video/*") }
        binding.btnPlay.setOnClickListener { binding.videoView.start() }
        binding.btnPause.setOnClickListener { binding.videoView.pause() }
        binding.btnTrim.setOnClickListener { Toast.makeText(this, "Trim: requires FFmpeg integration. Coming soon.", Toast.LENGTH_LONG).show() }
        binding.btnCaption.setOnClickListener { Toast.makeText(this, "AI captions: requires Whisper integration. Coming soon.", Toast.LENGTH_LONG).show() }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
