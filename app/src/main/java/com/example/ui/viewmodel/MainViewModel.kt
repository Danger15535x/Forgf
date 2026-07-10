package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CallSession
import com.example.data.model.TranscriptMessage
import com.example.data.repository.CallSessionRepository
import com.example.data.api.GeminiClient
import com.example.ui.voice.VoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceContact(val name: String, val phoneNumber: String)

enum class AppScreen {
    ONBOARDING,
    LIVE,
    HISTORY,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CallSessionRepository
    private var voiceManager: VoiceManager? = null

    // Screen navigation
    private val _currentScreen = MutableStateFlow(AppScreen.ONBOARDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Permissions State
    private val _micPermissionGranted = MutableStateFlow(false)
    val micPermissionGranted: StateFlow<Boolean> = _micPermissionGranted.asStateFlow()

    private val _notifyPermissionGranted = MutableStateFlow(false)
    val notifyPermissionGranted: StateFlow<Boolean> = _notifyPermissionGranted.asStateFlow()

    private val _contactsPermissionGranted = MutableStateFlow(false)
    val contactsPermissionGranted: StateFlow<Boolean> = _contactsPermissionGranted.asStateFlow()

    // Device Contacts
    private val _deviceContacts = MutableStateFlow<List<DeviceContact>>(emptyList())
    val deviceContacts: StateFlow<List<DeviceContact>> = _deviceContacts.asStateFlow()

    // Voice Engine Settings
    private val _speakingSpeed = MutableStateFlow(1.0f)
    val speakingSpeed: StateFlow<Float> = _speakingSpeed.asStateFlow()

    private val _pitchVariance = MutableStateFlow(1.0f)
    val pitchVariance: StateFlow<Float> = _pitchVariance.asStateFlow()

    private val _activeVoiceProfile = MutableStateFlow("Professional")
    val activeVoiceProfile: StateFlow<String> = _activeVoiceProfile.asStateFlow()

    private val _autoRecord = MutableStateFlow(true)
    val autoRecord: StateFlow<Boolean> = _autoRecord.asStateFlow()

    // Database / Call History List
    private val _callHistory = MutableStateFlow<List<CallSession>>(emptyList())
    val callHistory: StateFlow<List<CallSession>> = _callHistory.asStateFlow()

    // Active Call State
    private val _isCallActive = MutableStateFlow(false)
    val isCallActive: StateFlow<Boolean> = _isCallActive.asStateFlow()

    private val _currentPartnerName = MutableStateFlow("Jordan Sterling")
    val currentPartnerName: StateFlow<String> = _currentPartnerName.asStateFlow()

    private val _liveTranscript = MutableStateFlow<List<TranscriptMessage>>(emptyList())
    val liveTranscript: StateFlow<List<TranscriptMessage>> = _liveTranscript.asStateFlow()

    private val _suggestedReplies = MutableStateFlow<List<String>>(
        listOf(
            "Hello, let's get started.",
            "I'm checking the files right now.",
            "Can you please explain that detail?"
        )
    )
    val suggestedReplies: StateFlow<List<String>> = _suggestedReplies.asStateFlow()

    private val _isGeneratingSuggestions = MutableStateFlow(false)
    val isGeneratingSuggestions: StateFlow<Boolean> = _isGeneratingSuggestions.asStateFlow()

    private val _sttStatus = MutableStateFlow("Idle")
    val sttStatus: StateFlow<String> = _sttStatus.asStateFlow()

    private val _sttError = MutableStateFlow<String?>(null)
    val sttError: StateFlow<String?> = _sttError.asStateFlow()

    private val _callTimerSeconds = MutableStateFlow(0)
    val callTimerSeconds: StateFlow<Int> = _callTimerSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var suggestionsJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CallSessionRepository(database.callSessionDao())

        // Load History
        viewModelScope.launch {
            repository.allSessions.collectLatest { sessions ->
                _callHistory.value = sessions
                // If there's already history, we skip onboarding and go straight to History screen!
                if (sessions.isNotEmpty() && _currentScreen.value == AppScreen.ONBOARDING) {
                    _currentScreen.value = AppScreen.HISTORY
                }
            }
        }

        // Check contacts permission on startup
        val hasContactsPerm = androidx.core.content.ContextCompat.checkSelfPermission(
            application,
            android.Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        _contactsPermissionGranted.value = hasContactsPerm
        if (hasContactsPerm) {
            fetchContacts()
        }

        // Initialize voice manager
        initializeVoiceManager()
    }

    private fun initializeVoiceManager() {
        voiceManager = VoiceManager(
            context = getApplication(),
            onTranscriptResult = { recognizedText ->
                addPartnerTranscript(recognizedText)
            },
            onPartialTranscript = { partialText ->
                _sttStatus.value = "Recognizing: $partialText"
            },
            onSpeechRecognizerError = { errorMessage ->
                _sttError.value = errorMessage
            },
            onStatusChange = { status ->
                _sttStatus.value = status
            }
        )
        applyVoiceSettings()
    }

    private fun applyVoiceSettings() {
        voiceManager?.setSpeechSettings(
            speed = _speakingSpeed.value,
            pitch = _pitchVariance.value,
            profile = _activeVoiceProfile.value
        )
    }

    // Navigation triggers
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun grantMicPermission(granted: Boolean) {
        _micPermissionGranted.value = granted
    }

    fun grantNotifyPermission(granted: Boolean) {
        _notifyPermissionGranted.value = granted
    }

    fun updateContactsPermission(granted: Boolean) {
        _contactsPermissionGranted.value = granted
        if (granted) {
            fetchContacts()
        }
    }

    fun fetchContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contactsList = mutableListOf<DeviceContact>()
            val context = getApplication<Application>()
            
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_CONTACTS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return@launch
            }

