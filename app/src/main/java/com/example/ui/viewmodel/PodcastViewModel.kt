package com.example.ui.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.DialoguePart
import com.example.data.api.GeminiService
import com.example.data.api.PodcastGenerationResult
import com.example.data.database.PodcastDatabase
import com.example.data.database.PodcastEpisode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Locale
import java.util.UUID

class PodcastViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = PodcastDatabase.getDatabase(application)
    private val dao = db.podcastDao()
    private val geminiService = GeminiService()

    // Database flow of all episodes
    val allEpisodes: StateFlow<List<PodcastEpisode>> = dao.getAllEpisodes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Database flow of favorite episodes
    val favoriteEpisodes: StateFlow<List<PodcastEpisode>> = dao.getFavoriteEpisodes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI state flows
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _currentEpisode = MutableStateFlow<PodcastEpisode?>(null)
    val currentEpisode: StateFlow<PodcastEpisode?> = _currentEpisode.asStateFlow()

    private val _activeDialogueIndex = MutableStateFlow(-1)
    val activeDialogueIndex: StateFlow<Int> = _activeDialogueIndex.asStateFlow()

    private val _activeHost = MutableStateFlow("Cosmic Gemini")
    val activeHost: StateFlow<String> = _activeHost.asStateFlow()

    // Generation state flow
    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    // Real-time audio amplitude mock data for visualizer (0.0 to 1.0f)
    private val _visualizerAmplitude = MutableStateFlow(0.1f)
    val visualizerAmplitude: StateFlow<Float> = _visualizerAmplitude.asStateFlow()

    // Audio engines
    private var mediaPlayer: MediaPlayer? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    // Dialogue parts for user-generated scripts
    private var currentDialogueParts = listOf<DialoguePart>()

    // Handler to poll seekbar progress and update amplitude wave
    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    _currentTime.value = player.currentPosition.toLong()
                    // Feed visualizer based on mock dynamic variations around player status
                    _visualizerAmplitude.value = (0.2f + (0.6f * Math.random().toFloat()))
                    progressHandler.postDelayed(this, 100)
                }
            }
        }
    }

    // Custom TTS Dialogue player loop
    private var ttsDialogueJob = false
    private val ttsHandler = Handler(Looper.getMainLooper())

    init {
        // Initialize TTS
        tts = TextToSpeech(application, this)
        seedInitialEpisodes()
    }

    private fun seedInitialEpisodes() {
        viewModelScope.launch {
            // Check if episodes already exist, if empty, seed them
            val currentList = dao.getEpisodeById("seed_1")
            if (currentList == null) {
                val seed1 = PodcastEpisode(
                    id = "seed_1",
                    title = "Quantum Echoes: Decoding the Void",
                    hostName = "Cosmic Gemini",
                    topic = "Quantum Information & Reality Structure",
                    description = "Delve deep into the quantum sub-structure of our physical reality and explore whether the cosmos operates like a vast, interactive data processor.",
                    duration = "5:00",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    isFavorite = false,
                    isUserGenerated = false
                )
                val seed2 = PodcastEpisode(
                    id = "seed_2",
                    title = "Neon Synths & Digital Hearts",
                    hostName = "DJ Nebula",
                    topic = "Cybernetic Aesthetics",
                    description = "How synthesized audio frequencies, nostalgic visual tones, and generative algorithms shape humanity's modern cybernetic identities.",
                    duration = "6:00",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    isFavorite = true,
                    isUserGenerated = false
                )
                val seed3 = PodcastEpisode(
                    id = "seed_3",
                    title = "The Cipher of Oak Island",
                    hostName = "Detective Cipher",
                    topic = "Cryptographic Anomalies",
                    description = "An analytical investigation tracing ancient physical manuscripts, mathematical runic carvings, and deep cryptographic secrets.",
                    duration = "5:30",
                    audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                    isFavorite = false,
                    isUserGenerated = false
                )

                dao.insertEpisode(seed1)
                dao.insertEpisode(seed2)
                dao.insertEpisode(seed3)

                // Select the first episode by default
                _currentEpisode.value = seed1
            } else {
                // If already seeded, make sure we select one by default
                dao.getAllEpisodes().collect { episodes ->
                    if (episodes.isNotEmpty() && _currentEpisode.value == null) {
                        _currentEpisode.value = episodes.first()
                    }
                }
            }
        }
    }

    // TTS init callback
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("PodcastViewModel", "TTS language not supported.")
            } else {
                isTtsInitialized = true
            }
        } else {
            Log.e("PodcastViewModel", "TTS Initialization failed.")
        }
    }

    fun selectEpisode(episode: PodcastEpisode) {
        stopPlayback()
        _currentEpisode.value = episode
        _activeHost.value = episode.hostName
        _currentTime.value = 0L
        _activeDialogueIndex.value = -1

        if (episode.isUserGenerated) {
            parseEpisodeScript(episode)
        } else {
            // For standard streaming podcast, prepare the duration (mocked initially, sets properly on load)
            val minSec = episode.duration.split(":")
            if (minSec.size == 2) {
                val mins = minSec[0].toLongOrNull() ?: 0L
                val secs = minSec[1].toLongOrNull() ?: 0L
                _totalDuration.value = (mins * 60 + secs) * 1000
            }
        }
    }

    private fun parseEpisodeScript(episode: PodcastEpisode) {
        try {
            val array = JSONArray(episode.scriptJson)
            val list = mutableListOf<DialoguePart>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DialoguePart(
                        speaker = obj.getString("speaker"),
                        text = obj.getString("text")
                    )
                )
            }
            currentDialogueParts = list
            _totalDuration.value = (list.size * 5000L) // Estimate 5s per dialogue part
        } catch (e: Exception) {
            Log.e("PodcastViewModel", "Failed to parse script JSON for episode ${episode.title}", e)
        }
    }

    fun togglePlayPause() {
        val episode = _currentEpisode.value ?: return

        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback(episode)
        }
    }

    private fun startPlayback(episode: PodcastEpisode) {
        if (episode.isUserGenerated) {
            // User generated script playback via TTS
            _isPlaying.value = true
            ttsDialogueJob = true
            playNextDialoguePart()
        } else {
            // Stream audio from URL
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(episode.audioUrl)
                        setOnPreparedListener { mp ->
                            _totalDuration.value = mp.duration.toLong()
                            mp.start()
                            _isPlaying.value = true
                            progressHandler.post(progressRunnable)
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                        setOnErrorListener { _, what, extra ->
                            Log.e("PodcastViewModel", "MediaPlayer error: $what, $extra")
                            stopPlayback()
                            true
                        }
                        prepareAsync()
                    }
                } else {
                    mediaPlayer?.start()
                    _isPlaying.value = true
                    progressHandler.post(progressRunnable)
                }
            } catch (e: Exception) {
                Log.e("PodcastViewModel", "Failed to start media player", e)
                _isPlaying.value = false
            }
        }
    }

    private fun pausePlayback() {
        _isPlaying.value = false
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer?.pause()
        }
        progressHandler.removeCallbacks(progressRunnable)

        // For TTS loop
        ttsDialogueJob = false
        tts?.stop()
        ttsHandler.removeCallbacksAndMessages(null)
    }

    fun stopPlayback() {
        _isPlaying.value = false
        _currentTime.value = 0L
        _visualizerAmplitude.value = 0.1f

        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        progressHandler.removeCallbacks(progressRunnable)

        // TTS Cleanup
        ttsDialogueJob = false
        tts?.stop()
        ttsHandler.removeCallbacksAndMessages(null)
        _activeDialogueIndex.value = -1
    }

    private fun playNextDialoguePart() {
        if (!ttsDialogueJob || currentDialogueParts.isEmpty()) return

        val nextIndex = _activeDialogueIndex.value + 1
        if (nextIndex >= currentDialogueParts.size) {
            // Finished script playback
            stopPlayback()
            return
        }

        _activeDialogueIndex.value = nextIndex
        _currentTime.value = nextIndex * 5000L
        val part = currentDialogueParts[nextIndex]

        // Custom voice parameters per host
        if (isTtsInitialized) {
            if (part.speaker == "Cosmic Gemini") {
                _activeHost.value = "Cosmic Gemini"
                tts?.setPitch(0.80f) // Deep, rich tone
                tts?.setSpeechRate(0.95f)
            } else {
                _activeHost.value = "DJ Nebula"
                tts?.setPitch(1.20f) // High-energy, upbeat co-host tone
                tts?.setSpeechRate(1.05f)
            }

            tts?.speak(part.text, TextToSpeech.QUEUE_FLUSH, null, "dialogue_${nextIndex}")
        }

        // Animate the visualizer amplitude while TTS is playing
        val utteranceDuration = 5500L // 5.5 seconds per line
        var msElapsed = 0L
        val amplitudeRunnable = object : Runnable {
            override fun run() {
                if (ttsDialogueJob && _activeDialogueIndex.value == nextIndex) {
                    _visualizerAmplitude.value = (0.25f + (0.7f * Math.random().toFloat()))
                    if (msElapsed < utteranceDuration) {
                        msElapsed += 150
                        ttsHandler.postDelayed(this, 150)
                    }
                }
            }
        }
        ttsHandler.post(amplitudeRunnable)

        // Queue next dialogue part
        ttsHandler.postDelayed({
            if (ttsDialogueJob) {
                playNextDialoguePart()
            }
        }, utteranceDuration)
    }

    fun seekTo(progress: Long) {
        val episode = _currentEpisode.value ?: return
        if (episode.isUserGenerated) {
            // Seek to appropriate dialogue line
            if (currentDialogueParts.isNotEmpty()) {
                val lineIndex = (progress / 5000L).toInt().coerceIn(0, currentDialogueParts.size - 1)
                _activeDialogueIndex.value = lineIndex - 1 // Will trigger playNext on next step
                _currentTime.value = progress
                if (_isPlaying.value) {
                    ttsHandler.removeCallbacksAndMessages(null)
                    playNextDialoguePart()
                }
            }
        } else {
            mediaPlayer?.let { player ->
                player.seekTo(progress.toInt())
                _currentTime.value = progress
            }
        }
    }

    fun changePlaybackSpeed() {
        val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f)
        val currentIndex = speeds.indexOf(_playbackSpeed.value)
        val nextIndex = (currentIndex + 1) % speeds.size
        val newSpeed = speeds[nextIndex]
        _playbackSpeed.value = newSpeed

        // MediaPlayer speed adjustment (Android 6.0+)
        mediaPlayer?.let { player ->
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    player.playbackParams = player.playbackParams.setSpeed(newSpeed)
                }
            } catch (e: Exception) {
                Log.e("PodcastViewModel", "Failed to set playback speed", e)
            }
        }
    }

    fun toggleFavorite(episode: PodcastEpisode) {
        viewModelScope.launch {
            val newFav = !episode.isFavorite
            dao.updateFavoriteStatus(episode.id, newFav)
            if (_currentEpisode.value?.id == episode.id) {
                _currentEpisode.value = _currentEpisode.value?.copy(isFavorite = newFav)
            }
        }
    }

    fun generateNewEpisode(topic: String) {
        if (topic.trim().isEmpty()) return

        _generationState.value = GenerationUiState.Generating

        viewModelScope.launch {
            when (val result = geminiService.generatePodcastScript(topic)) {
                is PodcastGenerationResult.Success -> {
                    // Create local episode
                    val dialogueJsonArray = JSONArray()
                    result.dialogue.forEach { part ->
                        dialogueJsonArray.put(
                            org.json.JSONObject().apply {
                                put("speaker", part.speaker)
                                put("text", part.text)
                            }
                        )
                    }

                    val newEp = PodcastEpisode(
                        id = UUID.randomUUID().toString(),
                        title = result.title,
                        hostName = "Cosmic Gemini",
                        topic = topic,
                        description = result.description,
                        duration = "${(result.dialogue.size * 5) / 60}:${String.format("%02d", (result.dialogue.size * 5) % 60)}",
                        audioUrl = "", // Empty indicates user-generated Text-To-Speech
                        isFavorite = false,
                        isUserGenerated = true,
                        scriptJson = dialogueJsonArray.toString()
                    )

                    dao.insertEpisode(newEp)
                    selectEpisode(newEp)
                    _generationState.value = GenerationUiState.Success(newEp)
                }
                is PodcastGenerationResult.Error -> {
                    _generationState.value = GenerationUiState.Error(result.message)
                }
            }
        }
    }

    fun clearGenerationState() {
        _generationState.value = GenerationUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
        tts?.shutdown()
    }
}

sealed class GenerationUiState {
    object Idle : GenerationUiState()
    object Generating : GenerationUiState()
    data class Success(val episode: PodcastEpisode) : GenerationUiState()
    data class Error(val message: String) : GenerationUiState()
}
