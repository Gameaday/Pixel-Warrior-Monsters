package com.pixelwarrior.monsters.game.story

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Interactive Dialogue System - Phase 2 Implementation
 * Handles dialogue trees, character relationships, and dynamic conversations
 */
class DialogueSystem {
    
    private val _currentDialogue = MutableStateFlow<DialogueNode?>(null)
    val currentDialogue: StateFlow<DialogueNode?> = _currentDialogue.asStateFlow()
    
    private val _characterRelationships = MutableStateFlow<Map<String, CharacterRelationship>>(emptyMap())
    val characterRelationships: StateFlow<Map<String, CharacterRelationship>> = _characterRelationships.asStateFlow()
    
    private val dialogueDatabase = createDialogueDatabase()
    
    /**
     * Start a dialogue with a character
     */
    fun startDialogue(characterId: String, storyProgress: Map<String, Boolean>): DialogueNode? {
        val startingDialogue = getStartingDialogue(characterId, storyProgress)
        _currentDialogue.value = startingDialogue
        return startingDialogue
    }
    
    /**
     * Select a dialogue choice and progress the conversation
     */
    fun selectChoice(choiceId: String): DialogueResult {
        val currentNode = _currentDialogue.value ?: return DialogueResult.NoDialogue
        
        val choice = currentNode.choices.find { it.id == choiceId }
            ?: return DialogueResult.InvalidChoice
        
        // Apply choice consequences
        applyChoiceConsequences(choice)
        
        // Get next dialogue node
        val nextNode = choice.nextNodeId?.let { getDialogueNode(it) }
        _currentDialogue.value = nextNode
        
        return if (nextNode != null) {
            DialogueResult.Continue(nextNode)
        } else {
            DialogueResult.End(choice.consequences)
        }
    }
    
    /**
     * End the current dialogue
     */
    fun endDialogue() {
        _currentDialogue.value = null
    }
    
    /**
     * Get character relationship level
     */
    fun getRelationshipLevel(characterId: String): RelationshipLevel {
        return _characterRelationships.value[characterId]?.level ?: RelationshipLevel.NEUTRAL
    }
    
    /**
     * Update character relationship based on interactions
     */
    fun updateRelationship(characterId: String, change: Int) {
        val relationships = _characterRelationships.value.toMutableMap()
        val current = relationships[characterId] ?: CharacterRelationship(characterId, 0, RelationshipLevel.NEUTRAL)
        
        val newPoints = (current.relationshipPoints + change).coerceIn(-100, 100)
        val newLevel = when {
            newPoints >= 80 -> RelationshipLevel.TRUSTED
            newPoints >= 40 -> RelationshipLevel.FRIENDLY
            newPoints >= -20 -> RelationshipLevel.NEUTRAL
            newPoints >= -60 -> RelationshipLevel.UNFRIENDLY
            else -> RelationshipLevel.HOSTILE
        }
        
        relationships[characterId] = CharacterRelationship(characterId, newPoints, newLevel)
        _characterRelationships.value = relationships
    }
    
    private fun getStartingDialogue(characterId: String, storyProgress: Map<String, Boolean>): DialogueNode? {
        val characterDialogues = dialogueDatabase[characterId] ?: return null
        
        // Find the most appropriate starting dialogue based on story progress
        return characterDialogues
            .filter { it.isAvailable(storyProgress, getRelationshipLevel(characterId)) }
            .maxByOrNull { it.priority }
    }
    
    private fun getDialogueNode(nodeId: String): DialogueNode? {
        return dialogueDatabase.values.flatten().find { it.id == nodeId }
    }
    
    private fun applyChoiceConsequences(choice: DialogueChoice) {
        choice.consequences.forEach { consequence ->
            when (consequence.type) {
                ConsequenceType.RELATIONSHIP -> {
                    updateRelationship(consequence.characterId!!, consequence.value as Int)
                }
                ConsequenceType.STORY_FLAG -> {
                    // Would update story progress - handled by StorySystem
                }
                ConsequenceType.ITEM_REWARD -> {
                    // Would give items - handled by inventory system
                }
                ConsequenceType.QUEST_TRIGGER -> {
                    // Would trigger quest - handled by StorySystem
                }
            }
        }
    }
    
