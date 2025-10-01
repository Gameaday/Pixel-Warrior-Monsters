package com.pixelwarrior.monsters

import com.pixelwarrior.monsters.data.model.*

/**
 * Test helper functions for creating test data
 */

/**
 * Create a test Monster with reasonable defaults
 */
fun createTestMonster(
    id: String = "test_monster_1",
    speciesId: String = "test_species",
    name: String = "TestMon",
    type1: MonsterType = MonsterType.NORMAL,
    type2: MonsterType? = null,
    family: MonsterFamily = MonsterFamily.BEAST,
    level: Int = 10,
    currentHp: Int = 100,
    currentMp: Int = 50,
    experience: Long = 0L,
    experienceToNext: Long = 1000L,
    attack: Int = 50,
    defense: Int = 40,
    agility: Int = 45,
    magic: Int = 30,
    wisdom: Int = 35,
    maxHp: Int = 100,
    maxMp: Int = 50,
    skills: List<String> = listOf("tackle"),
    traits: List<String> = emptyList(),
    isWild: Boolean = false,
    captureRate: Int = 100,
    growthRate: GrowthRate = GrowthRate.MEDIUM_FAST,
    affection: Int = 50,
    personality: Personality = Personality.NONE
): Monster {
    val baseStats = MonsterStats(
        attack = attack,
        defense = defense,
        agility = agility,
        magic = magic,
        wisdom = wisdom,
        maxHp = maxHp,
        maxMp = maxMp
    )
    
    return Monster(
        id = id,
        speciesId = speciesId,
        name = name,
        type1 = type1,
        type2 = type2,
        family = family,
        level = level,
        currentHp = currentHp,
        currentMp = currentMp,
        experience = experience,
        experienceToNext = experienceToNext,
        baseStats = baseStats,
        currentStats = baseStats,
        skills = skills,
        traits = traits,
        isWild = isWild,
        captureRate = captureRate,
        growthRate = growthRate,
        affection = affection,
        personality = personality
    )
}

/**
 * Create a test MonsterStats with reasonable defaults
 */
fun createTestStats(
    attack: Int = 50,
    defense: Int = 40,
    agility: Int = 45,
    magic: Int = 30,
    wisdom: Int = 35,
    maxHp: Int = 100,
    maxMp: Int = 50
): MonsterStats {
    return MonsterStats(
        attack = attack,
        defense = defense,
        agility = agility,
        magic = magic,
        wisdom = wisdom,
        maxHp = maxHp,
        maxMp = maxMp
    )
}
