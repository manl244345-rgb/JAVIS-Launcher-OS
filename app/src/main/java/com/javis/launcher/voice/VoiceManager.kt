package com.javis.launcher.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

enum class VoiceState { IDLE, LISTENING, SPEAKING, ERROR }

class VoiceManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var stt: SpeechRecognizer? = null
    private var ttsReady = false
    private var currentState = VoiceState.IDLE
    private var onSpeakDone: (() -> Unit)? = null

    init {
        initTTS()
    }

    private fun initTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true
                tts?.language = Locale.US
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(0.82f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { currentState = VoiceState.SPEAKING }
                    override fun onDone(utteranceId: String?) { currentState = VoiceState.IDLE; onSpeakDone?.invoke(); onSpeakDone = null }
                    override fun onError(utteranceId: String?) { currentState = VoiceState.IDLE }
                })
            }
        }
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!ttsReady) { onDone?.invoke(); return }
        onSpeakDone = onDone
        tts?.stop()
        val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "javis_${System.currentTimeMillis()}") }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "javis_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() { tts?.stop(); currentState = VoiceState.IDLE }

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) { onError("Speech recognition not available"); return }
        stt?.cancel()
        stt?.destroy()
        stt = SpeechRecognizer.createSpeechRecognizer(context)
        stt?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) { currentState = VoiceState.LISTENING }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(code: Int) {
                currentState = VoiceState.IDLE
                val msg = when (code) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that. Please try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything. Try speaking again."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error. Check your connection."
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error. Mic may be in use."
                    else -> "Recognition error ($code). Please retry."
                }
                onError(msg)
            }
            override fun onResults(results: Bundle?) {
                currentState = VoiceState.IDLE
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: return onError("No speech detected.")
                onResult(text)
            }
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(t: Int, p: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        stt?.startListening(intent)
    }

    fun stopListening() { stt?.cancel(); currentState = VoiceState.IDLE }
    fun getState() = currentState
    fun isReady() = ttsReady

    fun destroy() { tts?.shutdown(); stt?.destroy() }
}