    private fun createDialogueDatabase(): Map<String, List<DialogueNode>> {
        return mapOf(
            "master" -> createMasterDialogues(),
            "librarian" -> createLibrarianDialogues(),
            "synthesis_expert" -> createSynthesisExpertDialogues(),
            "arena_master" -> createArenaMasterDialogues()
        )
    }
    
    private fun createMasterDialogues(): List<DialogueNode> {
        return listOf(
            DialogueNode(
                id = "master_intro",
                characterId = "master",
                text = "Welcome to the Monster Sanctuary, young trainer! I am Master Teto, and I will guide you on your journey to become a true Monster Master.",
                choices = listOf(
                    DialogueChoice(
                        id = "eager_response",
                        text = "I'm ready to learn everything!",
                        consequences = listOf(
                            DialogueConsequence(ConsequenceType.RELATIONSHIP, "master", 5)
                        ),
                        nextNodeId = "master_tutorial_start"
                    ),
                    DialogueChoice(
                        id = "cautious_response", 
                        text = "What exactly does monster training involve?",
                        consequences = listOf(),
                        nextNodeId = "master_explanation"
                    )
                ),
                requirements = listOf("game_started"),
                relationshipRequirement = RelationshipLevel.NEUTRAL,
                priority = 10
            ),
            DialogueNode(
                id = "master_tutorial_start",
                characterId = "master",
                text = "Excellent attitude! Monster training is about forming partnerships with creatures from different worlds. Each monster has unique abilities and personalities.",
                choices = listOf(
                    DialogueChoice(
                        id = "continue_tutorial",
                        text = "Tell me more about monster types.",
                        consequences = listOf(),
                        nextNodeId = "master_monster_types"
                    )
                ),
                requirements = listOf("game_started"),
                relationshipRequirement = RelationshipLevel.NEUTRAL,
                priority = 5
            ),
            DialogueNode(
                id = "master_explanation",
                characterId = "master",
                text = "Monster training involves capturing wild monsters, raising them, and forming bonds. You'll explore different worlds, discover new species, and even combine monsters through synthesis.",
                choices = listOf(
                    DialogueChoice(
                        id = "sounds_interesting",
                        text = "That sounds fascinating!",
                        consequences = listOf(
                            DialogueConsequence(ConsequenceType.RELATIONSHIP, "master", 3)
                        ),
                        nextNodeId = "master_tutorial_start"
                    ),
                    DialogueChoice(
                        id = "seems_dangerous",
                        text = "Isn't that dangerous?",
                        consequences = listOf(
                            DialogueConsequence(ConsequenceType.RELATIONSHIP, "master", -1)
                        ),
                        nextNodeId = "master_safety"
                    )
                ),
                requirements = listOf("game_started"),
                relationshipRequirement = RelationshipLevel.NEUTRAL,
                priority = 5
            ),
            DialogueNode(
                id = "master_advanced",
                characterId = "master",
                text = "You've made excellent progress! How are you finding the deeper aspects of monster training?",
                choices = listOf(
                    DialogueChoice(
                        id = "going_well",
                        text = "I'm learning so much every day.",
                        consequences = listOf(
                            DialogueConsequence(ConsequenceType.RELATIONSHIP, "master", 2)
                        ),
                        nextNodeId = null
                    ),
                    DialogueChoice(
                        id = "need_help",
                        text = "I could use some advanced techniques.",
                        consequences = listOf(),
                        nextNodeId = "master_advanced_tips"
                    )
                ),
                requirements = listOf("completed_tutorial", "first_monster_captured"),
                relationshipRequirement = RelationshipLevel.FRIENDLY,
                priority = 15
            )
        )
    }
    
