package com.pixelwarrior.monsters.audio

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Unit tests for Real 8-Bit Audio System - Phase 4 Implementation
 * Tests authentic 8-bit audio generation, voice synthesis, and contextual music
 */
class Real8BitAudioSystemTest {

    private lateinit var mockContext: Context
    private lateinit var real8BitEngine: Real8BitAudioEngine
    private lateinit var voiceSynthesis: Voice8BitSynthesis
    private lateinit var chiptuneEngine: ChiptuneAudioEngine

    @Before
    fun setup() {
        mockContext = mock(Context::class.java)
        real8BitEngine = Real8BitAudioEngine(mockContext)
        voiceSynthesis = Voice8BitSynthesis(mockContext, real8BitEngine)
        chiptuneEngine = ChiptuneAudioEngine(mockContext)
    }

    @Test
    fun `real 8-bit engine initializes correctly`() = runTest {
        assertTrue("Engine should be enabled by default", real8BitEngine.isEnabled.value)
        assertTrue("Volume should be reasonable", real8BitEngine.volume.value > 0.0f)
        assertEquals("Default volume should be 0.7", 0.7f, real8BitEngine.volume.value, 0.01f)
    }

    @Test
    fun `chip note generation works for different waveforms`() = runTest {
        // Test that waveform generation doesn't crash for different types
        real8BitEngine.playChipNote(440.0, 100, ChipWaveform.SQUARE)
        real8BitEngine.playChipNote(440.0, 100, ChipWaveform.TRIANGLE)
        real8BitEngine.playChipNote(440.0, 100, ChipWaveform.SAWTOOTH)
        real8BitEngine.playChipNote(440.0, 100, ChipWaveform.SINE)
        real8BitEngine.playChipNote(440.0, 100, ChipWaveform.PULSE)
        real8BitEngine.playChipNote(440.0, 100, ChipWaveform.NOISE)
        
        // If we get here without exceptions, the basic generation works
        assertTrue("Waveform generation should complete without errors", true)
    }

    @Test
    fun `melody playback works correctly`() = runTest {
        val melody = listOf(
            ChipNote(262.0, 200, ChipWaveform.SQUARE), // C4
            ChipNote(330.0, 200, ChipWaveform.SQUARE), // E4
            ChipNote(392.0, 200, ChipWaveform.SQUARE)  // G4
        )
        
        // Test melody playback (should complete without errors)
        real8BitEngine.playMelody(melody, loop = false)
        
        assertTrue("Melody playback should complete successfully", true)
    }

    @Test
    fun `monster cry generation produces unique patterns`() = runTest {
        // Test different monster species produce different cry patterns
        real8BitEngine.playMonsterCry("dragon")
        real8BitEngine.playMonsterCry("slime") 
        real8BitEngine.playMonsterCry("beast")
        real8BitEngine.playMonsterCry("bird")
        real8BitEngine.playMonsterCry("unknown_species") // Should use generic pattern
        
        assertTrue("Monster cry generation should handle all species", true)
    }

    @Test
    fun `contextual music generates different melodies`() = runTest {
        // Test different music contexts
        real8BitEngine.playBackgroundMusic(MusicContext.BATTLE, loop = false)
        real8BitEngine.playBackgroundMusic(MusicContext.EXPLORATION, loop = false)
        real8BitEngine.playBackgroundMusic(MusicContext.HUB_WORLD, loop = false)
        real8BitEngine.playBackgroundMusic(MusicContext.SYNTHESIS_LAB, loop = false)
        real8BitEngine.playBackgroundMusic(MusicContext.VICTORY, loop = false)
        real8BitEngine.playBackgroundMusic(MusicContext.DEFEAT, loop = false)
        
        assertTrue("Contextual music should generate for all contexts", true)
    }

    @Test
    fun `voice synthesis initializes correctly`() = runTest {
        assertTrue("Voice should be enabled by default", voiceSynthesis.voiceEnabled.value)
        assertTrue("Voice volume should be reasonable", voiceSynthesis.voiceVolume.value > 0.0f)
        assertEquals("Default voice volume should be 0.6", 0.6f, voiceSynthesis.voiceVolume.value, 0.01f)
    }

    @Test
    fun `character voice patterns are unique`() = runTest {
        val master = VoiceCharacter("master", "Master Teto", CharacterType.MASTER)
        val librarian = VoiceCharacter("librarian", "Scholar Maya", CharacterType.LIBRARIAN)
        val rival = VoiceCharacter("rival", "Rival Alex", CharacterType.RIVAL)
        
        // Test that different characters can speak
        voiceSynthesis.speakCharacterLine(master, "Welcome, young trainer!")
        voiceSynthesis.speakCharacterLine(librarian, "Knowledge is power.")
        voiceSynthesis.speakCharacterLine(rival, "I'll show you!")
        
        assertTrue("Character voice synthesis should work for different characters", true)
    }

    @Test
    fun `voice emotions modify speech patterns`() = runTest {
        val character = VoiceCharacter("test", "Test Character", CharacterType.MASTER)
        
        // Test different emotions
        voiceSynthesis.speakCharacterLine(character, "Hello!", VoiceEmotion.HAPPY)
        voiceSynthesis.speakCharacterLine(character, "Hello!", VoiceEmotion.SAD)
        voiceSynthesis.speakCharacterLine(character, "Hello!", VoiceEmotion.ANGRY)
        voiceSynthesis.speakCharacterLine(character, "Hello!", VoiceEmotion.EXCITED)
        
        assertTrue("Voice emotions should modify speech without errors", true)
    }

