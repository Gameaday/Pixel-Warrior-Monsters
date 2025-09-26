package com.pixelwarrior.monsters.audio

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import android.content.Context
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*

/**
 * Unit tests for the chiptune audio system
 */
class AudioSystemTest {

    @Mock
    private lateinit var mockContext: Context
    
    private lateinit var audioEngine: ChiptuneAudioEngine
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        audioEngine = ChiptuneAudioEngine(mockContext)
    }
    
    @Test
    fun testAudioEngineInitialization() {
        assertTrue("Audio engine should initialize with music enabled", 
                   audioEngine.isMusicEnabled.value)
        assertTrue("Audio engine should initialize with sound enabled", 
                   audioEngine.isSoundEnabled.value)
        assertEquals("Default music volume should be 0.7", 0.7f, 
                    audioEngine.musicVolume.value, 0.01f)
        assertEquals("Default sound volume should be 0.8", 0.8f, 
                    audioEngine.soundVolume.value, 0.01f)
    }
    
    @Test
    fun testMusicTrackEnumValues() {
        val tracks = ChiptuneAudioEngine.MusicTrack.values()
        assertTrue("Should have TITLE_THEME track", 
                   tracks.contains(ChiptuneAudioEngine.MusicTrack.TITLE_THEME))
        assertTrue("Should have WORLD_MAP track", 
                   tracks.contains(ChiptuneAudioEngine.MusicTrack.WORLD_MAP))
        assertTrue("Should have BATTLE_WILD track", 
                   tracks.contains(ChiptuneAudioEngine.MusicTrack.BATTLE_WILD))
        assertTrue("Should have BREEDING_FARM track", 
                   tracks.contains(ChiptuneAudioEngine.MusicTrack.BREEDING_FARM))
        assertTrue("Should have VICTORY track", 
                   tracks.contains(ChiptuneAudioEngine.MusicTrack.VICTORY))
    }
    
    @Test
    fun testSoundEffectEnumValues() {
        val effects = ChiptuneAudioEngine.SoundEffect.values()
        assertTrue("Should have MENU_SELECT effect", 
                   effects.contains(ChiptuneAudioEngine.SoundEffect.MENU_SELECT))
        assertTrue("Should have BATTLE_HIT effect", 
                   effects.contains(ChiptuneAudioEngine.SoundEffect.BATTLE_HIT))
        assertTrue("Should have LEVEL_UP effect", 
                   effects.contains(ChiptuneAudioEngine.SoundEffect.LEVEL_UP))
        assertTrue("Should have MONSTER_CAPTURE effect", 
                   effects.contains(ChiptuneAudioEngine.SoundEffect.MONSTER_CAPTURE))
    }
    
    @Test
    fun testVolumeSettings() {
        audioEngine.setMusicVolume(0.5f)
        assertEquals("Music volume should be set to 0.5", 0.5f, 
                    audioEngine.musicVolume.value, 0.01f)
        
        audioEngine.setSoundVolume(0.3f)
        assertEquals("Sound volume should be set to 0.3", 0.3f, 
                    audioEngine.soundVolume.value, 0.01f)
        
        // Test volume clamping
        audioEngine.setMusicVolume(1.5f)
        assertEquals("Music volume should be clamped to 1.0", 1.0f, 
                    audioEngine.musicVolume.value, 0.01f)
        
        audioEngine.setSoundVolume(-0.1f)
        assertEquals("Sound volume should be clamped to 0.0", 0.0f, 
                    audioEngine.soundVolume.value, 0.01f)
    }
    
    @Test
    fun testMusicEnabledDisabled() {
        // Initially enabled
        assertTrue("Music should start enabled", audioEngine.isMusicEnabled.value)
        
        // Disable music
        audioEngine.setMusicEnabled(false)
        assertFalse("Music should be disabled", audioEngine.isMusicEnabled.value)
        
        // Re-enable music
        audioEngine.setMusicEnabled(true)
        assertTrue("Music should be re-enabled", audioEngine.isMusicEnabled.value)
    }
    
    @Test
    fun testSoundEnabledDisabled() {
        // Initially enabled
        assertTrue("Sound should start enabled", audioEngine.isSoundEnabled.value)
        
        // Disable sound
        audioEngine.setSoundEnabled(false)
        assertFalse("Sound should be disabled", audioEngine.isSoundEnabled.value)
        
        // Re-enable sound
        audioEngine.setSoundEnabled(true)
        assertTrue("Sound should be re-enabled", audioEngine.isSoundEnabled.value)
    }
    
    @Test
    fun testMusicPlayback() = runBlocking {
        // Test that music playback doesn't throw exceptions
        try {
            audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.TITLE_THEME, true)
            // Brief delay to simulate playback start
            kotlinx.coroutines.delay(100)
            audioEngine.stopMusic()
        } catch (e: Exception) {
            fail("Music playback should not throw exceptions: ${e.message}")
        }
    }
    
    @Test
    fun testSoundEffectPlayback() = runBlocking {
        // Test that sound effect playback doesn't throw exceptions
        try {
            audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.MENU_SELECT)
            audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.BATTLE_HIT)
            audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.LEVEL_UP)
            // Brief delay to simulate sound playback
            kotlinx.coroutines.delay(50)
        } catch (e: Exception) {
            fail("Sound effect playback should not throw exceptions: ${e.message}")
        }
    }
    
    @Test
    fun testAudioEngineCleanup() {
        try {
            audioEngine.release()
            // Should not throw any exceptions
        } catch (e: Exception) {
            fail("Audio engine cleanup should not throw exceptions: ${e.message}")
        }
    }
    
    @Test
    fun testDisabledAudioDoesNotPlay() {
        // Disable music and sound
        audioEngine.setMusicEnabled(false)
        audioEngine.setSoundEnabled(false)
        
        // These calls should not actually play audio
        audioEngine.playMusic(ChiptuneAudioEngine.MusicTrack.BATTLE_WILD, true)
        audioEngine.playSound(ChiptuneAudioEngine.SoundEffect.BATTLE_HIT)
        
        // Test passes if no exceptions are thrown and audio remains disabled
        assertFalse("Music should remain disabled", audioEngine.isMusicEnabled.value)
        assertFalse("Sound should remain disabled", audioEngine.isSoundEnabled.value)
    }
}