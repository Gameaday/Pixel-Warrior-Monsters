package com.pixelwarrior.monsters

import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.game.battle.BattleEngine
import com.pixelwarrior.monsters.game.breeding.BreedingSystem
import com.pixelwarrior.monsters.utils.GameUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for core game systems
 */
class GameSystemsTest {

    @Test
    fun testMonsterCreation() {
        val stats = MonsterStats(
            attack = 50,
            defense = 40,
            agility = 45,
            magic = 30,
            wisdom = 35,
            maxHp = 120,
            maxMp = 60
        )

        val monster = Monster(
            id = "test-1",
            speciesId = "test_species",
            name = "TestMon",
            type1 = MonsterType.NORMAL,
            type2 = null,
            family = MonsterFamily.BEAST,
            level = 5,
            currentHp = 120,
            currentMp = 60,
            experience = 0,
            experienceToNext = 150,
            baseStats = stats,
            currentStats = stats,
            skills = listOf("tackle"),
            traits = listOf("Hardy"),
            isWild = false,
            captureRate = 100,
            growthRate = GrowthRate.MEDIUM_FAST
        )

        assertEquals("TestMon", monster.name)
        assertEquals(5, monster.level)
        assertEquals(MonsterType.NORMAL, monster.type1)
        assertNull(monster.type2)
        assertEquals(120, monster.currentHp)
    }

    @Test
    fun testBattleEngine_DamageCalculation() {
        val attacker = createTestMonster("Attacker", level = 10, attack = 60)
        val defender = createTestMonster("Defender", level = 8, defense = 40)
        
        val skill = Skill(
            id = "test_attack",
            name = "Test Attack",
            description = "A test skill",
            type = SkillType.PHYSICAL,
            target = SkillTarget.SINGLE_ENEMY,
            mpCost = 5,
            power = 80,
            accuracy = 100
        )

        val battleEngine = BattleEngine()
        val damage = battleEngine.calculateDamage(attacker, defender, skill, true)

        assertTrue("Damage should be positive", damage > 0)
        assertTrue("Damage should be reasonable", damage < 200)
    }

    @Test
    fun testBreedingSystem_Compatibility() {
        val parent1 = createTestMonster("Parent1", family = MonsterFamily.BEAST, level = 15)
        val parent2 = createTestMonster("Parent2", family = MonsterFamily.BIRD, level = 12)
        val incompatible = createTestMonster("Incompatible", family = MonsterFamily.UNDEAD, level = 20)

        val breedingSystem = BreedingSystem()

        assertTrue("Beast and Bird should be compatible", breedingSystem.canBreed(parent1, parent2))
        assertFalse("Beast and Undead should not be compatible", breedingSystem.canBreed(parent1, incompatible))
        assertFalse("Same species should not breed", breedingSystem.canBreed(parent1, parent1))
    }

    @Test
    fun testBreedingSystem_OffspringGeneration() {
        val parent1 = createTestMonster("Parent1", family = MonsterFamily.BEAST, level = 15)
        val parent2 = createTestMonster("Parent2", family = MonsterFamily.BIRD, level = 12)

        val breedingSystem = BreedingSystem()
        val offspring = breedingSystem.breedMonsters(parent1, parent2)

        assertNotNull("Offspring should be generated", offspring)
        offspring?.let {
            assertEquals("Offspring should start at level 1", 1, it.level)
            assertTrue("Offspring should have positive stats", it.baseStats.maxHp > 0)
            assertTrue("Offspring should inherit some skills", it.skills.isNotEmpty())
        }
    }

    @Test
    fun testGameUtils_ExperienceCalculation() {
        val expForLevel5 = GameUtils.calculateExpForLevel(5, GrowthRate.MEDIUM_FAST)
        val expForLevel10 = GameUtils.calculateExpForLevel(10, GrowthRate.MEDIUM_FAST)

        assertTrue("Higher level should require more exp", expForLevel10 > expForLevel5)
        assertTrue("Experience should be positive", expForLevel5 > 0)
    }

    @Test
    fun testGameUtils_LevelUp() {
        val monster = createTestMonster("TestMon", level = 5)
        val leveledUp = GameUtils.levelUpMonster(monster)

        assertEquals("Monster should be level 6", 6, leveledUp.level)
        assertTrue("Stats should improve", leveledUp.currentStats.maxHp > monster.currentStats.maxHp)
    }

    @Test
    fun testGameUtils_CaptureRate() {
        val fullHpMonster = createTestMonster("FullHP", level = 10)
        val lowHpMonster = fullHpMonster.copy(currentHp = 10)

        val fullHpRate = GameUtils.calculateCaptureRate(fullHpMonster)
        val lowHpRate = GameUtils.calculateCaptureRate(lowHpMonster)

        assertTrue("Low HP monster should be easier to capture", lowHpRate > fullHpRate)
        assertTrue("Capture rate should be between 0 and 1", fullHpRate in 0.0f..1.0f)
        assertTrue("Capture rate should be between 0 and 1", lowHpRate in 0.0f..1.0f)
    }

    @Test
    fun testMonsterExtensions() {
        val monster = createTestMonster("TestMon", level = 5, currentHp = 60, maxHp = 120)

        assertEquals("HP percentage should be 50%", 0.5f, monster.getHpPercentage(), 0.01f)
        assertFalse("Monster with HP should not be fainted", monster.isFainted())
        assertEquals("Display name should include level", "TestMon (Lv.5)", monster.getDisplayName())

        val faintedMonster = monster.copy(currentHp = 0)
        assertTrue("Monster with 0 HP should be fainted", faintedMonster.isFainted())
    }

    @Test
    fun testValidation() {
        assertTrue("Valid name should pass", GameUtils.isValidMonsterName("Buddy"))
        assertTrue("Name with spaces should pass", GameUtils.isValidMonsterName("Fire Dragon"))
        assertFalse("Empty name should fail", GameUtils.isValidMonsterName(""))
        assertFalse("Too long name should fail", GameUtils.isValidMonsterName("VeryLongMonsterNameThatExceedsLimit"))
    }

    // Helper function to create test monsters
    private fun createTestMonster(
        name: String,
        level: Int = 5,
        family: MonsterFamily = MonsterFamily.BEAST,
        attack: Int = 50,
        defense: Int = 40,
        currentHp: Int = 120,
        maxHp: Int = 120
    ): Monster {
        val stats = MonsterStats(attack, defense, 45, 30, 35, maxHp, 60)
        return Monster(
            id = "test-${name.lowercase()}",
            speciesId = "test_species",
            name = name,
            type1 = MonsterType.NORMAL,
            type2 = null,
            family = family,
            level = level,
            currentHp = currentHp,
            currentMp = 60,
            experience = 0,
            experienceToNext = 150,
            baseStats = stats,
            currentStats = stats,
            skills = listOf("tackle"),
            traits = listOf("Hardy"),
            isWild = false,
            captureRate = 100,
            growthRate = GrowthRate.MEDIUM_FAST
        )
    }
}