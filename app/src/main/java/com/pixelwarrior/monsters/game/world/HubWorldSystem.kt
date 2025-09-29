package com.pixelwarrior.monsters.game.world

import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.game.story.StorySystem

/**
 * Central hub world system with progressive story unlocks
 * Manages the Master's Sanctuary and access to different game areas
 * Integrated with StorySystem for proper progression gates
 */
class HubWorldSystem(private val storySystem: StorySystem) {
    
    /**
     * Hub areas that unlock as the player progresses
     */
    enum class HubArea(
        val id: String,
        val displayName: String,
        val description: String,
        val requiredKeyItem: String? = null,
        val requiredStoryProgress: String? = null
    ) {
        MAIN_HALL("main_hall", "Main Hall", "Central meeting area with the Master"),
        MONSTER_LIBRARY("library", "Monster Library", "Study monster information and breeding compatibility", requiredStoryProgress = "first_capture"),
        BREEDING_LAB("breeding_lab", "Breeding Laboratory", "Advanced breeding facilities", requiredKeyItem = "breeder_license"),
        BATTLE_ARENA("arena", "Battle Arena", "Compete in tournaments", requiredKeyItem = "novice_badge"),
        SYNTHESIS_LAB("synthesis", "Synthesis Laboratory", "Combine monsters into new forms", requiredStoryProgress = "completed_first_dungeon"),
        ITEM_SHOP("shop", "Item Shop", "Purchase tools and supplies", requiredStoryProgress = "earned_gold"),
        GATE_CHAMBER("gates", "Gate Chamber", "Access to different worlds", requiredKeyItem = "explorer_compass"),
        MASTER_QUARTERS("quarters", "Master's Quarters", "Private area with advanced secrets", requiredKeyItem = "master_key"),
        SECRET_VAULT("vault", "Secret Vault", "Hidden treasures and legendary monsters", requiredKeyItem = "ancient_relic")
    }
    
    /**
     * NPCs that populate the hub world
     */
    enum class HubNPC(
        val id: String,
        val displayName: String,
        val title: String,
        val location: HubArea,
        val dialogue: List<String>
    ) {
        MASTER("master", "Master Teto", "Monster Master", HubArea.MAIN_HALL, listOf(
            "Welcome to the Monster Sanctuary, young trainer!",
            "Your journey to become a Monster Master begins here.",
            "Train hard, breed wisely, and remember - monsters are partners, not tools."
        )),
        LIBRARIAN("librarian", "Scholar Maya", "Monster Researcher", HubArea.MONSTER_LIBRARY, listOf(
            "Knowledge is the key to successful monster training.",
            "Each monster family has unique breeding patterns.",
            "Study your monsters well, and they will serve you faithfully."
        )),
        STABLE_KEEPER("keeper", "Keeper Jonas", "Monster Caretaker", HubArea.BREEDING_LAB, listOf(
            "A healthy monster is a happy monster!",
            "Proper breeding takes patience and understanding.",
            "I've cared for monsters for over 30 years."
        )),
        ARENA_MASTER("arena_master", "Champion Rica", "Battle Arena Manager", HubArea.BATTLE_ARENA, listOf(
            "Think you're ready for tournament battle?",
            "Only the strongest trainers earn their badges here.",
            "Show me what you and your monsters can do!"
        )),
        SYNTHESIS_EXPERT("synthesis", "Dr. Kaine", "Synthesis Researcher", HubArea.SYNTHESIS_LAB, listOf(
            "Monster synthesis is a delicate art.",
            "Two compatible monsters can become something greater.",
            "But beware - the process is irreversible!"
        )),
        MERCHANT("merchant", "Trader Finn", "Item Merchant", HubArea.ITEM_SHOP, listOf(
            "Welcome to my shop! Quality goods for monster trainers.",
            "Healing herbs, capture tools, breeding supplements - I have it all!",
            "Gold well spent is an investment in victory."
        )),
        GATE_KEEPER("gate_keeper", "Guardian Zara", "Gate Keeper", HubArea.GATE_CHAMBER, listOf(
            "These gates lead to wondrous and dangerous realms.",
            "Each world holds unique monsters and challenges.",
            "Are you prepared for what lies beyond?"
        ))
    }
    
