package com.example.ui.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onTranscriptResult: (String) -> Unit,
    private val onPartialTranscript: (String) -> Unit,
    private val onSpeechRecognizerError: (String) -> Unit,
    private val onStatusChange: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false
    private var isListening = false

    // TTS Parameters
    private var speed = 1.0f
    private var pitch = 1.0f
    private var activeProfile = "Professional"

    init {
        // Initialize TextToSpeech
        tts = TextToSpeech(context, this)

        // Initialize SpeechRecognizer
        initializeSpeechRecognizer()
    }

    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onStatusChange("Listening...")
                    }

                    override fun onBeginningOfSpeech() {
                        onStatusChange("Recording speech...")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Can be used for waveform visualization
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        onStatusChange("Processing...")
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server-side error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input timeout"
                            else -> "Unknown recognition error"
                        }
                        Log.e("VoiceManager", "Speech recognition error: $message")
                        isListening = false
                        onSpeechRecognizerError(message)
                        onStatusChange("Idle")
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val resultText = matches[0]
                            onTranscriptResult(resultText)
                        }
                        isListening = false
                        onStatusChange("Idle")
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onPartialTranscript(matches[0])
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            } catch (e: Exception) {
                Log.e("VoiceManager", "Failed to create SpeechRecognizer: ${e.message}")
            }
        } else {
            onSpeechRecognizerError("Speech recognition not available on this device")
        }
    }

    // Text To Speech Callbacks
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("VoiceManager", "US English is not supported for TTS")
            } else {
                isTtsInitialized = true
                applyTtsSettings()
            }
        } else {
            Log.e("VoiceManager", "Failed to initialize TextToSpeech")
        }
    }

    fun setSpeechSettings(speed: Float, pitch: Float, profile: String) {
        this.speed = speed
        this.pitch = pitch
        this.activeProfile = profile
        applyTtsSettings()
    }

    private fun applyTtsSettings() {
        if (!isTtsInitialized) return
        
        // Apply speaking speed
        tts?.setSpeechRate(speed)

        // Apply pitch based on profile + pitch factor
        // Professional uses lower pitch, friendly uses higher, neutral uses standard
        val basePitch = when (activeProfile) {
            "Professional" -> 0.85f
            "Friendly" -> 1.15f
            else -> 1.0f // Neutral
        }
        val finalPitch = basePitch * pitch
        tts?.setPitch(finalPitch)
    }

    // Speak out loud in real time
    fun speak(text: String) {
        if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId_${System.currentTimeMillis()}")
        } else {
            Log.e("VoiceManager", "TTS not initialized yet")
        }
    }

    // Start Real Time Listening (Speech To Text)
    fun startListening() {
        if (isListening) return
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            onStatusChange("Listening...")
        } catch (e: Exception) {
            onSpeechRecognizerError("Failed to start speech recognizer: ${e.message}")
            Log.e("VoiceManager", "Error starting listening: ${e.message}")
        }
    }

    // Stop Listening
    fun stopListening() {
        if (!isListening) return
        try {
            speechRecognizer?.stopListening()
            isListening = false
            onStatusChange("Idle")
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error stopping listening: ${e.message}")
        }
    }

    // Release resources
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error during shutdown: ${e.message}")
        }
    }
}
