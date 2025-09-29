package com.pixelwarrior.monsters.game.tournament

import com.pixelwarrior.monsters.data.model.Monster
import com.pixelwarrior.monsters.data.model.MonsterType
import com.pixelwarrior.monsters.data.model.MonsterFamily
import com.pixelwarrior.monsters.data.model.MonsterStats
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Tournament and Competition System for arena battles, rival trainers, and seasonal events
 * Implements Phase 3 of the roadmap with authentic DQM-style tournaments
 */

data class TournamentTier(
    val name: String,
    val entryFee: Int,
    val prizePool: Int,
    val minLevel: Int,
    val maxLevel: Int,
    val description: String
) {
    companion object {
        val ROOKIE = TournamentTier("Rookie Cup", 50, 200, 1, 15, "For beginning trainers")
        val BRONZE = TournamentTier("Bronze League", 100, 500, 10, 25, "Intermediate competition")
        val SILVER = TournamentTier("Silver Championship", 250, 1000, 20, 35, "Advanced trainers only")
        val GOLD = TournamentTier("Gold Masters", 500, 2500, 30, 45, "Elite tournament")
        val MASTER = TournamentTier("Master's Crown", 1000, 5000, 40, 50, "Ultimate championship")
        
        val ALL_TIERS = listOf(ROOKIE, BRONZE, SILVER, GOLD, MASTER)
    }
}

