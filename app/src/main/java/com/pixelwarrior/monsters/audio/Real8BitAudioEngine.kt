package com.pixelwarrior.monsters.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Real 8-Bit Audio Synthesis Engine - Phase 4 Implementation
 * Generates actual 8-bit audio waveforms using AudioTrack for authentic chiptune sound
 */
class Real8BitAudioEngine(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentAudioTrack: AudioTrack? = null
    private var isPlaying = false
    
    // Audio configuration for 8-bit style
    private val sampleRate = 22050 // Lower sample rate for authentic 8-bit feel
    private val channels = AudioFormat.CHANNEL_OUT_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = try {
        AudioTrack.getMinBufferSize(sampleRate, channels, encoding) * 2
    } catch (e: RuntimeException) {
        // In unit tests, Android framework is not available
        4096 // Default buffer size for tests
    }
    
    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()
    
    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    /**
     * Play a single 8-bit note with specified waveform
     */
    suspend fun playChipNote(
        frequency: Double, 
        durationMs: Int, 
        waveform: ChipWaveform, 
        volume: Float = _volume.value
    ) = withContext(Dispatchers.IO) {
        
        if (!_isEnabled.value) return@withContext
        
        try {
            val samples = generateWaveform(frequency, durationMs, waveform, volume)
            val audioTrack = createAudioTrack()
            
            audioTrack.play()
            audioTrack.write(samples, 0, samples.size)
            
            // Wait for playback to complete
            delay(durationMs.toLong())
            
            audioTrack.stop()
            audioTrack.release()
            
        } catch (e: Exception) {
            // Fallback to silence on audio errors
            delay(durationMs.toLong())
        }
    }
    
    /**
     * Play a sequence of notes for melodies
     */
    suspend fun playMelody(notes: List<ChipNote>, loop: Boolean = false) = withContext(Dispatchers.IO) {
        
        if (!_isEnabled.value) return@withContext
        
        do {
            for (note in notes) {
                if (!_isEnabled.value) break
                
                if (note.frequency > 0) {
                    playChipNote(note.frequency, note.duration, note.waveform, note.volume)
                } else {
                    // Rest note (silence)
                    delay(note.duration.toLong())
                }
            }
            
            if (loop && _isEnabled.value) {
                delay(200) // Brief pause between loops
            }
        } while (loop && _isEnabled.value)
    }
    
    /**
     * Generate 8-bit style monster cry based on species characteristics
     */
    suspend fun playMonsterCry(monsterSpecies: String, volume: Float = _volume.value) {
        
        val cryPattern = generateMonsterCryPattern(monsterSpecies)
        
        for (segment in cryPattern) {
            playChipNote(segment.frequency, segment.duration, segment.waveform, volume * 0.8f)
            delay(50) // Brief gap between segments
        }
    }
    
    /**
     * Generate procedural background music for different contexts
     */
    suspend fun playBackgroundMusic(context: MusicContext, loop: Boolean = true) {
        
        val melody = generateContextualMelody(context)
        playMelody(melody, loop)
    }
    
    /**
     * Generate 8-bit waveform samples
     */
    private fun generateWaveform(
        frequency: Double, 
        durationMs: Int, 
        waveform: ChipWaveform, 
        volume: Float
    ): ShortArray {
        
        val samples = (sampleRate * durationMs / 1000.0).toInt()
        val buffer = ShortArray(samples)
        val amplitude = (Short.MAX_VALUE * volume * 0.3).toInt() // Reduced amplitude for 8-bit feel
        
        for (i in 0 until samples) {
            val time = i.toDouble() / sampleRate
            val phase = 2 * PI * frequency * time
            
            val sample = when (waveform) {
                ChipWaveform.SQUARE -> {
                    if (sin(phase) >= 0) amplitude else -amplitude
                }
                ChipWaveform.TRIANGLE -> {
                    val trianglePhase = phase % (2 * PI)
                    when {
                        trianglePhase < PI / 2 -> (amplitude * 2 * trianglePhase / PI).toInt()
                        trianglePhase < 3 * PI / 2 -> (amplitude * (2 - 2 * trianglePhase / PI)).toInt()
                        else -> (amplitude * (2 * trianglePhase / PI - 4)).toInt()
                    }
                }
                ChipWaveform.SAWTOOTH -> {
                    val sawPhase = phase % (2 * PI)
                    (amplitude * (2 * sawPhase / (2 * PI) - 1)).toInt()
                }
                ChipWaveform.SINE -> {
                    (amplitude * sin(phase)).toInt()
                }
                ChipWaveform.PULSE -> {
                    val pulseWidth = 0.25 // 25% duty cycle for authentic 8-bit pulse
                    val pulsePhase = phase % (2 * PI)
                    if (pulsePhase < 2 * PI * pulseWidth) amplitude else -amplitude
                }
                ChipWaveform.NOISE -> {
                    // Simple noise generation
                    (amplitude * (Math.random() * 2 - 1)).toInt()
                }
            }
            
            // Apply simple low-pass filter for 8-bit feel
            buffer[i] = (sample.toDouble() * 0.8 + (if (i > 0) buffer[i-1].toDouble() * 0.2 else 0.0)).toInt().toShort()
        }
        
        // Apply fade-out to prevent clicking
        val fadeOutSamples = minOf(samples / 10, 1000)
        for (i in (samples - fadeOutSamples) until samples) {
            val fade = (samples - i).toFloat() / fadeOutSamples
            buffer[i] = (buffer[i].toInt() * fade).toInt().toShort()
        }
        
        return buffer
    }
    
    /**
     * Create and configure AudioTrack for 8-bit audio
     */
    private fun createAudioTrack(): AudioTrack {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        return AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(encoding)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channels)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }
    
    /**
     * Generate monster cry pattern based on species characteristics
     */
    private fun generateMonsterCryPattern(species: String): List<ChipNote> {
        return when (species.lowercase()) {
            "dragon", "fire_drake" -> listOf(
                ChipNote(220.0, 300, ChipWaveform.SAWTOOTH, 0.8f),
                ChipNote(180.0, 200, ChipWaveform.SQUARE, 0.6f),
                ChipNote(260.0, 250, ChipWaveform.PULSE, 0.7f)
            )
            "slime", "water_spirit" -> listOf(
                ChipNote(150.0, 200, ChipWaveform.TRIANGLE, 0.5f),
                ChipNote(180.0, 150, ChipWaveform.SINE, 0.4f),
                ChipNote(120.0, 300, ChipWaveform.TRIANGLE, 0.6f)
            )
            "beast", "forest_wolf" -> listOf(
                ChipNote(300.0, 150, ChipWaveform.SAWTOOTH, 0.7f),
                ChipNote(200.0, 100, ChipWaveform.NOISE, 0.3f),
                ChipNote(400.0, 200, ChipWaveform.SQUARE, 0.8f)
            )
            "bird", "sky_hawk" -> listOf(
                ChipNote(600.0, 100, ChipWaveform.SQUARE, 0.6f),
                ChipNote(800.0, 80, ChipWaveform.TRIANGLE, 0.5f),
                ChipNote(500.0, 120, ChipWaveform.PULSE, 0.7f)
            )
            "insect", "giant_mantis" -> listOf(
                ChipNote(800.0, 50, ChipWaveform.PULSE, 0.4f),
                ChipNote(1000.0, 30, ChipWaveform.NOISE, 0.3f),
                ChipNote(600.0, 80, ChipWaveform.SQUARE, 0.5f)
            )
            else -> listOf(
                // Generic cry pattern
                ChipNote(250.0, 200, ChipWaveform.TRIANGLE, 0.6f),
                ChipNote(200.0, 150, ChipWaveform.SQUARE, 0.5f)
            )
        }
    }
    
    /**
     * Generate contextual background melodies
     */
    private fun generateContextualMelody(context: MusicContext): List<ChipNote> {
        return when (context) {
            MusicContext.BATTLE -> generateBattleMusic()
            MusicContext.EXPLORATION -> generateExplorationMusic()
            MusicContext.HUB_WORLD -> generateHubMusic()
            MusicContext.SYNTHESIS_LAB -> generateSynthesisMusic()
            MusicContext.VICTORY -> generateVictoryMusic()
            MusicContext.DEFEAT -> generateDefeatMusic()
        }
    }
    
    private fun generateBattleMusic(): List<ChipNote> {
        return listOf(
            // Fast-paced battle theme in 8-bit style
            ChipNote(440.0, 200, ChipWaveform.SQUARE, 0.7f),
            ChipNote(523.0, 200, ChipWaveform.SQUARE, 0.7f),
            ChipNote(659.0, 200, ChipWaveform.SQUARE, 0.7f),
            ChipNote(784.0, 200, ChipWaveform.SQUARE, 0.7f),
            ChipNote(659.0, 200, ChipWaveform.TRIANGLE, 0.6f),
            ChipNote(523.0, 200, ChipWaveform.TRIANGLE, 0.6f),
            ChipNote(440.0, 400, ChipWaveform.SQUARE, 0.8f),
            ChipNote(0.0, 100, ChipWaveform.SQUARE, 0.0f), // Rest
            ChipNote(392.0, 200, ChipWaveform.PULSE, 0.6f),
            ChipNote(440.0, 200, ChipWaveform.PULSE, 0.6f),
            ChipNote(523.0, 400, ChipWaveform.SAWTOOTH, 0.7f)
        )
    }
    
    private fun generateExplorationMusic(): List<ChipNote> {
        return listOf(
            // Gentle exploration melody
            ChipNote(330.0, 400, ChipWaveform.TRIANGLE, 0.5f),
            ChipNote(370.0, 400, ChipWaveform.TRIANGLE, 0.5f),
            ChipNote(415.0, 400, ChipWaveform.TRIANGLE, 0.5f),
            ChipNote(370.0, 400, ChipWaveform.SINE, 0.4f),
            ChipNote(330.0, 800, ChipWaveform.TRIANGLE, 0.5f),
            ChipNote(0.0, 200, ChipWaveform.SQUARE, 0.0f), // Rest
            ChipNote(294.0, 400, ChipWaveform.TRIANGLE, 0.4f),
            ChipNote(330.0, 400, ChipWaveform.TRIANGLE, 0.5f),
            ChipNote(370.0, 800, ChipWaveform.SINE, 0.4f)
        )
    }
    
    private fun generateHubMusic(): List<ChipNote> {
        return listOf(
            // Peaceful hub world theme
            ChipNote(262.0, 600, ChipWaveform.SINE, 0.4f), // C4
            ChipNote(330.0, 600, ChipWaveform.TRIANGLE, 0.5f), // E4
            ChipNote(392.0, 600, ChipWaveform.SINE, 0.4f), // G4
            ChipNote(523.0, 600, ChipWaveform.TRIANGLE, 0.5f), // C5
            ChipNote(440.0, 600, ChipWaveform.SINE, 0.4f), // A4
            ChipNote(330.0, 600, ChipWaveform.TRIANGLE, 0.5f), // E4
            ChipNote(262.0, 1200, ChipWaveform.SINE, 0.4f), // C4
            ChipNote(0.0, 400, ChipWaveform.SQUARE, 0.0f) // Rest
        )
    }
    
    private fun generateSynthesisMusic(): List<ChipNote> {
        return listOf(
            // Mysterious synthesis lab theme
            ChipNote(220.0, 300, ChipWaveform.SAWTOOTH, 0.3f),
            ChipNote(246.0, 300, ChipWaveform.TRIANGLE, 0.4f),
            ChipNote(277.0, 300, ChipWaveform.SAWTOOTH, 0.3f),
            ChipNote(311.0, 300, ChipWaveform.PULSE, 0.5f),
            ChipNote(349.0, 600, ChipWaveform.TRIANGLE, 0.4f),
            ChipNote(311.0, 300, ChipWaveform.SAWTOOTH, 0.3f),
            ChipNote(277.0, 300, ChipWaveform.TRIANGLE, 0.4f),
            ChipNote(220.0, 900, ChipWaveform.SINE, 0.3f)
        )
    }
    
    private fun generateVictoryMusic(): List<ChipNote> {
        return listOf(
            // Triumphant victory fanfare
            ChipNote(523.0, 150, ChipWaveform.SQUARE, 0.8f), // C5
            ChipNote(659.0, 150, ChipWaveform.SQUARE, 0.8f), // E5
            ChipNote(784.0, 150, ChipWaveform.SQUARE, 0.8f), // G5
            ChipNote(1047.0, 300, ChipWaveform.SQUARE, 0.9f), // C6
            ChipNote(0.0, 100, ChipWaveform.SQUARE, 0.0f), // Rest
            ChipNote(880.0, 150, ChipWaveform.TRIANGLE, 0.7f), // A5
            ChipNote(784.0, 150, ChipWaveform.TRIANGLE, 0.7f), // G5
            ChipNote(659.0, 150, ChipWaveform.TRIANGLE, 0.7f), // E5
            ChipNote(523.0, 600, ChipWaveform.SQUARE, 0.8f) // C5
        )
    }
    
    private fun generateDefeatMusic(): List<ChipNote> {
        return listOf(
            // Somber defeat theme
            ChipNote(440.0, 400, ChipWaveform.TRIANGLE, 0.6f),
            ChipNote(415.0, 400, ChipWaveform.TRIANGLE, 0.5f),
            ChipNote(392.0, 400, ChipWaveform.TRIANGLE, 0.5f),
            ChipNote(370.0, 400, ChipWaveform.SINE, 0.4f),
            ChipNote(330.0, 800, ChipWaveform.TRIANGLE, 0.4f),
            ChipNote(294.0, 800, ChipWaveform.SINE, 0.3f),
            ChipNote(262.0, 1200, ChipWaveform.TRIANGLE, 0.3f)
        )
    }
    
    /**
     * Update volume setting
     */
    fun setVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
    }
    
    /**
     * Enable or disable audio
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        if (!enabled) {
            currentAudioTrack?.stop()
        }
    }
    
    /**
     * Clean up resources
     */
    fun release() {
        scope.cancel()
        currentAudioTrack?.apply {
            stop()
            release()
        }
    }
}

/**
 * Data classes and enums for 8-bit audio system
 */
data class ChipNote(
    val frequency: Double, // Hz
    val duration: Int, // milliseconds
    val waveform: ChipWaveform,
    val volume: Float = 1.0f
)

enum class ChipWaveform {
    SQUARE, TRIANGLE, SAWTOOTH, SINE, PULSE, NOISE
}

enum class MusicContext {
    BATTLE, EXPLORATION, HUB_WORLD, SYNTHESIS_LAB, VICTORY, DEFEAT
}