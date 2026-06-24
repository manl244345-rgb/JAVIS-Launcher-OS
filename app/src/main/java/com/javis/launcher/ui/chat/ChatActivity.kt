package com.javis.launcher.ui.chat

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.javis.launcher.databinding.ActivityChatBinding
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val vm: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "JAVIS Chat"

        adapter = ChatAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(this).also { it.stackFromEnd = true }
        binding.rvMessages.adapter = adapter

        lifecycleScope.launch {
            vm.messages.collect { msgs ->
                adapter.submitList(msgs)
                if (msgs.isNotEmpty()) binding.rvMessages.smoothScrollToPosition(msgs.size - 1)
            }
        }
        lifecycleScope.launch {
            vm.isTyping.collect { binding.tvTyping.visibility = if (it) android.view.View.VISIBLE else android.view.View.GONE }
        }
        lifecycleScope.launch {
            vm.isListening.collect { listening ->
                binding.btnVoice.setImageResource(if (listening) android.R.drawable.ic_btn_speak_now else android.R.drawable.ic_lock_silent_mode_off)
            }
        }

        binding.btnSend.setOnClickListener { sendMsg() }
        binding.etInput.setOnEditorActionListener { _, action, _ -> if (action == EditorInfo.IME_ACTION_SEND) { sendMsg(); true } else false }
        binding.btnVoice.setOnClickListener { vm.toggleVoice() }
        binding.btnSpeakLast.setOnClickListener { vm.speakLast() }
        binding.btnClear.setOnClickListener { vm.clearHistory() }
        vm.loadHistory()
    }

    private fun sendMsg() {
        val text = binding.etInput.text.toString().trim()
        if (text.isNotEmpty()) { vm.sendMessage(text); binding.etInput.text?.clear() }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
