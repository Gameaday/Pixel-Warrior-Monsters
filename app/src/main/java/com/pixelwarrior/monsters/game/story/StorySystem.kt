package com.pixelwarrior.monsters.game.story

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Story Progression Framework - Phase 2 Implementation
 * Manages story milestones, quest progression, and narrative triggers
 */
class StorySystem {
    
    private val _currentStoryProgress = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val currentStoryProgress: StateFlow<Map<String, Boolean>> = _currentStoryProgress.asStateFlow()
    
    private val _activeQuests = MutableStateFlow<List<Quest>>(emptyList())
    val activeQuests: StateFlow<List<Quest>> = _activeQuests.asStateFlow()
    
    private val _completedQuests = MutableStateFlow<List<Quest>>(emptyList())
    val completedQuests: StateFlow<List<Quest>> = _completedQuests.asStateFlow()
    
    private val storyMilestones = createStoryMilestones()
    private val questDatabase = createQuestDatabase()
    
    /**
     * Initialize story system with default progress
     */
    fun initializeStory() {
        _currentStoryProgress.value = mapOf(
            "game_started" to true,
            "met_master" to false,
            "first_monster_captured" to false,
            "visited_library" to false,
            "completed_tutorial" to false,
            "first_synthesis" to false,
            "tournament_unlocked" to false,
            "gates_discovered" to false
        )
        
        // Start with initial quest
        val initialQuest = questDatabase.find { it.id == "tutorial_quest" }
        if (initialQuest != null) {
            _activeQuests.value = listOf(initialQuest)
        }
    }
    
    /**
     * Trigger a story milestone and check for unlocks
     */
    fun triggerMilestone(milestoneId: String): List<StoryUnlock> {
        val currentProgress = _currentStoryProgress.value.toMutableMap()
        currentProgress[milestoneId] = true
        _currentStoryProgress.value = currentProgress
        
        val unlocks = mutableListOf<StoryUnlock>()
        
        // Check for story-triggered unlocks
        storyMilestones.filter { it.triggerMilestone == milestoneId }.forEach { milestone ->
            if (milestone.isUnlocked(currentProgress)) {
                unlocks.add(StoryUnlock(
                    type = UnlockType.AREA,
                    content = milestone.unlockedContent,
                    description = milestone.description
                ))
            }
        }
        
        // Check for new quests
        questDatabase.filter { quest ->
            quest.requirements.all { currentProgress[it] == true } &&
            !_activeQuests.value.contains(quest) &&
            !_completedQuests.value.contains(quest)
        }.forEach { quest ->
            val currentActive = _activeQuests.value.toMutableList()
            currentActive.add(quest)
            _activeQuests.value = currentActive
            
            unlocks.add(StoryUnlock(
                type = UnlockType.QUEST,
                content = quest.title,
                description = "New quest available: ${quest.title}"
            ))
        }
        
        return unlocks
    }
    
