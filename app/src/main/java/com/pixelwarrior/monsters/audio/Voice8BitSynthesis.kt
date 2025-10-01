package com.pixelwarrior.monsters.audio

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * 8-Bit Voice Synthesis System - Phase 4 Implementation
 * Generates procedural character voices with 8-bit filtering and processing
 */
class Voice8BitSynthesis(
    private val context: Context,
    private val audioEngine: Real8BitAudioEngine
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _voiceEnabled = MutableStateFlow(true)
    val voiceEnabled: StateFlow<Boolean> = _voiceEnabled.asStateFlow()
    
    private val _voiceVolume = MutableStateFlow(0.6f)
    val voiceVolume: StateFlow<Float> = _voiceVolume.asStateFlow()
    
    /**
     * Generate and play 8-bit style character voice
     */
    suspend fun speakCharacterLine(
        character: VoiceCharacter,
        text: String,
        emotion: VoiceEmotion = VoiceEmotion.NEUTRAL
    ) {
        if (!_voiceEnabled.value) return
        
        val voicePattern = generateVoicePattern(character, text, emotion)
        playVoicePattern(voicePattern)
    }
    
    /**
     * Speak character line using data model VoiceCharacter
     */
    suspend fun speakCharacterLine(
        character: com.pixelwarrior.monsters.data.model.VoiceCharacter,
        text: String
    ) {
        // Convert data.model.VoiceCharacter to audio.VoiceCharacter
        val audioCharacter = VoiceCharacter(
            name = character.id,
            displayName = character.name,
            type = when (character.type) {
                com.pixelwarrior.monsters.data.model.CharacterType.MASTER -> CharacterType.MASTER
                com.pixelwarrior.monsters.data.model.CharacterType.LIBRARIAN -> CharacterType.LIBRARIAN
                com.pixelwarrior.monsters.data.model.CharacterType.SYNTHESIS_EXPERT -> CharacterType.SYNTHESIS_EXPERT
                com.pixelwarrior.monsters.data.model.CharacterType.ARENA_MASTER -> CharacterType.ARENA_MASTER
                com.pixelwarrior.monsters.data.model.CharacterType.MERCHANT -> CharacterType.MERCHANT
                com.pixelwarrior.monsters.data.model.CharacterType.RIVAL -> CharacterType.RIVAL
                else -> CharacterType.MYSTERIOUS_NPC
            }
        )
        speakCharacterLine(audioCharacter, text)
    }
    
    /**
     * Play quick voice acknowledgment (for menu interactions)
     */
    suspend fun playVoiceAck(character: VoiceCharacter, ackType: VoiceAckType) {
        if (!_voiceEnabled.value) return
        
        val pattern = when (ackType) {
            VoiceAckType.CONFIRM -> generateConfirmSound(character)
            VoiceAckType.CANCEL -> generateCancelSound(character)
            VoiceAckType.SELECT -> generateSelectSound(character)
            VoiceAckType.ERROR -> generateErrorSound(character)
        }
        
        playVoicePattern(pattern)
    }
    
    /**
     * Generate voice pattern based on character and text
     */
    private fun generateVoicePattern(
        character: VoiceCharacter,
        text: String,
        emotion: VoiceEmotion
    ): VoicePattern {
        
        val baseFreq = getCharacterBaseFrequency(character)
        val tempo = getCharacterTempo(character)
        val waveform = getCharacterWaveform(character)
        
        // Apply emotion modifiers
        val emotionModifier = getEmotionModifier(emotion)
        val finalFreq = baseFreq * emotionModifier.pitchMultiplier
        val finalTempo = tempo * emotionModifier.speedMultiplier
        
        // Generate syllable-based pattern
        val syllables = breakIntoSyllables(text)
        val notes = mutableListOf<ChipNote>()
        
        for ((index, syllable) in syllables.withIndex()) {
            val syllableFreq = finalFreq + (syllable.length * 10) + Random.nextInt(-20, 21)
            val syllableDuration = (finalTempo * syllable.length * 50).toInt().coerceIn(50, 300)
            
            // Add slight frequency variation for naturalness
            val variation = if (index % 2 == 0) 1.05 else 0.95
            
            notes.add(
                ChipNote(
                    frequency = syllableFreq * variation,
                    duration = syllableDuration,
                    waveform = waveform,
                    volume = _voiceVolume.value * emotionModifier.volumeMultiplier
                )
            )
            
            // Add brief pause between syllables
            if (index < syllables.size - 1) {
                notes.add(
                    ChipNote(
                        frequency = 0.0, // Silence
                        duration = (finalTempo * 30).toInt(),
                        waveform = waveform,
                        volume = 0f
                    )
                )
            }
        }
        
        return VoicePattern(
            character = character,
            notes = notes,
            emotion = emotion,
            totalDuration = notes.sumOf { it.duration }
        )
    }
    
    /**
     * Play a voice pattern using the audio engine
     */
    private suspend fun playVoicePattern(pattern: VoicePattern) {
        audioEngine.playMelody(pattern.notes, loop = false)
    }
    
    /**
     * Get character's base voice frequency
     */
    private fun getCharacterBaseFrequency(character: VoiceCharacter): Double {
        return when (character.type) {
            CharacterType.MASTER -> 200.0 // Deep, authoritative
            CharacterType.LIBRARIAN -> 300.0 // Higher, scholarly
            CharacterType.SYNTHESIS_EXPERT -> 250.0 // Mid-range, technical
            CharacterType.ARENA_MASTER -> 180.0 // Lower, commanding
            CharacterType.MERCHANT -> 350.0 // Higher, friendly
            CharacterType.STABLE_KEEPER -> 220.0 // Warm, caring
            CharacterType.RIVAL -> 280.0 // Youthful, energetic
            CharacterType.MYSTERIOUS_NPC -> 160.0 // Very low, mysterious
        }
    }
    
    /**
     * Get character's speaking tempo
     */
    private fun getCharacterTempo(character: VoiceCharacter): Double {
        return when (character.type) {
            CharacterType.MASTER -> 1.0 // Normal pace
            CharacterType.LIBRARIAN -> 0.8 // Slower, thoughtful
            CharacterType.SYNTHESIS_EXPERT -> 1.2 // Faster, excited about science
            CharacterType.ARENA_MASTER -> 1.1 // Slightly faster, energetic
            CharacterType.MERCHANT -> 1.3 // Quick, business-like
            CharacterType.STABLE_KEEPER -> 0.9 // Relaxed pace
            CharacterType.RIVAL -> 1.4 // Fast, impulsive
            CharacterType.MYSTERIOUS_NPC -> 0.7 // Very slow, deliberate
        }
    }
    
    /**
     * Get character's preferred waveform
     */
    private fun getCharacterWaveform(character: VoiceCharacter): ChipWaveform {
        return when (character.type) {
            CharacterType.MASTER -> ChipWaveform.SQUARE // Strong, authoritative
            CharacterType.LIBRARIAN -> ChipWaveform.TRIANGLE // Soft, gentle
            CharacterType.SYNTHESIS_EXPERT -> ChipWaveform.SAWTOOTH // Sharp, scientific
            CharacterType.ARENA_MASTER -> ChipWaveform.PULSE // Bold, commanding
            CharacterType.MERCHANT -> ChipWaveform.SINE // Smooth, friendly
            CharacterType.STABLE_KEEPER -> ChipWaveform.TRIANGLE // Warm, nurturing
            CharacterType.RIVAL -> ChipWaveform.SQUARE // Bold, confrontational
            CharacterType.MYSTERIOUS_NPC -> ChipWaveform.SAWTOOTH // Mysterious, otherworldly
        }
    }
    
    /**
     * Get emotion modifiers for voice
     */
    private fun getEmotionModifier(emotion: VoiceEmotion): EmotionModifier {
        return when (emotion) {
            VoiceEmotion.NEUTRAL -> EmotionModifier(1.0f, 1.0f, 1.0f)
            VoiceEmotion.HAPPY -> EmotionModifier(1.2f, 1.1f, 1.1f) // Higher pitch, faster, louder
            VoiceEmotion.SAD -> EmotionModifier(0.8f, 0.8f, 0.7f) // Lower pitch, slower, quieter
            VoiceEmotion.ANGRY -> EmotionModifier(1.3f, 1.2f, 1.2f) // Much higher pitch, faster, louder
            VoiceEmotion.EXCITED -> EmotionModifier(1.4f, 1.3f, 1.1f) // High pitch, fast, loud
            VoiceEmotion.CONFUSED -> EmotionModifier(1.1f, 0.9f, 0.9f) // Slightly higher, slower
            VoiceEmotion.MYSTERIOUS -> EmotionModifier(0.7f, 0.8f, 0.8f) // Lower, slower, quieter
            VoiceEmotion.DETERMINED -> EmotionModifier(1.1f, 1.0f, 1.2f) // Slightly higher, same speed, louder
        }
    }
    
    /**
     * Break text into syllables for voice generation
     */
    private fun breakIntoSyllables(text: String): List<String> {
        // Simple syllable breaking - split on vowel groups
        val words = text.lowercase().split(" ")
        val syllables = mutableListOf<String>()
        
        for (word in words) {
            if (word.length <= 3) {
                syllables.add(word)
            } else {
                // Simple syllable detection
                var currentSyllable = ""
                var vowelCount = 0
                
                for (char in word) {
                    currentSyllable += char
                    
                    if (char in "aeiou") {
                        vowelCount++
                        
                        if (vowelCount == 2 || currentSyllable.length >= 4) {
                            syllables.add(currentSyllable.dropLast(1))
                            currentSyllable = char.toString()
                            vowelCount = 1
                        }
                    }
                }
                
                if (currentSyllable.isNotEmpty()) {
                    syllables.add(currentSyllable)
                }
            }
        }
        
        return syllables.ifEmpty { listOf("...") }
    }
    
    /**
     * Generate quick confirmation sound
     */
    private fun generateConfirmSound(character: VoiceCharacter): VoicePattern {
        val baseFreq = getCharacterBaseFrequency(character)
        val waveform = getCharacterWaveform(character)
        
        return VoicePattern(
            character = character,
            notes = listOf(
                ChipNote(baseFreq, 100, waveform, _voiceVolume.value),
                ChipNote(baseFreq * 1.5, 150, waveform, _voiceVolume.value)
            ),
            emotion = VoiceEmotion.NEUTRAL,
            totalDuration = 250
        )
    }
    
    /**
     * Generate cancel/negative sound
     */
    private fun generateCancelSound(character: VoiceCharacter): VoicePattern {
        val baseFreq = getCharacterBaseFrequency(character)
        val waveform = getCharacterWaveform(character)
        
        return VoicePattern(
            character = character,
            notes = listOf(
                ChipNote(baseFreq, 120, waveform, _voiceVolume.value),
                ChipNote(baseFreq * 0.7, 180, waveform, _voiceVolume.value)
            ),
            emotion = VoiceEmotion.NEUTRAL,
            totalDuration = 300
        )
    }
    
    /**
     * Generate selection sound
     */
    private fun generateSelectSound(character: VoiceCharacter): VoicePattern {
        val baseFreq = getCharacterBaseFrequency(character)
        val waveform = getCharacterWaveform(character)
        
        return VoicePattern(
            character = character,
            notes = listOf(
                ChipNote(baseFreq * 1.2, 80, waveform, _voiceVolume.value * 0.8f)
            ),
            emotion = VoiceEmotion.NEUTRAL,
            totalDuration = 80
        )
    }
    
    /**
     * Generate error sound
     */
    private fun generateErrorSound(character: VoiceCharacter): VoicePattern {
        val baseFreq = getCharacterBaseFrequency(character)
        
        return VoicePattern(
            character = character,
            notes = listOf(
                ChipNote(baseFreq * 0.6, 200, ChipWaveform.SAWTOOTH, _voiceVolume.value),
                ChipNote(0.0, 50, ChipWaveform.SQUARE, 0f), // Brief pause
                ChipNote(baseFreq * 0.5, 200, ChipWaveform.SAWTOOTH, _voiceVolume.value)
            ),
            emotion = VoiceEmotion.NEUTRAL,
            totalDuration = 450
        )
    }
    
    /**
     * Set voice enabled state
     */
    fun setVoiceEnabled(enabled: Boolean) {
        _voiceEnabled.value = enabled
    }
    
    /**
     * Set voice volume
     */
    fun setVoiceVolume(volume: Float) {
        _voiceVolume.value = volume.coerceIn(0f, 1f)
    }
    
    /**
     * Release resources
     */
    fun release() {
        scope.cancel()
    }
}

/**
 * Data classes for voice synthesis
 */
data class VoiceCharacter(
    val name: String,
    val displayName: String,
    val type: CharacterType
)

data class VoicePattern(
    val character: VoiceCharacter,
    val notes: List<ChipNote>,
    val emotion: VoiceEmotion,
    val totalDuration: Int
)

data class EmotionModifier(
    val pitchMultiplier: Float,
    val speedMultiplier: Float,
    val volumeMultiplier: Float
)

enum class CharacterType {
    MASTER, LIBRARIAN, SYNTHESIS_EXPERT, ARENA_MASTER,
    MERCHANT, STABLE_KEEPER, RIVAL, MYSTERIOUS_NPC
}

enum class VoiceEmotion {
    NEUTRAL, HAPPY, SAD, ANGRY, EXCITED, CONFUSED, MYSTERIOUS, DETERMINED
}

enum class VoiceAckType {
    CONFIRM, CANCEL, SELECT, ERROR
}