package com.pixelwarrior.monsters.game.world

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.pixelwarrior.monsters.data.model.*
import kotlin.random.Random

/**
 * Advanced dungeon system with multiple floors, themed areas, and special events
 * Inspired by the original Dragon Warrior Monsters dungeon structure
 */
class DungeonSystem {

    companion object {
        const val FLOORS_PER_LEVEL = 8
        const val EVENT_FLOOR_CHANCE = 0.15f
        const val WANDERING_EVENT_CHANCE = 0.08f
        const val BOSS_FLOOR_INTERVAL = 8
    }

    private val dungeons = generateDungeons()
    
    /**
     * Get all available dungeons based on player progress
     */
    fun getAvailableDungeons(playerProgress: Map<String, Boolean>): List<Dungeon> {
        return dungeons.filter { dungeon ->
            dungeon.unlockRequirement == null || playerProgress[dungeon.unlockRequirement] == true
        }
    }
    
    /**
     * Get available dungeons for a player based on their GameSave
     */
    fun getAvailableDungeons(gameSave: com.pixelwarrior.monsters.data.model.GameSave): List<Dungeon> {
        return getAvailableDungeons(gameSave.storyProgress)
    }
    
    /**
     * Enter a dungeon and get the first floor
     */
    fun enterDungeon(dungeonId: String): DungeonFloor? {
        val dungeon = dungeons.find { it.id == dungeonId } ?: return null
        return generateFloor(dungeon, 1)
    }
    
    /**
     * Proceed to the next floor in the dungeon
     */
    fun proceedToNextFloor(dungeonId: String, currentFloor: Int): DungeonFloor? {
        val dungeon = dungeons.find { it.id == dungeonId } ?: return null
        val nextFloorNumber = currentFloor + 1
        
        // Check if this is the final floor
        if (nextFloorNumber > dungeon.maxFloors) return null
        
        return generateFloor(dungeon, nextFloorNumber)
    }
    
    /**
     * Generate a floor based on dungeon theme and floor number
     */
    private fun generateFloor(dungeon: Dungeon, floorNumber: Int): DungeonFloor {
        val isBossFloor = floorNumber % BOSS_FLOOR_INTERVAL == 0
        val isEventFloor = !isBossFloor && Random.nextFloat() < EVENT_FLOOR_CHANCE
        
        return when {
            isBossFloor -> generateBossFloor(dungeon, floorNumber)
            isEventFloor -> generateEventFloor(dungeon, floorNumber)
            else -> generateRegularFloor(dungeon, floorNumber)
        }
    }
    
    /**
     * Generate a regular exploration floor
     */
    private fun generateRegularFloor(dungeon: Dungeon, floorNumber: Int): DungeonFloor {
        val encounters = selectEncountersForFloor(dungeon, floorNumber)
        val wanderingEvents = generateWanderingEvents(dungeon.theme)
        
        return DungeonFloor(
            dungeonId = dungeon.id,
            floorNumber = floorNumber,
            type = FloorType.REGULAR,
            theme = dungeon.theme,
            description = "${dungeon.name} - Floor $floorNumber\n${getFloorDescription(dungeon.theme, floorNumber)}",
            encounterRate = dungeon.baseEncounterRate + (floorNumber * 0.02f),
            possibleEncounters = encounters,
            wanderingEvents = wanderingEvents,
            exitCount = if (floorNumber == dungeon.maxFloors) 1 else Random.nextInt(2, 4),
            hasStairs = floorNumber < dungeon.maxFloors,
            specialFeatures = generateSpecialFeatures(dungeon.theme, floorNumber)
        )
    }
    
    /**
     * Generate a boss floor with powerful encounters
     */
    private fun generateBossFloor(dungeon: Dungeon, floorNumber: Int): DungeonFloor {
        val bossEncounters = selectBossEncounters(dungeon, floorNumber)
        
        return DungeonFloor(
            dungeonId = dungeon.id,
            floorNumber = floorNumber,
            type = FloorType.BOSS,
            theme = dungeon.theme,
            description = "${dungeon.name} - Boss Floor $floorNumber\n${getBossFloorDescription(dungeon.theme)}",
            encounterRate = 1.0f, // Guaranteed boss encounter
            possibleEncounters = bossEncounters,
            wanderingEvents = emptyList(),
            exitCount = 1,
            hasStairs = floorNumber < dungeon.maxFloors,
            specialFeatures = listOf("boss_arena", "treasure_chest", "healing_spring")
        )
    }
    
