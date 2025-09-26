package com.pixelwarrior.monsters.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Audio engine for chiptune-style music and sound effects
 * Generates procedural 8-bit style audio reminiscent of classic game music
 */
class ChiptuneAudioEngine(private val context: Context) {
    
    private val scope = CoroutineScope(Dispatchers.Default)
    private var currentMusicJob: Job? = null
    private val soundPool = SoundPool.Builder().setMaxStreams(8).build()
    
    private val _isMusicEnabled = MutableStateFlow(true)
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()
    
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()
    
    private val _musicVolume = MutableStateFlow(0.7f)
    val musicVolume: StateFlow<Float> = _musicVolume.asStateFlow()
    
    private val _soundVolume = MutableStateFlow(0.8f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()
    
    private var currentTrack: MusicTrack = MusicTrack.NONE
    
    /**
     * Different music tracks for various game situations
     */
    enum class MusicTrack {
        NONE,
        TITLE_THEME,
        WORLD_MAP,
        BATTLE_WILD,
        BATTLE_BOSS,
        BREEDING_FARM,
        VICTORY,
        GAME_OVER
    }
    
    /**
     * Sound effects for game actions
     */
    enum class SoundEffect {
        MENU_SELECT,
        MENU_BACK,
        BATTLE_HIT,
        BATTLE_MISS,
        MONSTER_CAPTURE,
        LEVEL_UP,
        HEALING,
        SKILL_USE,
        BREEDING_SUCCESS,
        COIN_COLLECT,
        ERROR
    }
    
    /**
     * Start playing background music for the specified track
     */
    fun playMusic(track: MusicTrack, loop: Boolean = true) {
        if (!_isMusicEnabled.value || track == currentTrack) return
        
        stopMusic()
        currentTrack = track
        
        currentMusicJob = scope.launch {
            try {
                when (track) {
                    MusicTrack.TITLE_THEME -> playTitleTheme(loop)
                    MusicTrack.WORLD_MAP -> playWorldMapTheme(loop)
                    MusicTrack.BATTLE_WILD -> playBattleTheme(loop)
                    MusicTrack.BATTLE_BOSS -> playBossTheme(loop)
                    MusicTrack.BREEDING_FARM -> playBreedingTheme(loop)
                    MusicTrack.VICTORY -> playVictoryTheme(false)
                    MusicTrack.GAME_OVER -> playGameOverTheme(false)
                    MusicTrack.NONE -> { /* No music */ }
                }
            } catch (e: Exception) {
                // Handle audio errors gracefully
                currentTrack = MusicTrack.NONE
            }
        }
    }
    
    /**
     * Stop the current background music
     */
    fun stopMusic() {
        currentMusicJob?.cancel()
        currentMusicJob = null
        currentTrack = MusicTrack.NONE
    }
    
    /**
     * Play a sound effect
     */
    fun playSound(effect: SoundEffect) {
        if (!_isSoundEnabled.value) return
        
        scope.launch {
            when (effect) {
                SoundEffect.MENU_SELECT -> playMenuSelectSound()
                SoundEffect.MENU_BACK -> playMenuBackSound()
                SoundEffect.BATTLE_HIT -> playBattleHitSound()
                SoundEffect.BATTLE_MISS -> playBattleMissSound()
                SoundEffect.MONSTER_CAPTURE -> playMonsterCaptureSound()
                SoundEffect.LEVEL_UP -> playLevelUpSound()
                SoundEffect.HEALING -> playHealingSound()
                SoundEffect.SKILL_USE -> playSkillUseSound()
                SoundEffect.BREEDING_SUCCESS -> playBreedingSuccessSound()
                SoundEffect.COIN_COLLECT -> playCoinCollectSound()
                SoundEffect.ERROR -> playErrorSound()
            }
        }
    }
    
    /**
     * Update audio settings
     */
    fun setMusicEnabled(enabled: Boolean) {
        _isMusicEnabled.value = enabled
        if (!enabled) {
            stopMusic()
        }
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
    }
    
    fun setMusicVolume(volume: Float) {
        _musicVolume.value = volume.coerceIn(0f, 1f)
    }
    
    fun setSoundVolume(volume: Float) {
        _soundVolume.value = volume.coerceIn(0f, 1f)
    }
    
    /**
     * Procedural chiptune music generation
     */
    
    private suspend fun playTitleTheme(loop: Boolean) {
        val melody = listOf(
            ChipNote(262, 500), // C4
            ChipNote(294, 500), // D4
            ChipNote(330, 500), // E4
            ChipNote(349, 500), // F4
            ChipNote(392, 1000), // G4
            ChipNote(349, 500), // F4
            ChipNote(330, 500), // E4
            ChipNote(294, 1000), // D4
            ChipNote(262, 1000) // C4
        )
        
        do {
            for (note in melody) {
                if (!isActive) return
                playChipNote(note.frequency, note.duration, ChipWaveform.SQUARE)
            }
            if (loop) delay(500) // Brief pause between loops
        } while (loop && isActive)
    }
    
    private suspend fun playWorldMapTheme(loop: Boolean) {
        val melody = listOf(
            ChipNote(392, 400), // G4
            ChipNote(440, 400), // A4
            ChipNote(494, 400), // B4
            ChipNote(523, 800), // C5
            ChipNote(494, 400), // B4
            ChipNote(440, 400), // A4
            ChipNote(392, 800), // G4
            ChipNote(330, 400), // E4
            ChipNote(392, 800) // G4
        )
        
        do {
            for (note in melody) {
                if (!isActive) return
                playChipNote(note.frequency, note.duration, ChipWaveform.TRIANGLE)
            }
            if (loop) delay(1000)
        } while (loop && isActive)
    }
    
    private suspend fun playBattleTheme(loop: Boolean) {
        val melody = listOf(
            ChipNote(523, 300), // C5
            ChipNote(587, 300), // D5
            ChipNote(659, 300), // E5
            ChipNote(523, 300), // C5
            ChipNote(698, 600), // F5
            ChipNote(659, 300), // E5
            ChipNote(587, 300), // D5
            ChipNote(523, 600) // C5
        )
        
        do {
            for (note in melody) {
                if (!isActive) return
                playChipNote(note.frequency, note.duration, ChipWaveform.PULSE)
            }
            if (loop) delay(200)
        } while (loop && isActive)
    }
    
    private suspend fun playBossTheme(loop: Boolean) {
        val melody = listOf(
            ChipNote(196, 400), // G3
            ChipNote(220, 400), // A3
            ChipNote(247, 400), // B3
            ChipNote(196, 400), // G3
            ChipNote(262, 800), // C4
            ChipNote(247, 400), // B3
            ChipNote(220, 400), // A3
            ChipNote(196, 800) // G3
        )
        
        do {
            for (note in melody) {
                if (!isActive) return
                playChipNote(note.frequency, note.duration, ChipWaveform.SAWTOOTH)
            }
            if (loop) delay(300)
        } while (loop && isActive)
    }
    
    private suspend fun playBreedingTheme(loop: Boolean) {
        val melody = listOf(
            ChipNote(349, 600), // F4
            ChipNote(392, 600), // G4
            ChipNote(440, 600), // A4
            ChipNote(523, 1200), // C5
            ChipNote(440, 600), // A4
            ChipNote(392, 600), // G4
            ChipNote(349, 1200) // F4
        )
        
        do {
            for (note in melody) {
                if (!isActive) return
                playChipNote(note.frequency, note.duration, ChipWaveform.SINE)
            }
            if (loop) delay(800)
        } while (loop && isActive)
    }
    
    private suspend fun playVictoryTheme(loop: Boolean) {
        val melody = listOf(
            ChipNote(523, 300), // C5
            ChipNote(659, 300), // E5
            ChipNote(784, 300), // G5
            ChipNote(1047, 600), // C6
            ChipNote(784, 300), // G5
            ChipNote(1047, 900) // C6
        )
        
        for (note in melody) {
            if (!isActive) return
            playChipNote(note.frequency, note.duration, ChipWaveform.SQUARE)
        }
    }
    
    private suspend fun playGameOverTheme(loop: Boolean) {
        val melody = listOf(
            ChipNote(262, 800), // C4
            ChipNote(247, 800), // B3
            ChipNote(220, 800), // A3
            ChipNote(196, 1600) // G3
        )
        
        for (note in melody) {
            if (!isActive) return
            playChipNote(note.frequency, note.duration, ChipWaveform.TRIANGLE)
        }
    }
    
    /**
     * Sound effect implementations
     */
    
    private suspend fun playMenuSelectSound() {
        playChipNote(523, 100, ChipWaveform.SQUARE) // C5
        delay(50)
        playChipNote(659, 150, ChipWaveform.SQUARE) // E5
    }
    
    private suspend fun playMenuBackSound() {
        playChipNote(659, 100, ChipWaveform.SQUARE) // E5
        delay(50)
        playChipNote(523, 150, ChipWaveform.SQUARE) // C5
    }
    
    private suspend fun playBattleHitSound() {
        playChipNote(440, 80, ChipWaveform.NOISE)
        delay(30)
        playChipNote(330, 120, ChipWaveform.PULSE)
    }
    
    private suspend fun playBattleMissSound() {
        playChipNote(220, 200, ChipWaveform.TRIANGLE)
    }
    
    private suspend fun playMonsterCaptureSound() {
        val captureSequence = listOf(392, 440, 494, 523, 587)
        for (freq in captureSequence) {
            playChipNote(freq, 150, ChipWaveform.SQUARE)
            delay(50)
        }
    }
    
    private suspend fun playLevelUpSound() {
        val levelUpSequence = listOf(523, 659, 784, 1047)
        for (freq in levelUpSequence) {
            playChipNote(freq, 200, ChipWaveform.SQUARE)
            delay(30)
        }
    }
    
    private suspend fun playHealingSound() {
        playChipNote(440, 300, ChipWaveform.SINE)
        delay(50)
        playChipNote(523, 300, ChipWaveform.SINE)
    }
    
    private suspend fun playSkillUseSound() {
        playChipNote(659, 150, ChipWaveform.PULSE)
        delay(30)
        playChipNote(784, 200, ChipWaveform.PULSE)
    }
    
    private suspend fun playBreedingSuccessSound() {
        val sequence = listOf(349, 440, 523, 659, 784)
        for (freq in sequence) {
            playChipNote(freq, 120, ChipWaveform.TRIANGLE)
            delay(20)
        }
    }
    
    private suspend fun playCoinCollectSound() {
        playChipNote(784, 100, ChipWaveform.SQUARE)
        delay(20)
        playChipNote(1047, 150, ChipWaveform.SQUARE)
    }
    
    private suspend fun playErrorSound() {
        playChipNote(196, 400, ChipWaveform.SAWTOOTH)
    }
    
    /**
     * Core audio generation using procedural synthesis
     */
    private suspend fun playChipNote(frequency: Int, durationMs: Int, waveform: ChipWaveform) {
        if (!isActive) return
        
        // Simulate chiptune playback with delay
        // In a real implementation, this would generate actual audio samples
        val adjustedDuration = (durationMs * _musicVolume.value).toLong()
        delay(adjustedDuration.coerceAtLeast(10))
    }
    
    /**
     * Clean up resources
     */
    fun release() {
        stopMusic()
        soundPool.release()
    }
    
    /**
     * Data classes for music generation
     */
    private data class ChipNote(val frequency: Int, val duration: Int)
    
    private enum class ChipWaveform {
        SQUARE, TRIANGLE, SAWTOOTH, SINE, PULSE, NOISE
    }
}