    /**
     * Complete a quest and trigger rewards
     */
    fun completeQuest(questId: String): QuestReward? {
        val quest = _activeQuests.value.find { it.id == questId } ?: return null
        
        // Move from active to completed
        val activeQuests = _activeQuests.value.toMutableList()
        val completedQuests = _completedQuests.value.toMutableList()
        
        activeQuests.remove(quest)
        completedQuests.add(quest.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
        
        _activeQuests.value = activeQuests
        _completedQuests.value = completedQuests
        
        // Trigger any story milestones from quest completion
        quest.completionMilestone?.let { triggerMilestone(it) }
        
        return quest.reward
    }
    
    /**
     * Check if a story gate should be unlocked
     */
    fun isStoryGateUnlocked(gateId: String): Boolean {
        val currentProgress = _currentStoryProgress.value
        val gate = storyMilestones.find { it.unlockedContent == gateId }
        return gate?.isUnlocked(currentProgress) ?: false
    }
    
    /**
     * Get available dialogue options for a character
     */
    fun getAvailableDialogue(characterId: String): List<DialogueOption> {
        val progress = _currentStoryProgress.value
        val activeQuestIds = _activeQuests.value.map { it.id }
        
        return createDialogueOptions(characterId, progress, activeQuestIds)
    }
    
    /**
     * Update quest progress  
     */
    fun updateQuestProgress(questId: String, progressKey: String, value: Any) {
        val activeQuests = _activeQuests.value.toMutableList()
        val questIndex = activeQuests.indexOfFirst { it.id == questId }
        
        if (questIndex >= 0) {
            val quest = activeQuests[questIndex]
            val updatedProgress = quest.progress.toMutableMap()
            updatedProgress[progressKey] = value
            
            activeQuests[questIndex] = quest.copy(progress = updatedProgress)
            _activeQuests.value = activeQuests
            
            // Check if quest is now complete
            if (quest.isComplete(updatedProgress)) {
                completeQuest(questId)
            }
        }
    }
    
    /**
     * Get available quests
     */
    fun getAvailableQuests(): List<Quest> {
        return _activeQuests.value
    }
    
    /**
     * Get story milestones
     */
    fun getStoryMilestones(): List<StoryMilestone> {
        return storyMilestones
    }
    
    /**
     * Check if story can progress based on game save
     */
    fun canProgressStory(gameSave: com.pixelwarrior.monsters.data.model.GameSave): Boolean {
        val progress = gameSave.storyProgress
        // Check if there are any active quests or unlockable milestones
        return _activeQuests.value.isNotEmpty() || 
               storyMilestones.any { !it.isUnlocked(progress) }
    }
    
    private fun createStoryMilestones(): List<StoryMilestone> {
        return listOf(
            StoryMilestone(
                id = "tutorial_completion",
                triggerMilestone = "completed_tutorial",
                unlockedContent = "monster_library",
                description = "Monster Library now available",
                requirements = listOf("completed_tutorial")
            ),
            StoryMilestone(
                id = "first_capture",
                triggerMilestone = "first_monster_captured", 
                unlockedContent = "breeding_lab",
                description = "Breeding Laboratory unlocked",
                requirements = listOf("first_monster_captured", "visited_library")
            ),
            StoryMilestone(
                id = "synthesis_unlock",
                triggerMilestone = "first_synthesis",
                unlockedContent = "synthesis_lab",
                description = "Synthesis Laboratory accessible",
                requirements = listOf("first_synthesis", "completed_tutorial")
            ),
            StoryMilestone(
                id = "tournament_access",
                triggerMilestone = "tournament_unlocked",
                unlockedContent = "battle_arena",
                description = "Battle Arena tournaments available",
                requirements = listOf("first_monster_captured", "completed_tutorial")
            ),
            StoryMilestone(
                id = "exploration_gates",
                triggerMilestone = "gates_discovered",
                unlockedContent = "gate_chamber",
                description = "Gate Chamber for world exploration",
                requirements = listOf("tournament_unlocked", "first_synthesis")
            )
        )
    }
    
    private fun createQuestDatabase(): List<Quest> {
        return listOf(
            Quest(
                id = "tutorial_quest",
                title = "Welcome to Monster Training",
                description = "Learn the basics of monster training from Master Teto",
                requirements = listOf("game_started"),
                objectives = listOf(
                    QuestObjective("talk_to_master", "Speak with Master Teto", false),
                    QuestObjective("visit_library", "Visit the Monster Library", false),
                    QuestObjective("capture_first_monster", "Capture your first monster", false)
                ),
                reward = QuestReward(
                    gold = 500,
                    items = mapOf("herb" to 5, "monster_bait" to 3),
                    experience = 100
                ),
                completionMilestone = "completed_tutorial"
            ),
            Quest(
                id = "first_synthesis",
                title = "The Art of Synthesis", 
                description = "Learn to combine monsters through synthesis",
                requirements = listOf("completed_tutorial", "first_monster_captured"),
                objectives = listOf(
                    QuestObjective("talk_to_synthesis_expert", "Speak with Dr. Kaine", false),
                    QuestObjective("perform_synthesis", "Successfully synthesize two monsters", false)
                ),
                reward = QuestReward(
                    gold = 1000,
                    items = mapOf("synthesis_crystal" to 1),
                    experience = 250
                ),
                completionMilestone = "first_synthesis"
            ),
            Quest(
                id = "tournament_prep",
                title = "Tournament Preparation",
                description = "Prepare for your first tournament battle",
                requirements = listOf("first_synthesis", "completed_tutorial"),
                objectives = listOf(
                    QuestObjective("train_monster_level_10", "Train a monster to level 10", false),
                    QuestObjective("talk_to_arena_master", "Speak with Champion Rica", false),
                    QuestObjective("win_first_tournament", "Win your first tournament match", false)
                ),
                reward = QuestReward(
                    gold = 2000,
                    items = mapOf("tournament_badge" to 1, "victory_crown" to 1),
                    experience = 500
                ),
                completionMilestone = "tournament_unlocked"
            )
        )
    }
    
    private fun createDialogueOptions(
        characterId: String,
        progress: Map<String, Boolean>,
        activeQuests: List<String>
    ): List<DialogueOption> {
        return when (characterId) {
            "master" -> listOf(
                DialogueOption(
                    id = "greeting",
                    text = if (progress["completed_tutorial"] == true) 
                        "How is your training progressing?" 
                        else "Welcome, young trainer!",
                    isAvailable = true
                ),
                DialogueOption(
                    id = "tutorial",
                    text = "Can you teach me about monster training?",
                    isAvailable = progress["completed_tutorial"] != true && "tutorial_quest" in activeQuests
                ),
                DialogueOption(
                    id = "advice",
                    text = "Do you have any advice for me?",
                    isAvailable = progress["completed_tutorial"] == true
                )
            )
            "librarian" -> listOf(
                DialogueOption(
                    id = "greeting",
                    text = "Welcome to the Monster Library",
                    isAvailable = progress["visited_library"] == true
                ),
                DialogueOption(
                    id = "monster_info",
                    text = "Tell me about monster families",
                    isAvailable = progress["visited_library"] == true
                ),
                DialogueOption(
                    id = "breeding_info",
                    text = "How does monster breeding work?",
                    isAvailable = progress["first_monster_captured"] == true
                )
            )
            else -> emptyList()
        }
    }
}

/**
 * Data classes for story system
 */
data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val requirements: List<String>,
    val objectives: List<QuestObjective>,
    val reward: QuestReward,
    val progress: Map<String, Any> = emptyMap(),
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val completionMilestone: String? = null
) {
    fun isComplete(currentProgress: Map<String, Any>): Boolean {
        return objectives.all { objective ->
            currentProgress[objective.id] == true
        }
    }
}

data class QuestObjective(
    val id: String,
    val description: String,
    val isCompleted: Boolean
)

data class QuestReward(
    val gold: Int = 0,
    val items: Map<String, Int> = emptyMap(),
    val experience: Int = 0,
    val unlockedFeatures: List<String> = emptyList()
)

data class StoryMilestone(
    val id: String,
    val triggerMilestone: String,
    val unlockedContent: String,
    val description: String,
    val requirements: List<String>
) {
    fun isUnlocked(progress: Map<String, Boolean>): Boolean {
        return requirements.all { progress[it] == true }
    }
}

data class StoryUnlock(
    val type: UnlockType,
    val content: String,
    val description: String
)

data class DialogueOption(
    val id: String,
    val text: String,
    val isAvailable: Boolean,
    val nextDialogueId: String? = null
)

enum class UnlockType {
    AREA, QUEST, FEATURE, ITEM
}