    /**
     * Generate a special event floor with unique mechanics
     */
    private fun generateEventFloor(dungeon: Dungeon, floorNumber: Int): DungeonFloor {
        val eventType = selectRandomEvent(dungeon.theme)
        
        return DungeonFloor(
            dungeonId = dungeon.id,
            floorNumber = floorNumber,
            type = FloorType.EVENT,
            theme = dungeon.theme,
            description = "${dungeon.name} - ${eventType.name}\n${eventType.description}",
            encounterRate = eventType.encounterRate,
            possibleEncounters = eventType.specialEncounters,
            wanderingEvents = emptyList(),
            exitCount = eventType.exitCount,
            hasStairs = true,
            specialFeatures = eventType.features,
            eventType = eventType
        )
    }
    
    /**
     * Select appropriate encounters based on dungeon theme and floor depth
     */
    private fun selectEncountersForFloor(dungeon: Dungeon, floorNumber: Int): List<String> {
        val baseEncounters = dungeon.themeEncounters
        val floorDifficulty = (floorNumber - 1) / 3 // Groups floors into difficulty tiers
        
        return baseEncounters.mapNotNull { encounterId ->
            val encounterLevel = getEncounterDifficultyLevel(encounterId)
            if (encounterLevel <= floorDifficulty + 1) encounterId else null
        }
    }
    
