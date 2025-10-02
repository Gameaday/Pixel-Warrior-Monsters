package com.pixelwarrior.monsters.data.model

/**
 * Represents breeding compatibility assessment between two monsters
 */
data class BreedingCompatibility(
    val isCompatible: Boolean,
    val compatibilityScore: Int, // 0-100
    val reason: String,
    val recommendedLevel: Int? = null,
    val potentialOffspring: List<String> = emptyList()
)