data class RivalTrainer(
    val id: String,
    val name: String,
    val title: String,
    val preferredType: MonsterType,
    val difficulty: Int, // 1-10 scale
    val personality: String,
    val team: List<Monster>,
    val winRate: Float = 0.65f,
    val signature: String
) {
    companion object {
        fun createRivalTrainers(): List<RivalTrainer> {
            return listOf(
                RivalTrainer("elena", "Elena", "Fire Tamer", MonsterType.FIRE, 3, "Aggressive", 
                    createTeam(MonsterType.FIRE, 18), signature = "Always leads with fire attacks"),
                RivalTrainer("marcus", "Marcus", "Water Guardian", MonsterType.WATER, 4, "Defensive", 
                    createTeam(MonsterType.WATER, 22), signature = "Focuses on healing and defense"),
                RivalTrainer("aria", "Aria", "Wind Dancer", MonsterType.FLYING, 5, "Swift", 
                    createTeam(MonsterType.FLYING, 25), signature = "Emphasizes speed and evasion"),
                RivalTrainer("zane", "Zane", "Earth Shaker", MonsterType.GROUND, 6, "Sturdy", 
                    createTeam(MonsterType.GROUND, 28), signature = "Builds defensive walls"),
                RivalTrainer("nova", "Nova", "Spark Master", MonsterType.ELECTRIC, 7, "Energetic", 
                    createTeam(MonsterType.ELECTRIC, 30), signature = "Paralyzes opponents first"),
                RivalTrainer("ivy", "Ivy", "Nature's Voice", MonsterType.GRASS, 5, "Calm", 
                    createTeam(MonsterType.GRASS, 26), signature = "Uses status effects creatively"),
                RivalTrainer("frost", "Frost", "Ice Queen", MonsterType.ICE, 6, "Calculating", 
                    createTeam(MonsterType.ICE, 27), signature = "Freezes enemies methodically"),
                RivalTrainer("shadow", "Shadow", "Dark Whisper", MonsterType.DARK, 8, "Mysterious", 
                    createTeam(MonsterType.DARK, 32), signature = "Unpredictable battle style"),
                RivalTrainer("luna", "Luna", "Light Bearer", MonsterType.PSYCHIC, 7, "Noble", 
                    createTeam(MonsterType.PSYCHIC, 31), signature = "Heals while dealing damage"),
                RivalTrainer("steel", "Steel", "Iron Wall", MonsterType.STEEL, 6, "Methodical", 
                    createTeam(MonsterType.STEEL, 29), signature = "Outlasts opponents"),
                RivalTrainer("crystal", "Crystal", "Gem Collector", MonsterType.ROCK, 8, "Precise", 
                    createTeam(MonsterType.ROCK, 33), signature = "Critical hits specialist"),
                RivalTrainer("void", "Void", "Champion of Champions", MonsterType.DARK, 10, "Legendary", 
                    createMasterTeam(), signature = "Adapts to any strategy")
            )
        }

        private fun createTeam(type: MonsterType, avgLevel: Int): List<Monster> {
            val team = mutableListOf<Monster>()
            repeat(4) { index ->
                val level = avgLevel + Random.nextInt(-3, 4)
                val baseStats = MonsterStats(
                    attack = Random.nextInt(40, 80),
                    defense = Random.nextInt(35, 75),
                    agility = Random.nextInt(30, 70),
                    magic = Random.nextInt(25, 65),
                    wisdom = Random.nextInt(25, 65),
                    maxHp = Random.nextInt(60, 120),
                    maxMp = Random.nextInt(20, 60)
                )
                val monster = Monster(
                    id = "rival_${type.name.lowercase()}_$index",
                    speciesId = "${type.name.lowercase()}_${getSpeciesName(index)}",
                    name = "${type.name.lowercase().replaceFirstChar { it.uppercase() }} ${getSpeciesName(index)}",
                    type1 = type,
                    type2 = if (Random.nextFloat() < 0.3f) MonsterType.values().random() else null,
                    family = MonsterFamily.values().random(),
                    level = level.coerceIn(1, 50),
                    currentHp = baseStats.maxHp,
                    currentMp = baseStats.maxMp,
                    experience = 0L,
                    experienceToNext = calculateExperienceToNext(level.coerceIn(1, 50)),
                    baseStats = baseStats,
                    currentStats = baseStats,
                    skills = listOf("Basic Attack", "${type.name} Strike", "Defend")
                )
                team.add(monster)
            }
            return team
        }

        private fun createMasterTeam(): List<Monster> {
            return listOf(
                // Void Dragon
                run {
                    val baseStats = MonsterStats(95, 85, 80, 90, 85, 150, 80)
                    Monster(
                        id = "void_dragon",
                        speciesId = "void_dragon_species",
                        name = "Void Dragon",
                        type1 = MonsterType.DARK,
                        type2 = MonsterType.ROCK, // Crystal -> Rock
                        family = MonsterFamily.DRAGON,
                        level = 50,
                        currentHp = baseStats.maxHp,
                        currentMp = baseStats.maxMp,
                        experience = 0,
                        experienceToNext = calculateExperienceToNext(50),
                        baseStats = baseStats,
                        currentStats = baseStats,
                        skills = listOf("Void Strike", "Crystal Beam", "Dark Heal", "Dragon Roar")
                    )
                },
                // Void Phoenix
                run {
                    val baseStats = MonsterStats(85, 70, 95, 90, 80, 130, 90)
                    Monster(
                        id = "void_phoenix",
                        speciesId = "void_phoenix_species",
                        name = "Void Phoenix",
                        type1 = MonsterType.FIRE,
                        type2 = MonsterType.FLYING, // AIR -> FLYING
                        family = MonsterFamily.BIRD,
                        level = 48,
                        currentHp = baseStats.maxHp,
                        currentMp = baseStats.maxMp,
                        experience = 0,
                        experienceToNext = calculateExperienceToNext(48),
                        baseStats = baseStats,
                        currentStats = baseStats,
                        skills = listOf("Phoenix Fire", "Wind Slash", "Regenerate", "Sky Dance")
                    )
                },
                // Void Leviathan  
                run {
                    val baseStats = MonsterStats(80, 95, 70, 85, 90, 160, 85)
                    Monster(
                        id = "void_leviathan",
                        speciesId = "void_leviathan_species",
                        name = "Void Leviathan",
                        type1 = MonsterType.WATER,
                        type2 = MonsterType.ICE,
                        family = MonsterFamily.BEAST,
                        level = 49,
                        currentHp = baseStats.maxHp,
                        currentMp = baseStats.maxMp,
                        experience = 0,
                        experienceToNext = calculateExperienceToNext(49),
                        baseStats = baseStats,
                        currentStats = baseStats,
                        skills = listOf("Tidal Wave", "Frost Armor", "Deep Heal", "Leviathan's Wrath")
                    )
                },
                // Void Titan
                run {
                    val baseStats = MonsterStats(100, 100, 60, 75, 80, 180, 70)
                    Monster(
                        id = "void_titan",
                        speciesId = "void_titan_species",
                        name = "Void Titan",
                        type1 = MonsterType.GROUND, // EARTH -> GROUND
                        type2 = MonsterType.STEEL,
                        family = MonsterFamily.MATERIAL,
                        level = 50,
                        currentHp = baseStats.maxHp,
                        currentMp = baseStats.maxMp,
                        experience = 0,
                        experienceToNext = calculateExperienceToNext(50),
                        baseStats = baseStats,
                        currentStats = baseStats,
                        skills = listOf("Earthquake", "Steel Fist", "Rock Shield", "Titan's Rage")
                    )
                }
            )
        }

        private fun getSpeciesName(index: Int): String {
            return when (index) {
                0 -> "Warrior"
                1 -> "Guardian"
                2 -> "Striker"
                3 -> "Champion"
                else -> "Fighter"
            }
        }
        
        private fun calculateExperienceToNext(level: Int): Long {
            return (level * level * level).toLong()
        }
    }
}

