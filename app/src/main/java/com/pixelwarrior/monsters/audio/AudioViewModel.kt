package com.pixelwarrior.monsters.audio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing game audio state and settings
 */
class AudioViewModel(context: Context) : ViewModel() {
    
    private val audioEngine = ChiptuneAudioEngine(context)
    
    val isMusicEnabled: StateFlow<Boolean> = audioEngine.isMusicEnabled
    val isSoundEnabled: StateFlow<Boolean> = audioEngine.isSoundEnabled
    val musicVolume: StateFlow<Float> = audioEngine.musicVolume
    val soundVolume: StateFlow<Float> = audioEngine.soundVolume
    
    /**
     * Play background music for different game screens
     */
    fun playTitleMusic() {
        audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.TITLE_THEME, true)
    }
    
    fun playWorldMapMusic() {
        audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.WORLD_MAP, true)
    }
    
    fun playBattleMusic() {
        audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.BATTLE_WILD, true)
    }
    
    fun playBossBattleMusic() {
        audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.BATTLE_BOSS, true)
    }
    
    fun playBreedingMusic() {
        audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.BREEDING_FARM, true)
    }
    
    fun playVictoryMusic() {
        audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.VICTORY, false)
    }
    
    fun playGameOverMusic() {
        audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.GAME_OVER, false)
    }
    
    fun stopMusic() {
        audioEngine.stopMusic()
    }
    
    /**
     * Play sound effects for game actions
     */
    fun playMenuSelectSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.MENU_SELECT)
    }
    
    fun playMenuBackSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.MENU_BACK)
    }
    
    fun playBattleHitSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.BATTLE_HIT)
    }
    
    fun playBattleMissSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.BATTLE_MISS)
    }
    
    fun playMonsterCaptureSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.MONSTER_CAPTURE)
    }
    
    fun playLevelUpSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.LEVEL_UP)
    }
    
    fun playHealingSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.HEALING)
    }
    
    fun playSkillUseSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.SKILL_USE)
    }
    
    fun playBreedingSuccessSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.BREEDING_SUCCESS)
    }
    
    fun playCoinCollectSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.COIN_COLLECT)
    }
    
    fun playErrorSound() {
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.ERROR)
    }
    
    /**
     * Audio settings management
     */
    fun setMusicEnabled(enabled: Boolean) {
        audioEngine.setMusicEnabled(enabled)
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        audioEngine.setSoundEnabled(enabled)
    }
    
    fun setMusicVolume(volume: Float) {
        audioEngine.setMusicVolume(volume)
    }
    
    fun setSoundVolume(volume: Float) {
        audioEngine.setSoundVolume(volume)
    }
    
    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}

/**
 * Factory for creating AudioViewModel with Context
 */
class AudioViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioViewModel::class.java)) {
            return AudioViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}