    /**
     * Generate wandering events that can occur on regular floors
     */
    private fun generateWanderingEvents(theme: DungeonTheme): List<WanderingEvent> {
        val events = mutableListOf<WanderingEvent>()
        
        // Theme-specific wandering events
        when (theme) {
            DungeonTheme.FOREST -> {
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("fairy_ring", "You discover a ring of mushrooms", EventOutcome.HEALING))
                }
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("ancient_tree", "An ancient tree offers wisdom", EventOutcome.EXPERIENCE))
                }
            }
            DungeonTheme.VOLCANIC -> {
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("lava_pool", "A bubbling lava pool blocks your path", EventOutcome.DAMAGE))
                }
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("fire_crystal", "A glowing crystal radiates heat", EventOutcome.FIRE_BOOST))
                }
            }
            DungeonTheme.ICE -> {
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("frozen_fountain", "A frozen fountain sparkles in the light", EventOutcome.MP_RESTORE))
                }
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("ice_storm", "A sudden ice storm strikes!", EventOutcome.FREEZE))
                }
            }
            DungeonTheme.RUINS -> {
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("ancient_inscription", "Ancient runes glow with power", EventOutcome.STAT_BOOST))
                }
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("treasure_chamber", "A hidden chamber reveals treasures", EventOutcome.TREASURE))
                }
            }
            DungeonTheme.UNDERWATER -> {
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("air_bubble", "You find a pocket of air", EventOutcome.OXYGEN_RESTORE))
                }
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("coral_garden", "Beautiful corals sparkle around you", EventOutcome.WATER_BOOST))
                }
            }
            DungeonTheme.SKY -> {
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("wind_current", "Strong winds lift your spirits", EventOutcome.AGILITY_BOOST))
                }
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("cloud_shrine", "A shrine floating on clouds", EventOutcome.BLESSING))
                }
            }
            DungeonTheme.DESERT -> {
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("oasis", "A refreshing oasis appears", EventOutcome.FULL_HEALING))
                }
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("sandstorm", "A fierce sandstorm approaches", EventOutcome.CONFUSION))
                }
            }
            DungeonTheme.CRYSTAL_CAVE -> {
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("crystal_resonance", "Crystals sing in harmony", EventOutcome.MAGIC_BOOST))
                }
                if (Random.nextFloat() < WANDERING_EVENT_CHANCE) {
                    events.add(WanderingEvent("gem_vein", "Precious gems glitter in the walls", EventOutcome.GOLD))
                }
            }
        }
        
        return events
    }
    
    /**
     * Get all dungeons with their themes and progression
     */
    private fun generateDungeons(): List<Dungeon> {
        return listOf(
            Dungeon(
                id = "beginner_forest",
                name = "Whispering Woods",
                theme = DungeonTheme.FOREST,
                description = "A peaceful forest dungeon perfect for new tamers",
                maxFloors = 16,
                baseEncounterRate = 0.15f,
                unlockRequirement = null,
                themeEncounters = listOf("leaf_sprite", "forest_slime", "wood_golem", "tree_guardian", "fairy_queen")
            ),
            Dungeon(
                id = "molten_depths",
                name = "Molten Core Depths",
                theme = DungeonTheme.VOLCANIC,
                description = "Scorching caverns filled with fire monsters",
                maxFloors = 20,
                baseEncounterRate = 0.20f,
                unlockRequirement = "forest_complete",
                themeEncounters = listOf("fire_imp", "lava_serpent", "magma_golem", "flame_dragon", "volcano_lord")
            ),
            Dungeon(
                id = "frozen_palace",
                name = "Eternal Ice Palace",
                theme = DungeonTheme.ICE,
                description = "A frozen palace where ice monsters reign",
                maxFloors = 18,
                baseEncounterRate = 0.18f,
                unlockRequirement = "fire_key_obtained",
                themeEncounters = listOf("ice_sprite", "frost_wolf", "crystal_guardian", "ice_dragon", "winter_king")
            ),
            Dungeon(
                id = "lost_ruins",
                name = "Ancient Lost Ruins",
                theme = DungeonTheme.RUINS,
                description = "Mysterious ruins hiding powerful ancient monsters",
                maxFloors = 24,
                baseEncounterRate = 0.25f,
                unlockRequirement = "ice_crown_found",
                themeEncounters = listOf("stone_guardian", "ruin_specter", "ancient_golem", "shadow_knight", "ruin_emperor")
            ),
            Dungeon(
                id = "abyssal_depths",
                name = "Abyssal Ocean Depths",
                theme = DungeonTheme.UNDERWATER,
                description = "Deep underwater trenches with mysterious sea creatures",
                maxFloors = 22,
                baseEncounterRate = 0.22f,
                unlockRequirement = "ancient_key_acquired",
                themeEncounters = listOf("bubble_fish", "sea_serpent", "coral_beast", "leviathan", "ocean_god")
            ),
            Dungeon(
                id = "celestial_tower",
                name = "Celestial Sky Tower",
                theme = DungeonTheme.SKY,
                description = "A tower reaching into the heavens with flying monsters",
                maxFloors = 30,
                baseEncounterRate = 0.20f,
                unlockRequirement = "sea_blessing_received",
                themeEncounters = listOf("wind_wisp", "sky_serpent", "storm_eagle", "thunder_dragon", "sky_emperor")
            ),
            Dungeon(
                id = "mirage_desert",
                name = "Endless Mirage Desert",
                theme = DungeonTheme.DESERT,
                description = "A vast desert with shifting sands and sand monsters",
                maxFloors = 20,
                baseEncounterRate = 0.17f,
                unlockRequirement = "wind_blessing_earned",
                themeEncounters = listOf("sand_sprite", "desert_wolf", "dune_crawler", "sand_dragon", "desert_pharaoh")
            ),
            Dungeon(
                id = "rainbow_caverns",
                name = "Rainbow Crystal Caverns",
                theme = DungeonTheme.CRYSTAL_CAVE,
                description = "Brilliant caverns filled with crystal monsters and gems",
                maxFloors = 32,
                baseEncounterRate = 0.30f,
                unlockRequirement = "all_blessings_complete",
                themeEncounters = listOf("crystal_sprite", "gem_golem", "prism_beast", "diamond_dragon", "crystal_god")
            )
        )
    }
    
    // Helper functions for floor generation
    
    private fun getFloorDescription(theme: DungeonTheme, floorNumber: Int): String {
        val depth = when (floorNumber) {
            in 1..4 -> "shallow"
            in 5..12 -> "middle"
            in 13..20 -> "deep"
            else -> "deepest"
        }
        
        return when (theme) {
            DungeonTheme.FOREST -> "The $depth forest floor is covered in moss and fallen leaves."
            DungeonTheme.VOLCANIC -> "The $depth volcanic chamber pulses with molten heat."
            DungeonTheme.ICE -> "The $depth ice cavern sparkles with frozen crystals."
            DungeonTheme.RUINS -> "The $depth ruins echo with ancient mysteries."
            DungeonTheme.UNDERWATER -> "The $depth ocean floor is shrouded in darkness."
            DungeonTheme.SKY -> "The $depth sky level drifts among the clouds."
            DungeonTheme.DESERT -> "The $depth desert chamber is filled with shifting sand."
            DungeonTheme.CRYSTAL_CAVE -> "The $depth crystal chamber glows with rainbow light."
        }
    }
    
    private fun getBossFloorDescription(theme: DungeonTheme): String {
        return when (theme) {
            DungeonTheme.FOREST -> "A massive clearing where the forest guardian awaits."
            DungeonTheme.VOLCANIC -> "A lava-filled chamber where the volcano lord rules."
            DungeonTheme.ICE -> "A frozen throne room of the ice monarch."
            DungeonTheme.RUINS -> "An ancient arena where shadows gather."
            DungeonTheme.UNDERWATER -> "The deepest trench where ancient beasts slumber."
            DungeonTheme.SKY -> "A floating platform high above the clouds."
            DungeonTheme.DESERT -> "An oasis palace hidden in the endless sands."
            DungeonTheme.CRYSTAL_CAVE -> "A cathedral of crystals resonating with power."
        }
    }
    
    private fun selectBossEncounters(dungeon: Dungeon, floorNumber: Int): List<String> {
        // Return the most powerful monsters for boss floors
        return dungeon.themeEncounters.takeLast(2)
    }
    
    private fun selectRandomEvent(theme: DungeonTheme): EventFloor {
        val events = when (theme) {
            DungeonTheme.FOREST -> listOf(
                EventFloor("monster_village", "Monster Village", "A peaceful village of friendly monsters", 0.0f, listOf("village_elder"), 2, listOf("shop", "inn", "village_quest")),
                EventFloor("breeding_ground", "Natural Breeding Ground", "A magical grove perfect for monster breeding", 0.1f, listOf("breeding_helper"), 1, listOf("breeding_bonus", "egg_incubator")),
                EventFloor("wisdom_tree", "Tree of Wisdom", "An ancient tree that grants knowledge", 0.0f, emptyList(), 1, listOf("skill_teacher", "stat_boost_all"))
            )
            DungeonTheme.VOLCANIC -> listOf(
                EventFloor("forge_chamber", "Ancient Forge", "A magical forge for enhancing equipment", 0.05f, listOf("fire_smith"), 1, listOf("weapon_upgrade", "fire_immunity")),
                EventFloor("lava_springs", "Healing Lava Springs", "Surprisingly healing volcanic springs", 0.0f, emptyList(), 1, listOf("full_healing", "fire_resistance")),
                EventFloor("trial_by_fire", "Trial by Fire", "A challenging fire-based trial", 0.8f, listOf("fire_trial_boss"), 1, listOf("fire_mastery", "rare_treasure"))
            )
            DungeonTheme.ICE -> listOf(
                EventFloor("ice_sanctuary", "Frozen Sanctuary", "A serene ice temple", 0.0f, listOf("ice_priestess"), 1, listOf("blessing_ice", "mp_boost_all")),
                EventFloor("crystal_maze", "Crystal Maze", "A maze of reflecting ice crystals", 0.3f, listOf("mirror_monster"), 3, listOf("navigation_puzzle", "illusion_immunity")),
                EventFloor("frozen_library", "Frozen Library", "A library preserved in ice", 0.0f, emptyList(), 1, listOf("rare_skills", "ancient_knowledge"))
            )
            DungeonTheme.RUINS -> listOf(
                EventFloor("treasure_vault", "Ancient Treasure Vault", "A vault filled with ancient treasures", 0.4f, listOf("vault_guardian"), 1, listOf("legendary_items", "ancient_gold")),
                EventFloor("spirit_council", "Council of Ancient Spirits", "Ghostly spirits offer guidance", 0.0f, listOf("wise_spirit"), 1, listOf("prophecy", "stat_redistribution")),
                EventFloor("time_chamber", "Temporal Chamber", "A room where time flows differently", 0.2f, listOf("time_guardian"), 1, listOf("experience_bonus", "age_acceleration"))
            )
            DungeonTheme.UNDERWATER -> listOf(
                EventFloor("sunken_ship", "Sunken Treasure Ship", "A ship full of underwater treasures", 0.6f, listOf("ghost_pirate"), 2, listOf("pirate_treasure", "diving_gear")),
                EventFloor("coral_garden", "Living Coral Garden", "A beautiful garden of living coral", 0.1f, listOf("coral_keeper"), 1, listOf("water_blessing", "healing_coral")),
                EventFloor("mermaid_palace", "Mermaid Palace", "An underwater palace of mermaids", 0.0f, listOf("mermaid_queen"), 1, listOf("water_mastery", "pearl_of_wisdom"))
            )
            DungeonTheme.SKY -> listOf(
                EventFloor("cloud_city", "Floating Cloud City", "A city built on the clouds", 0.1f, listOf("sky_citizen"), 2, listOf("wind_power", "flight_training")),
                EventFloor("storm_center", "Eye of the Storm", "The calm center of a great storm", 0.7f, listOf("storm_lord"), 1, listOf("lightning_mastery", "storm_immunity")),
                EventFloor("star_observatory", "Celestial Observatory", "An observatory watching the stars", 0.0f, listOf("star_sage"), 1, listOf("cosmic_knowledge", "stellar_blessing"))
            )
            DungeonTheme.DESERT -> listOf(
                EventFloor("oasis_town", "Hidden Oasis Town", "A town built around a magical oasis", 0.0f, listOf("oasis_guardian"), 2, listOf("desert_survival", "water_blessing")),
                EventFloor("pyramid_chamber", "Pyramid Inner Chamber", "The heart of an ancient pyramid", 0.5f, listOf("mummy_lord"), 1, listOf("pharaoh_treasure", "curse_immunity")),
                EventFloor("mirage_realm", "Mirage Realm", "A realm of illusions and mirages", 0.3f, listOf("mirage_master"), 3, listOf("illusion_mastery", "reality_sight"))
            )
            DungeonTheme.CRYSTAL_CAVE -> listOf(
                EventFloor("crystal_heart", "Crystal Heart Chamber", "The pulsing heart of the crystal cave", 0.0f, listOf("crystal_spirit"), 1, listOf("crystal_evolution", "prismatic_power")),
                EventFloor("gem_workshop", "Master Gem Workshop", "A workshop for crafting magical gems", 0.1f, listOf("gem_crafter"), 1, listOf("gem_crafting", "crystal_enhancement")),
                EventFloor("rainbow_portal", "Rainbow Portal", "A portal to other dimensions", 0.9f, listOf("portal_guardian"), 1, listOf("dimensional_travel", "rainbow_blessing"))
            )
        }
        return events.random()
    }
    
    private fun generateSpecialFeatures(theme: DungeonTheme, floorNumber: Int): List<String> {
        val baseFeatures = mutableListOf<String>()
        
        // Common features
        if (Random.nextFloat() < 0.3f) baseFeatures.add("treasure_chest")
        if (Random.nextFloat() < 0.2f) baseFeatures.add("healing_spring")
        if (floorNumber > 5 && Random.nextFloat() < 0.15f) baseFeatures.add("monster_nest")
        
        // Theme-specific features
        when (theme) {
            DungeonTheme.FOREST -> {
                if (Random.nextFloat() < 0.2f) baseFeatures.add("mushroom_circle")
                if (Random.nextFloat() < 0.15f) baseFeatures.add("ancient_tree")
            }
            DungeonTheme.VOLCANIC -> {
                if (Random.nextFloat() < 0.25f) baseFeatures.add("lava_pool")
                if (Random.nextFloat() < 0.1f) baseFeatures.add("fire_crystal")
            }
            DungeonTheme.ICE -> {
                if (Random.nextFloat() < 0.2f) baseFeatures.add("ice_formation")
                if (Random.nextFloat() < 0.1f) baseFeatures.add("frozen_waterfall")
            }
            DungeonTheme.RUINS -> {
                if (Random.nextFloat() < 0.3f) baseFeatures.add("ancient_altar")
                if (Random.nextFloat() < 0.2f) baseFeatures.add("runic_inscription")
            }
            DungeonTheme.UNDERWATER -> {
                if (Random.nextFloat() < 0.2f) baseFeatures.add("air_pocket")
                if (Random.nextFloat() < 0.15f) baseFeatures.add("coral_formation")
            }
            DungeonTheme.SKY -> {
                if (Random.nextFloat() < 0.15f) baseFeatures.add("wind_current")
                if (Random.nextFloat() < 0.1f) baseFeatures.add("floating_platform")
            }
            DungeonTheme.DESERT -> {
                if (Random.nextFloat() < 0.1f) baseFeatures.add("hidden_oasis")
                if (Random.nextFloat() < 0.2f) baseFeatures.add("quicksand")
            }
            DungeonTheme.CRYSTAL_CAVE -> {
                if (Random.nextFloat() < 0.3f) baseFeatures.add("crystal_formation")
                if (Random.nextFloat() < 0.15f) baseFeatures.add("gem_vein")
            }
        }
        
        return baseFeatures
    }
    
    private fun getEncounterDifficultyLevel(encounterId: String): Int {
        // Simple difficulty mapping based on monster name patterns
        return when {
            encounterId.contains("sprite") || encounterId.contains("imp") -> 0
            encounterId.contains("wolf") || encounterId.contains("serpent") -> 1
            encounterId.contains("golem") || encounterId.contains("guardian") -> 2
            encounterId.contains("dragon") || encounterId.contains("lord") -> 3
            encounterId.contains("god") || encounterId.contains("emperor") -> 4
            else -> 1
        }
    }
}