data class TournamentRecord(
    val playerId: String,
    val wins: Int = 0,
    val losses: Int = 0,
    val highestStreak: Int = 0,
    val currentStreak: Int = 0,
    val totalPrizes: Int = 0,
    val championTitles: Map<String, Int> = emptyMap()
) {
    val winRate: Float
        get() = if (wins + losses > 0) wins.toFloat() / (wins + losses) else 0f
    
    val totalBattles: Int
        get() = wins + losses
        
    fun addVictory(rivalId: String): TournamentRecord {
        return copy(
            wins = wins + 1,
            currentStreak = currentStreak + 1,
            highestStreak = maxOf(highestStreak, currentStreak + 1)
        )
    }
    
    fun addDefeat(rivalId: String): TournamentRecord {
        return copy(
            losses = losses + 1,
            currentStreak = 0
        )
    }
}

data class SeasonalTournament(
    val id: String,
    val name: String,
    val description: String,
    val startMonth: Int,
    val durationDays: Int,
    val entryFee: Int,
    val grandPrize: Int,
    val specialRivals: List<String>, // Rival IDs
    val restrictions: String // e.g., "Fire types only", "Level 30 max"
) {
    companion object {
        val SPRING_FESTIVAL = SeasonalTournament(
            "spring_fest", "Spring Festival Tournament", 
            "Celebrate new growth with grass and nature types",
            3, 7, 500, 10000, listOf("ivy", "nova"), "Grass types preferred"
        )
        val SUMMER_BLAZE = SeasonalTournament(
            "summer_blaze", "Summer Blaze Championship",
            "The heat is on in this fire-themed tournament",
            6, 10, 750, 15000, listOf("elena", "frost"), "Fire vs Ice theme"
        )
        val AUTUMN_HARVEST = SeasonalTournament(
            "autumn_harvest", "Autumn Harvest Cup",
            "A season of experience and wisdom",
            9, 14, 600, 12000, listOf("steel", "shadow"), "Level 35+ only"
        )
        val WINTER_CROWN = SeasonalTournament(
            "winter_crown", "Winter Crown Masters",
            "The ultimate test of skill and strategy",
            12, 21, 1000, 20000, listOf("void", "crystal"), "No restrictions"
        )
        
        val ALL_SEASONS = listOf(SPRING_FESTIVAL, SUMMER_BLAZE, AUTUMN_HARVEST, WINTER_CROWN)
    }
}

class TournamentSystem {
    private val rivals = RivalTrainer.createRivalTrainers()
    private var playerRecord = TournamentRecord("player")
    
    fun getTournamentTiers(): List<TournamentTier> = TournamentTier.ALL_TIERS
    
    fun getRivalTrainers(): List<RivalTrainer> = rivals
    
    fun getAvailableRivals(tier: TournamentTier): List<RivalTrainer> {
        return rivals.filter { rival ->
            val avgLevel = rival.team.map { it.level }.average()
            avgLevel >= tier.minLevel && avgLevel <= tier.maxLevel
        }.sortedBy { it.difficulty }
    }
    
    fun canEnterTournament(tier: TournamentTier, playerGold: Int, playerParty: List<Monster>): Boolean {
        if (playerGold < tier.entryFee) return false
        if (playerParty.isEmpty()) return false
        
        val partyLevels = playerParty.map { it.level }
        val avgLevel = partyLevels.average()
        val minLevel = partyLevels.minOrNull() ?: 0
        
        return avgLevel >= tier.minLevel && minLevel >= (tier.minLevel - 5)
    }
    