    /**
     * Key items that unlock story progression and new areas
     */
    enum class KeyItem(
        val id: String,
        val displayName: String,
        val description: String,
        val unlocks: List<HubArea>
    ) {
        BREEDER_LICENSE("breeder_license", "Breeder's License", "Official permission to use advanced breeding facilities", 
            listOf(HubArea.BREEDING_LAB)),
        NOVICE_BADGE("novice_badge", "Novice Badge", "Proof of tournament participation eligibility", 
            listOf(HubArea.BATTLE_ARENA)),
        EXPLORER_COMPASS("explorer_compass", "Explorer's Compass", "Reveals hidden passages and gates", 
            listOf(HubArea.GATE_CHAMBER)),
        MASTER_KEY("master_key", "Master's Key", "Access to restricted sanctuary areas", 
            listOf(HubArea.MASTER_QUARTERS)),
        ANCIENT_RELIC("ancient_relic", "Ancient Relic", "Mysterious artifact of immense power", 
            listOf(HubArea.SECRET_VAULT)),
        SYNTHESIS_PERMIT("synthesis_permit", "Synthesis Permit", "Authorization for monster synthesis experiments", 
            listOf(HubArea.SYNTHESIS_LAB))
    }
    
    /**
     * Story progress milestones
     */
    enum class StoryMilestone(
        val id: String,
        val displayName: String,
        val description: String,
        val unlocks: List<HubArea>
    ) {
        FIRST_CAPTURE("first_capture", "First Monster Captured", "Successfully captured your first wild monster", 
            listOf(HubArea.MONSTER_LIBRARY)),
        EARNED_GOLD("earned_gold", "First Earnings", "Earned gold from battle victories", 
            listOf(HubArea.ITEM_SHOP)),
        COMPLETED_FIRST_DUNGEON("completed_first_dungeon", "Dungeon Explorer", "Completed your first dungeon floor", 
            listOf(HubArea.SYNTHESIS_LAB)),
        FIRST_BREEDING("first_breeding", "Successful Breeder", "Successfully bred your first monster", 
            listOf()),
        TOURNAMENT_WINNER("tournament_winner", "Tournament Champion", "Won your first tournament battle", 
            listOf()),
        MASTER_RANK("master_rank", "Master Rank Achieved", "Achieved the rank of Monster Master", 
            listOf(HubArea.SECRET_VAULT))
    }
    
    /**
     * Hub world facilities and their functions
     */
    data class HubFacility(
        val area: HubArea,
        val npc: HubNPC?,
        val isUnlocked: Boolean,
        val functions: List<FacilityFunction>
    )
    
    enum class FacilityFunction {
        MONSTER_INFO,      // View monster details and breeding compatibility
        MONSTER_STORAGE,   // Store/retrieve monsters from farm
        TOURNAMENT_BATTLE, // Participate in arena tournaments
        MONSTER_SYNTHESIS, // Combine monsters
        ITEM_PURCHASE,     // Buy items and tools
        WORLD_TRAVEL,      // Access dungeon portals
        STORY_DIALOGUE,    // Interact with NPCs for story
        MONSTER_HEALING    // Heal party monsters
    }
    
