package com.javis.launcher.ui.chat

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.javis.launcher.databinding.ActivityChatBinding
import com.javis.launcher.models.OrbAnimationState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupRecyclerView()
        setupInput()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter()
        binding.rvMessages.apply {
            this.adapter = this@ChatActivity.adapter
            layoutManager = LinearLayoutManager(this@ChatActivity).apply { stackFromEnd = true }
        }
    }

    private fun setupInput() {
        binding.btnSend.setOnClickListener { sendMessage() }
        binding.btnVoice.setOnClickListener { viewModel.toggleVoiceInput() }
        binding.etInput.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
        }
    }

    private fun sendMessage() {
        val text = binding.etInput.text.toString().trim()
        if (text.isNotBlank()) {
            binding.etInput.text?.clear()
            viewModel.sendMessage(text)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.messages.collectLatest { msgs ->
                adapter.submitList(msgs)
                if (msgs.isNotEmpty()) binding.rvMessages.scrollToPosition(msgs.size - 1)
            }
        }
        lifecycleScope.launch {
            viewModel.isTyping.collectLatest { typing ->
                binding.tvTypingIndicator.visibility = if (typing) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
        lifecycleScope.launch {
            viewModel.voiceState.collectLatest { state ->
                binding.btnVoice.setImageResource(
                    if (state == com.javis.launcher.voice.VoiceState.LISTENING)
                        android.R.drawable.ic_btn_speak_now
                    else android.R.drawable.ic_lock_silent_mode_off
                )
            }
        }
    }
}