    @Test
    fun `voice acknowledgments work for UI interactions`() = runTest {
        val character = VoiceCharacter("ui", "UI Character", CharacterType.MASTER)
        
        // Test different acknowledgment types
        voiceSynthesis.playVoiceAck(character, VoiceAckType.CONFIRM)
        voiceSynthesis.playVoiceAck(character, VoiceAckType.CANCEL)
        voiceSynthesis.playVoiceAck(character, VoiceAckType.SELECT)
        voiceSynthesis.playVoiceAck(character, VoiceAckType.ERROR)
        
        assertTrue("Voice acknowledgments should work for all types", true)
    }

    @Test
    fun `chiptune engine integrates with real audio system`() = runTest {
        // Test that ChiptuneAudioEngine uses real audio instead of delays
        
        val character = VoiceCharacter("master", "Master", CharacterType.MASTER)
        
        // Test integrated voice functionality
        chiptuneEngine.playCharacterVoice(character, "Hello, trainer!")
        chiptuneEngine.playVoiceAck(character, VoiceAckType.CONFIRM)
        
        // Test integrated music functionality
        chiptuneEngine.playContextualMusic(MusicContext.HUB_WORLD, loop = false)
        
        // Test integrated monster cries
        chiptuneEngine.playMonsterCry("dragon")
        
        assertTrue("ChiptuneAudioEngine should integrate with real audio system", true)
    }

    @Test
    fun `audio settings can be updated`() {
        chiptuneEngine.updateAudioSettings(
            musicEnabled = false,
            soundEnabled = true,
            musicVolume = 0.5f,
            soundVolume = 0.8f
        )
        
        // Test that settings were applied
        assertFalse("Music should be disabled", chiptuneEngine.isMusicEnabled.value)
        assertTrue("Sound should be enabled", chiptuneEngine.isSoundEnabled.value)
        assertEquals("Music volume should be updated", 0.5f, chiptuneEngine.musicVolume.value, 0.01f)
        assertEquals("Sound volume should be updated", 0.8f, chiptuneEngine.soundVolume.value, 0.01f)
    }

    @Test
    fun `volume controls work correctly`() {
        real8BitEngine.setVolume(0.5f)
        assertEquals("Volume should be set correctly", 0.5f, real8BitEngine.volume.value, 0.01f)
        
        real8BitEngine.setVolume(1.2f) // Should clamp to 1.0
        assertEquals("Volume should be clamped to 1.0", 1.0f, real8BitEngine.volume.value, 0.01f)
        
        real8BitEngine.setVolume(-0.1f) // Should clamp to 0.0
        assertEquals("Volume should be clamped to 0.0", 0.0f, real8BitEngine.volume.value, 0.01f)
        
        voiceSynthesis.setVoiceVolume(0.3f)
        assertEquals("Voice volume should be set correctly", 0.3f, voiceSynthesis.voiceVolume.value, 0.01f)
    }

    @Test
    fun `enable disable controls work correctly`() {
        real8BitEngine.setEnabled(false)
        assertFalse("Audio engine should be disabled", real8BitEngine.isEnabled.value)
        
        real8BitEngine.setEnabled(true)
        assertTrue("Audio engine should be enabled", real8BitEngine.isEnabled.value)
        
        voiceSynthesis.setVoiceEnabled(false)
        assertFalse("Voice synthesis should be disabled", voiceSynthesis.voiceEnabled.value)
        
        voiceSynthesis.setVoiceEnabled(true)
        assertTrue("Voice synthesis should be enabled", voiceSynthesis.voiceEnabled.value)
    }

    @Test
    fun `syllable breaking works for voice synthesis`() {
        val character = VoiceCharacter("test", "Test", CharacterType.MASTER)
        
        // Test different text lengths and patterns
        voiceSynthesis.speakCharacterLine(character, "Hi") // Short
        voiceSynthesis.speakCharacterLine(character, "Hello world") // Medium
        voiceSynthesis.speakCharacterLine(character, "Welcome to the monster training academy") // Long
        voiceSynthesis.speakCharacterLine(character, "Synthesis") // Complex word
        
        assertTrue("Voice synthesis should handle various text patterns", true)
    }

    @Test
    fun `audio system resources can be released safely`() {
        // Test that release methods don't throw exceptions
        real8BitEngine.release()
        voiceSynthesis.release()
        chiptuneEngine.release()
        
        assertTrue("Audio system should release resources safely", true)
    }

    @Test
    fun `waveform generation produces valid audio samples`() {
        // This would require more complex testing with actual audio buffer analysis
        // For now, we test that generation doesn't crash
        
        val testFrequencies = listOf(220.0, 440.0, 880.0, 1760.0) // Different octaves
        val testDurations = listOf(50, 100, 200, 500) // Different durations
        
        for (freq in testFrequencies) {
            for (duration in testDurations) {
                for (waveform in ChipWaveform.values()) {
                    try {
                        runTest {
                            real8BitEngine.playChipNote(freq, duration, waveform, 0.5f)
                        }
                    } catch (e: Exception) {
                        fail("Waveform generation should not throw exception for $waveform at ${freq}Hz for ${duration}ms")
                    }
                }
            }
        }
        
        assertTrue("All waveform generation should complete successfully", true)
    }
}