    /**
     * Check if a hub area is unlocked based on player progress
     */
    fun isAreaUnlocked(area: HubArea, playerSave: GameSave): Boolean {
        // Check key item requirement
        area.requiredKeyItem?.let { requiredItem ->
            if (!playerSave.inventory.containsKey(requiredItem) || 
                playerSave.inventory[requiredItem] == 0) {
                return false
            }
        }
        
        // Check story progress requirement
        area.requiredStoryProgress?.let { storyFlag ->
            if (!playerSave.storyProgress.containsKey(storyFlag) || 
                playerSave.storyProgress[storyFlag] != true) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Get all currently unlocked hub areas
     */
    fun getUnlockedAreas(playerSave: GameSave): List<HubArea> {
        return HubArea.values().filter { area ->
            isAreaUnlocked(area, playerSave)
        }
    }
    
    /**
     * Get available NPCs in unlocked areas
     */
    fun getAvailableNPCs(playerSave: GameSave): List<HubNPC> {
        val unlockedAreas = getUnlockedAreas(playerSave)
        return HubNPC.values().filter { npc ->
            unlockedAreas.contains(npc.location)
        }
    }
    
    /**
     * Award a key item to the player
     */
    fun awardKeyItem(keyItem: KeyItem, playerSave: GameSave): GameSave {
        val updatedInventory = playerSave.inventory.toMutableMap()
        updatedInventory[keyItem.id] = 1
        
        return playerSave.copy(inventory = updatedInventory)
    }
    
    /**
     * Mark a story milestone as completed
     */
    fun completeStoryMilestone(milestone: StoryMilestone, playerSave: GameSave): GameSave {
        val updatedStoryProgress = playerSave.storyProgress.toMutableMap()
        updatedStoryProgress[milestone.id] = true
        
        return playerSave.copy(storyProgress = updatedStoryProgress)
    }
    
    /**
     * Get hub facilities available to the player
     */
    fun getAvailableFacilities(playerSave: GameSave): List<HubFacility> {
        val unlockedAreas = getUnlockedAreas(playerSave)
        
        return unlockedAreas.map { area ->
            val npc = HubNPC.values().find { it.location == area }
            val functions = when (area) {
                HubArea.MAIN_HALL -> listOf(FacilityFunction.STORY_DIALOGUE, FacilityFunction.MONSTER_HEALING)
                HubArea.MONSTER_LIBRARY -> listOf(FacilityFunction.MONSTER_INFO)
                HubArea.BREEDING_LAB -> listOf(FacilityFunction.MONSTER_STORAGE, FacilityFunction.STORY_DIALOGUE)
                HubArea.BATTLE_ARENA -> listOf(FacilityFunction.TOURNAMENT_BATTLE, FacilityFunction.STORY_DIALOGUE)
                HubArea.SYNTHESIS_LAB -> listOf(FacilityFunction.MONSTER_SYNTHESIS, FacilityFunction.STORY_DIALOGUE)
                HubArea.ITEM_SHOP -> listOf(FacilityFunction.ITEM_PURCHASE, FacilityFunction.STORY_DIALOGUE)
                HubArea.GATE_CHAMBER -> listOf(FacilityFunction.WORLD_TRAVEL, FacilityFunction.STORY_DIALOGUE)
                HubArea.MASTER_QUARTERS -> listOf(FacilityFunction.STORY_DIALOGUE, FacilityFunction.MONSTER_HEALING)
                HubArea.SECRET_VAULT -> listOf(FacilityFunction.STORY_DIALOGUE, FacilityFunction.MONSTER_INFO)
            }
            
            HubFacility(
                area = area,
                npc = npc,
                isUnlocked = true,
                functions = functions
            )
        }
    }
    
    /**
     * Get next story objective for the player
     */
    fun getNextObjective(playerSave: GameSave): String {
        val unlockedAreas = getUnlockedAreas(playerSave)
        
        return when {
            !unlockedAreas.contains(HubArea.MONSTER_LIBRARY) -> 
                "Capture your first wild monster to unlock the Monster Library"
            !unlockedAreas.contains(HubArea.ITEM_SHOP) -> 
                "Win a battle to earn gold and unlock the Item Shop"
            !unlockedAreas.contains(HubArea.BREEDING_LAB) -> 
                "Complete a quest to earn your Breeder's License"
            !unlockedAreas.contains(HubArea.BATTLE_ARENA) -> 
                "Prove your skills to earn the Novice Badge"
            !unlockedAreas.contains(HubArea.SYNTHESIS_LAB) -> 
                "Complete a dungeon floor to unlock the Synthesis Lab"
            !unlockedAreas.contains(HubArea.GATE_CHAMBER) -> 
                "Find the Explorer's Compass to access other worlds"
            !unlockedAreas.contains(HubArea.MASTER_QUARTERS) -> 
                "Earn the Master's Key through great achievements"
            !unlockedAreas.contains(HubArea.SECRET_VAULT) -> 
                "Discover the Ancient Relic to unlock the final secrets"
            else -> 
                "Continue your journey as a Monster Master!"
        }
    }
    
    /**
     * STORY INTEGRATION - Trigger milestones when entering areas
     */
    fun onAreaEntered(area: HubArea): List<String> {
        val milestones = mutableListOf<String>()
        val storyProgress = storySystem.currentStoryProgress.value
        
        when (area) {
            HubArea.MONSTER_LIBRARY -> {
                if (storyProgress["visited_library"] != true) {
                    storySystem.triggerMilestone("visited_library")
                    milestones.add("You've discovered the Monster Library!")
                }
            }
            HubArea.SYNTHESIS_LAB -> {
                if (storyProgress["synthesis_lab_visited"] != true) {
                    storySystem.triggerMilestone("synthesis_lab_visited") 
                    milestones.add("The Synthesis Laboratory is now accessible!")
                }
            }
            HubArea.BATTLE_ARENA -> {
                if (storyProgress["arena_discovered"] != true) {
                    storySystem.triggerMilestone("arena_discovered")
                    milestones.add("You can now participate in tournaments!")
                }
            }
            else -> { /* No special milestones for other areas */ }
        }
        
        return milestones
    }
    
    /**
     * Check if area can be accessed with story system integration
     */
    fun canAccessAreaWithStory(area: HubArea, playerSave: GameSave): Boolean {
        val storyProgress = storySystem.currentStoryProgress.value
        
        val storyRequirementMet = area.requiredStoryProgress?.let {
            storyProgress[it] == true
        } ?: true
        
        val keyItemRequirementMet = area.requiredKeyItem?.let {
            playerSave.inventory[it]?.let { count -> count > 0 } ?: false
        } ?: true
        
        return storyRequirementMet && keyItemRequirementMet
    }
}