    suspend fun battleRival(playerParty: List<Monster>, rival: RivalTrainer): TournamentBattleResult {
        delay(1000) // Simulate battle time
        
        // Simple battle simulation - for now just return a basic result
        val playerAvgLevel = playerParty.map { it.level }.average()
        val rivalAvgLevel = rival.team.map { it.level }.average()
        
        val playerWins = playerAvgLevel >= rivalAvgLevel * 0.9 && Random.nextFloat() > 0.3
        
        return if (playerWins) {
            playerRecord = playerRecord.addVictory(rival.id)
            TournamentBattleResult.Victory(
                rival = rival,
                rewards = BattleRewards(
                    experience = rival.difficulty * 100, // Use difficulty instead of difficultyRating
                    gold = rival.difficulty * 50,
                    items = if (rival.difficulty >= 8) listOf("Rare Stone") else emptyList()
                )
            )
        } else {
            playerRecord = playerRecord.addDefeat(rival.id)
            TournamentBattleResult.Defeat(rival.id)
        }
    }
    
    fun completeTournament(tier: TournamentTier, victories: Int, totalRounds: Int): TournamentReward {
        val wasChampion = victories == totalRounds
        val prizeMultiplier = when {
            wasChampion -> 1.0f
            victories >= totalRounds * 0.75f -> 0.75f
            victories >= totalRounds * 0.5f -> 0.5f
            else -> 0.25f
        }
        
        val goldReward = (tier.prizePool * prizeMultiplier).toInt()
        val streakBonus = if (wasChampion) playerRecord.currentStreak * 50 else 0
        val totalReward = goldReward + streakBonus
        
        // Update player record
        playerRecord = playerRecord.copy(
            wins = playerRecord.wins + victories,
            losses = playerRecord.losses + (totalRounds - victories),
            currentStreak = if (wasChampion) playerRecord.currentStreak + 1 else 0,
            highestStreak = maxOf(playerRecord.highestStreak, 
                if (wasChampion) playerRecord.currentStreak + 1 else playerRecord.currentStreak),
            totalPrizes = playerRecord.totalPrizes + totalReward,
            championTitles = if (wasChampion) {
                playerRecord.championTitles + (tier.name to 
                    (playerRecord.championTitles[tier.name] ?: 0) + 1)
            } else playerRecord.championTitles
        )
        
        return TournamentReward(
            goldEarned = totalReward,
            title = if (wasChampion) "${tier.name} Champion" else null,
            streakBonus = streakBonus,
            wasChampion = wasChampion,
            finalRanking = if (wasChampion) 1 else (totalRounds - victories + 1)
        )
    }
    
    fun getPlayerRecord(): TournamentRecord = playerRecord
    
    fun getLeaderboard(): List<Pair<String, Float>> {
        // In a real implementation, this would fetch from a database
        // For now, return a simulated leaderboard with rival win rates
        return rivals.map { it.name to it.winRate }
            .plus("Player" to playerRecord.winRate)
            .sortedByDescending { it.second }
    }
    
    fun getCurrentSeasonalTournament(): SeasonalTournament? {
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        return SeasonalTournament.ALL_SEASONS.find { it.startMonth == currentMonth }
    }
    
    fun isSeasonalTournamentActive(): Boolean = getCurrentSeasonalTournament() != null
}

data class TournamentReward(
    val goldEarned: Int,
    val title: String?,
    val streakBonus: Int,
    val wasChampion: Boolean,
    val finalRanking: Int
)

// Tournament bracket for tracking progress
data class TournamentBracket(
    val tier: TournamentTier,
    val participants: List<String>, // Player + Rival names
    val rounds: List<TournamentRound>,
    val currentRound: Int = 0,
    val isComplete: Boolean = false
)

data class TournamentRound(
    val roundNumber: Int,
    val matches: List<TournamentMatch>
)

data class TournamentMatch(
    val participant1: String,
    val participant2: String,
    val winner: String? = null,
    val isComplete: Boolean = false
)

/**
 * Result of a tournament battle
 */
sealed class TournamentBattleResult {
    data class Victory(val rival: RivalTrainer, val rewards: BattleRewards) : TournamentBattleResult()
    data class Defeat(val rivalId: String) : TournamentBattleResult()
}

/**
 * Battle rewards
 */
data class BattleRewards(
    val experience: Int,
    val gold: Int,
    val items: List<String> = emptyList()
)