    private fun createLibrarianDialogues(): List<DialogueNode> {
        return listOf(
            DialogueNode(
                id = "librarian_intro",
                characterId = "librarian",
                text = "Welcome to the Monster Library! I'm Scholar Maya. Here you can learn about monster families, breeding compatibility, and battle strategies.",
                choices = listOf(
                    DialogueChoice(
                        id = "monster_families",
                        text = "Tell me about monster families.",
                        consequences = listOf(),
                        nextNodeId = "librarian_families"
                    ),
                    DialogueChoice(
                        id = "breeding_info",
                        text = "How does breeding work?",
                        consequences = listOf(),
                        nextNodeId = "librarian_breeding"
                    )
                ),
                requirements = listOf("visited_library"),
                relationshipRequirement = RelationshipLevel.NEUTRAL,
                priority = 10
            )
        )
    }
    
    private fun createSynthesisExpertDialogues(): List<DialogueNode> {
        return listOf(
            DialogueNode(
                id = "synthesis_intro",
                characterId = "synthesis_expert",
                text = "Ah, another aspiring synthesist! I'm Dr. Kaine. Monster synthesis is the ultimate expression of monster mastery - combining two creatures into something greater.",
                choices = listOf(
                    DialogueChoice(
                        id = "learn_synthesis",
                        text = "Teach me about synthesis!",
                        consequences = listOf(
                            DialogueConsequence(ConsequenceType.RELATIONSHIP, "synthesis_expert", 5)
                        ),
                        nextNodeId = "synthesis_explanation"
                    ),
                    DialogueChoice(
                        id = "is_it_safe",
                        text = "Is synthesis safe for the monsters?",
                        consequences = listOf(),
                        nextNodeId = "synthesis_safety"
                    )
                ),
                requirements = listOf("completed_tutorial"),
                relationshipRequirement = RelationshipLevel.NEUTRAL,
                priority = 10
            )
        )
    }
    
    private fun createArenaMasterDialogues(): List<DialogueNode> {
        return listOf(
            DialogueNode(
                id = "arena_intro",
                characterId = "arena_master",
                text = "So you think you're ready for tournament battle? I'm Champion Rica, and I've seen many trainers come and go. Show me what you've got!",
                choices = listOf(
                    DialogueChoice(
                        id = "accept_challenge",
                        text = "I'm ready for any challenge!",
                        consequences = listOf(
                            DialogueConsequence(ConsequenceType.RELATIONSHIP, "arena_master", 3)
                        ),
                        nextNodeId = "arena_challenge"
                    ),
                    DialogueChoice(
                        id = "ask_advice",
                        text = "What advice do you have for tournament battles?",
                        consequences = listOf(),
                        nextNodeId = "arena_advice"
                    )
                ),
                requirements = listOf("first_monster_captured"),
                relationshipRequirement = RelationshipLevel.NEUTRAL,
                priority = 10
            )  
        )
    }
}

/**
 * Data classes for dialogue system
 */
data class DialogueNode(
    val id: String,
    val characterId: String,
    val text: String,
    val choices: List<DialogueChoice>,
    val requirements: List<String> = emptyList(),
    val relationshipRequirement: RelationshipLevel = RelationshipLevel.NEUTRAL,
    val priority: Int = 0
) {
    fun isAvailable(storyProgress: Map<String, Boolean>, relationshipLevel: RelationshipLevel): Boolean {
        val storyRequirementsMet = requirements.all { storyProgress[it] == true }
        val relationshipMet = relationshipLevel.ordinal >= relationshipRequirement.ordinal
        return storyRequirementsMet && relationshipMet
    }
}

data class DialogueChoice(
    val id: String,
    val text: String,
    val consequences: List<DialogueConsequence>,
    val nextNodeId: String? = null
)

data class DialogueConsequence(
    val type: ConsequenceType,
    val characterId: String? = null,
    val value: Any
)

data class CharacterRelationship(
    val characterId: String,
    val relationshipPoints: Int,
    val level: RelationshipLevel
)

enum class RelationshipLevel {
    HOSTILE, UNFRIENDLY, NEUTRAL, FRIENDLY, TRUSTED
}

enum class ConsequenceType {
    RELATIONSHIP, STORY_FLAG, ITEM_REWARD, QUEST_TRIGGER
}

sealed class DialogueResult {
    object NoDialogue : DialogueResult()
    object InvalidChoice : DialogueResult()
    data class Continue(val nextNode: DialogueNode) : DialogueResult()
    data class End(val consequences: List<DialogueConsequence>) : DialogueResult()
}