/**
 * Data classes for the enhanced dungeon system
 */

@Parcelize
data class Dungeon(
    val id: String,
    val name: String,
    val theme: DungeonTheme,
    val description: String,
    val maxFloors: Int,
    val baseEncounterRate: Float,
    val unlockRequirement: String?,
    val themeEncounters: List<String>
) : Parcelable

enum class DungeonTheme {
    FOREST, VOLCANIC, ICE, RUINS, UNDERWATER, SKY, DESERT, CRYSTAL_CAVE
}

@Parcelize
data class DungeonFloor(
    val dungeonId: String,
    val floorNumber: Int,
    val type: FloorType,
    val theme: DungeonTheme,
    val description: String,
    val encounterRate: Float,
    val possibleEncounters: List<String>,
    val wanderingEvents: List<WanderingEvent>,
    val exitCount: Int,
    val hasStairs: Boolean,
    val specialFeatures: List<String>,
    val eventType: EventFloor? = null
) : Parcelable

enum class FloorType {
    REGULAR, BOSS, EVENT
}

@Parcelize
data class WanderingEvent(
    val id: String,
    val description: String,
    val outcome: EventOutcome
) : Parcelable

@Parcelize
data class EventFloor(
    val id: String,
    val name: String,
    val description: String,
    val encounterRate: Float,
    val specialEncounters: List<String>,
    val exitCount: Int,
    val features: List<String>
) : Parcelable

enum class EventOutcome {
    HEALING, DAMAGE, EXPERIENCE, TREASURE, STAT_BOOST, FIRE_BOOST, MP_RESTORE, 
    FREEZE, OXYGEN_RESTORE, WATER_BOOST, AGILITY_BOOST, BLESSING, FULL_HEALING, 
    CONFUSION, MAGIC_BOOST, GOLD
}