            val contentResolver = context.contentResolver
            try {
                val cursor = contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val nameIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        if (nameIndex >= 0 && numberIndex >= 0) {
                            val name = it.getString(nameIndex) ?: ""
                            val number = it.getString(numberIndex) ?: ""
                            if (name.isNotBlank()) {
                                contactsList.add(DeviceContact(name, number))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _deviceContacts.value = contactsList.distinctBy { it.name }
        }
    }

    // Update settings parameters
    fun updateSettings(speed: Float, pitch: Float, profile: String, autoRecordVal: Boolean) {
        _speakingSpeed.value = speed
        _pitchVariance.value = pitch
        _activeVoiceProfile.value = profile
        _autoRecord.value = autoRecordVal
        applyVoiceSettings()
    }

    // Text to Speech: User speaks out loud by typing text
    fun userSpeakText(text: String) {
        if (text.isBlank()) return

        // 1. Speak out loud
        voiceManager?.speak(text)

        // 2. Add to transcript
        val newMessage = TranscriptMessage(sender = "You", text = text)
        _liveTranscript.value = _liveTranscript.value + newMessage

        // 3. Update Gemini recommendations
        fetchGeminiSuggestions()
    }

    // Speech to Text: Partner speaks (either simulated or recognized from microphone)
    fun addPartnerTranscript(text: String) {
        if (text.isBlank()) return
        val newMessage = TranscriptMessage(sender = _currentPartnerName.value, text = text)
        _liveTranscript.value = _liveTranscript.value + newMessage

        // Update Gemini recommendations
        fetchGeminiSuggestions()
    }

    // Active Call controls
    fun startCall(partnerName: String) {
        _currentPartnerName.value = partnerName.ifBlank { "Unknown Contact" }
        _liveTranscript.value = emptyList()
        _isCallActive.value = true
        _sttError.value = null
        _callTimerSeconds.value = 0
        navigateTo(AppScreen.LIVE)

        // Reset suggestions
        _suggestedReplies.value = listOf(
            "Hello Jordan, I can hear you clearly.",
            "Can you tell me about the Singapore terminal delay?",
            "Let's check the shipment records."
        )

        // Start Speech recognizer if autoRecord is active
        if (_autoRecord.value) {
            voiceManager?.startListening()
        }

        // Start call timer
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isCallActive.value) {
                delay(1000)
                _callTimerSeconds.value += 1
            }
        }
    }

    fun endCall() {
        if (!_isCallActive.value) return

        // Stop listener
        voiceManager?.stopListening()
        _isCallActive.value = false
        timerJob?.cancel()

        // Save session to Room Database if there are messages
        val currentTranscript = _liveTranscript.value
        if (currentTranscript.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                // Serialize list to JSON using Moshi Converters
                val converters = com.example.data.db.Converters()
                val jsonString = converters.fromList(currentTranscript)

                val session = CallSession(
                    contactName = _currentPartnerName.value,
                    durationSeconds = _callTimerSeconds.value,
                    transcriptJson = jsonString
                )
                repository.insertSession(session)
            }
        }

        navigateTo(AppScreen.HISTORY)
    }

    // AI suggestions using Gemini API
    private fun fetchGeminiSuggestions() {
        suggestionsJob?.cancel()
        val currentMessages = _liveTranscript.value
        if (currentMessages.isEmpty()) return

        suggestionsJob = viewModelScope.launch {
            _isGeneratingSuggestions.value = true
            // Wait 1.5 seconds to gather full input turns and prevent heavy API throttling
            delay(1500)

            // Compile transcripts as a string block
            val conversationBlock = currentMessages.joinToString("\n") {
                "${it.sender}: ${it.text}"
            }

            val suggestions = GeminiClient.getSuggestedReplies(conversationBlock)
            if (suggestions.isNotEmpty()) {
                _suggestedReplies.value = suggestions
            }
            _isGeneratingSuggestions.value = false
        }
    }

    // Delete historical session
    fun deleteSession(session: CallSession) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSessionById(session.id)
        }
    }

    // Populate with dummy call histories to provide a rich starting experience if database is empty!
    fun loadSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            val converters = com.example.data.db.Converters()
            
            val sample1 = CallSession(
                contactName = "Jordan Sterling",
                timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
                durationSeconds = 765, // 12m 45s
                transcriptJson = converters.fromList(listOf(
                    TranscriptMessage("Jordan Sterling", "Hello, can we review the layout today?"),
                    TranscriptMessage("You", "Yes, I am looking at the glassmorphic card treatment now."),
                    TranscriptMessage("Jordan Sterling", "Excellent, we should finalize the architectural review by Thursday so the development team can start the sprint on Monday morning.")
                ))
            )

            val sample2 = CallSession(
                contactName = "+1 (555) 012-9938",
                timestamp = System.currentTimeMillis() - 86400000 * 2, // 2 days ago
                durationSeconds = 252, // 4m 12s
                transcriptJson = converters.fromList(listOf(
                    TranscriptMessage("+1 (555) 012-9938", "Your package delivery has been rescheduled."),
                    TranscriptMessage("You", "What is the new time slot?"),
                    TranscriptMessage("+1 (555) 012-9938", "The delivery of your package has been rescheduled to tomorrow between 2 PM and 5 PM. Please ensure someone is available.")
                ))
            )

            val sample3 = CallSession(
                contactName = "Sarah Chen",
                timestamp = System.currentTimeMillis() - 86400000 * 3, // 3 days ago
                durationSeconds = 1930, // 32m 10s
                transcriptJson = converters.fromList(listOf(
                    TranscriptMessage("Sarah Chen", "Hi! I just sent you the link to the dashboard prototyping workspace."),
                    TranscriptMessage("You", "Excellent, checking it out."),
                    TranscriptMessage("Sarah Chen", "I've shared the Figma links for the new dashboard prototype. Let me know what you think about the glassmorphic card treatment...")
                ))
            )

            repository.insertSession(sample1)
            repository.insertSession(sample2)
            repository.insertSession(sample3)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager?.shutdown()
        timerJob?.cancel()
        suggestionsJob?.cancel()
    }
}
