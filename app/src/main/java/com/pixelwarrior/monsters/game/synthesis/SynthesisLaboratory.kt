package com.pixelwarrior.monsters.game.synthesis

import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.game.story.StorySystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Synthesis Laboratory System - Phase 3 Implementation
 * Handles synthesis laboratory interactions, process management, and recipe discovery
 */
class SynthesisLaboratory(
    private val storySystem: StorySystem,
    private val monsterSynthesis: MonsterSynthesis = MonsterSynthesis()
) {
    
    /**
     * Secondary constructor for testing
     */
    constructor() : this(StorySystem().apply { initializeStory() })
    
    private val _isLabUnlocked = MutableStateFlow(false)
    val isLabUnlocked: StateFlow<Boolean> = _isLabUnlocked.asStateFlow()
    
    private val _discoveredRecipes = MutableStateFlow<Set<String>>(emptySet())
    val discoveredRecipes: StateFlow<Set<String>> = _discoveredRecipes.asStateFlow()
    
    private val _currentSynthesisProcess = MutableStateFlow<SynthesisProcess?>(null)
    val currentSynthesisProcess: StateFlow<SynthesisProcess?> = _currentSynthesisProcess.asStateFlow()
    
    private val _labResources = MutableStateFlow(LabResources())
    val labResources: StateFlow<LabResources> = _labResources.asStateFlow()
    
    /**
     * Initialize synthesis laboratory
     */
    fun initializeLab() {
        // Check if synthesis lab is unlocked through story
        val storyProgress = storySystem.currentStoryProgress.value
        _isLabUnlocked.value = storyProgress["synthesis_lab_visited"] == true
        
        // Initialize with basic resources
        _labResources.value = LabResources(
            synthesisEnergy = 100,
            catalystStones = 5,
            stabilizers = 3,
            enhancementCrystals = 1
        )
        
        // Start with basic recipes discovered
        _discoveredRecipes.value = setOf(
            "basic_dragon_synthesis",
            "elemental_fusion",
            "beast_evolution"
        )
    }
    
    /**
     * Start synthesis process with two monsters
     */
    fun startSynthesis(
        parent1: Monster,
        parent2: Monster,
        playerInventory: Map<String, Int>
    ): SynthesisResult {
        
        if (!_isLabUnlocked.value) {
            return SynthesisResult.Failure("Synthesis Laboratory is not yet accessible")
        }
        
        if (_currentSynthesisProcess.value != null) {
            return SynthesisResult.Failure("Another synthesis is already in progress")
        }
        
        // Check if monsters can be synthesized
        val compatibility = monsterSynthesis.checkCompatibility(
            EnhancedMonster(parent1), EnhancedMonster(parent2)
        )
        
        if (compatibility !is CompatibilityResult.Compatible) {
            return SynthesisResult.Failure(
                when (compatibility) {
                    is CompatibilityResult.IncompatibleFamily -> "Monster families are incompatible for synthesis"
                    is CompatibilityResult.LevelTooLow -> "Monsters must be at least level ${compatibility.minimumLevel}"
                    is CompatibilityResult.SameSpecies -> "Cannot synthesize monsters of the same species"
                    else -> "Monsters are not compatible for synthesis"
                }
            )
        }
        
        // Calculate synthesis cost
        val cost = calculateSynthesisCost(parent1, parent2)
        
        // Check if player has required resources
        if (!hasRequiredResources(cost, playerInventory)) {
            return SynthesisResult.Failure("Insufficient resources for synthesis")
        }
        
        // Calculate success rate
        val successRate = calculateSuccessRate(parent1, parent2)
        
        // Start synthesis process
        val process = SynthesisProcess(
            id = "synth_${System.currentTimeMillis()}",
            parent1 = parent1,
            parent2 = parent2,
            startTime = System.currentTimeMillis(),
            duration = calculateSynthesisDuration(parent1, parent2),
            successRate = successRate,
            cost = cost,
            phase = SynthesisPhase.PREPARATION
        )
        
        _currentSynthesisProcess.value = process
        
        return SynthesisResult.InProgress(process)
    }
    
    /**
     * Advance synthesis process to next phase
     */
    fun advanceSynthesis(): SynthesisResult {
        val currentProcess = _currentSynthesisProcess.value
            ?: return SynthesisResult.Failure("No synthesis in progress")
        
        val nextPhase = when (currentProcess.phase) {
            SynthesisPhase.PREPARATION -> SynthesisPhase.ENERGY_ALIGNMENT
            SynthesisPhase.ENERGY_ALIGNMENT -> SynthesisPhase.FUSION
            SynthesisPhase.FUSION -> SynthesisPhase.STABILIZATION
            SynthesisPhase.STABILIZATION -> SynthesisPhase.COMPLETION
            SynthesisPhase.COMPLETION -> return completeSynthesis()
        }
        
        val updatedProcess = currentProcess.copy(
            phase = nextPhase,
            progress = when (nextPhase) {
                SynthesisPhase.PREPARATION -> 0.0f
                SynthesisPhase.ENERGY_ALIGNMENT -> 0.25f
                SynthesisPhase.FUSION -> 0.5f
                SynthesisPhase.STABILIZATION -> 0.75f
                SynthesisPhase.COMPLETION -> 1.0f
            }
        )
        
        _currentSynthesisProcess.value = updatedProcess
        
        return SynthesisResult.InProgress(updatedProcess)
    }
    
    /**
     * Complete synthesis and generate result
     */
    private fun completeSynthesis(): SynthesisResult {
        val process = _currentSynthesisProcess.value
            ?: return SynthesisResult.Failure("No synthesis in progress")
        
        _currentSynthesisProcess.value = null
        
        // Roll for success
        val isSuccess = Random.nextFloat() < process.successRate
        
        if (!isSuccess) {
            return SynthesisResult.Failure("Synthesis failed - monsters remain unchanged")
        }
        
        // Perform actual synthesis
        val synthesisResult = monsterSynthesis.synthesizeMonsters(
            EnhancedMonster(process.parent1),
            EnhancedMonster(process.parent2)
        )
        
        return when (synthesisResult) {
            is SynthesisResult.Success -> {
                // Trigger story milestone for first synthesis
                val storyProgress = storySystem.currentStoryProgress.value
                if (storyProgress["first_synthesis"] != true) {
                    storySystem.triggerMilestone("first_synthesis")
                }
                
                // Discover new recipe if applicable
                discoverRecipeFromSynthesis(process.parent1, process.parent2)
                
                synthesisResult
            }
            else -> synthesisResult
        }
    }
    
    /**
     * Cancel current synthesis process
     */
    fun cancelSynthesis(): Boolean {
        val currentProcess = _currentSynthesisProcess.value ?: return false
        
        // Can only cancel in early phases
        if (currentProcess.phase in listOf(SynthesisPhase.FUSION, SynthesisPhase.STABILIZATION, SynthesisPhase.COMPLETION)) {
            return false
        }
        
        _currentSynthesisProcess.value = null
        return true
    }
    
    /**
     * Get synthesis preview without starting the process
     */
    fun previewSynthesis(parent1: Monster, parent2: Monster): SynthesisPreview {
        val compatibility = monsterSynthesis.checkCompatibility(
            EnhancedMonster(parent1), EnhancedMonster(parent2)
        )
        
        if (compatibility !is CompatibilityResult.Compatible) {
            return SynthesisPreview(
                isCompatible = false,
                possibleOffspring = emptyList(),
                successRate = 0.0f,
                cost = SynthesisCost()
            )
        }
        
        val possibleResults = monsterSynthesis.getPossibleSynthesisResults(
            EnhancedMonster(parent1), EnhancedMonster(parent2)
        )
        
        return SynthesisPreview(
            isCompatible = true,
            possibleOffspring = possibleResults,
            successRate = calculateSuccessRate(parent1, parent2),
            cost = calculateSynthesisCost(parent1, parent2)
        )
    }
    
    /**
     * Discover new synthesis recipes
     */
    fun discoverRecipe(recipeId: String) {
        val currentRecipes = _discoveredRecipes.value.toMutableSet()
        currentRecipes.add(recipeId)
        _discoveredRecipes.value = currentRecipes
    }
    
    /**
     * Get all available synthesis recipes
     */
    fun getAvailableRecipes(): List<SynthesisRecipe> {
        return monsterSynthesis.getAllRecipes()
            .filter { recipe ->
                val recipeId = "${recipe.parent1Family.name.lowercase()}_${recipe.parent2Family.name.lowercase()}_${recipe.resultSpeciesId}"
                _discoveredRecipes.value.contains(recipeId)
            }
    }
    
    private fun calculateSynthesisCost(parent1: Monster, parent2: Monster): SynthesisCost {
        val baseCost = (parent1.level + parent2.level) * 10
        
        return SynthesisCost(
            gold = baseCost,
            synthesisEnergy = parent1.level + parent2.level,
            catalystStones = 1,
            stabilizers = if (parent1.level > 20 || parent2.level > 20) 1 else 0
        )
    }
    
    private fun calculateSuccessRate(parent1: Monster, parent2: Monster): Float {
        val baseRate = 0.7f
        val levelBonus = minOf((parent1.level + parent2.level) * 0.01f, 0.2f)
        val compatibilityBonus = if (parent1.family == parent2.family) 0.1f else 0.0f
        
        return (baseRate + levelBonus + compatibilityBonus).coerceIn(0.1f, 0.95f)
    }
    
    private fun calculateSynthesisDuration(parent1: Monster, parent2: Monster): Long {
        val baseTime = 30_000L // 30 seconds
        val levelModifier = (parent1.level + parent2.level) * 1000L
        return baseTime + levelModifier
    }
    
    private fun hasRequiredResources(cost: SynthesisCost, inventory: Map<String, Int>): Boolean {
        val currentResources = _labResources.value
        
        return currentResources.synthesisEnergy >= cost.synthesisEnergy &&
               currentResources.catalystStones >= cost.catalystStones &&
               currentResources.stabilizers >= cost.stabilizers &&
               (inventory["gold"] ?: 0) >= cost.gold
    }
    
    private fun discoverRecipeFromSynthesis(parent1: Monster, parent2: Monster) {
        val recipeId = "${parent1.family.name.lowercase()}_${parent2.family.name.lowercase()}_fusion"
        discoverRecipe(recipeId)
    }
    
    /**
     * Get available synthesis combinations for a list of monsters
     */
    fun getAvailableSynthesis(monsters: List<Monster>): List<Map<String, Any>> {
        val synthesesOptions = mutableListOf<Map<String, Any>>()
        
        for (i in monsters.indices) {
            for (j in i+1 until monsters.size) {
                val monster1 = monsters[i]
                val monster2 = monsters[j]
                
                synthesesOptions.add(mapOf(
                    "parent1" to monster1.name,
                    "parent2" to monster2.name,
                    "resultLevel" to ((monster1.level + monster2.level) / 2),
                    "resultFamily" to monster1.family
                ))
            }
        }
        
        return synthesesOptions
    }
    
    /**
     * Get learnable skills for a monster
     */
    fun getLearnableSkills(monster: Monster): List<String> {
        return listOf(
            "Synthesis Boost",
            "Elemental Infusion",
            "Stat Transfer",
            "Trait Inheritance",
            "Advanced Fusion"
        ).filter { monster.level >= 10 }
    }
    
    /**
     * Calculate synthesis success rate based on factors
     */
    fun getSuccessRate(parent1: Monster, parent2: Monster, conditions: Map<String, Any> = emptyMap()): Float {
        var baseRate = 85
        
        // Level factor
        val avgLevel = (parent1.level + parent2.level) / 2
        val levelBonus = (avgLevel / 10) * 2
        baseRate += levelBonus
        
        // Affection factor (higher affection = higher success)
        val avgAffection = (parent1.affection + parent2.affection) / 2
        val affectionBonus = (avgAffection / 20)
        baseRate += affectionBonus
        
        // Family compatibility bonus
        if (parent1.family == parent2.family) {
            baseRate += 10
        }
        
        // Environmental conditions bonus
        if (conditions["full_moon"] == true) baseRate += 5
        if (conditions["special_catalyst"] == true) baseRate += 10
        
        // Cap at 95% and return as float (0.0-1.0)
        return minOf(95, maxOf(50, baseRate)) / 100.0f
    }
}

/**
 * Data classes for synthesis laboratory
 */
data class SynthesisProcess(
    val id: String,
    val parent1: Monster,
    val parent2: Monster,
    val startTime: Long,
    val duration: Long,
    val successRate: Float,
    val cost: SynthesisCost,
    val phase: SynthesisPhase,
    val progress: Float = 0.0f
)

data class SynthesisPreview(
    val isCompatible: Boolean,
    val possibleOffspring: List<String>,
    val successRate: Float,
    val cost: SynthesisCost
)

data class SynthesisCost(
    val gold: Int = 0,
    val synthesisEnergy: Int = 0,
    val catalystStones: Int = 0,
    val stabilizers: Int = 0,
    val enhancementCrystals: Int = 0
)

data class LabResources(
    val synthesisEnergy: Int = 100,
    val catalystStones: Int = 5,
    val stabilizers: Int = 3,
    val enhancementCrystals: Int = 1,
    val maxEnergy: Int = 200
)

enum class SynthesisPhase(val displayName: String) {
    PREPARATION("Preparing synthesis chamber..."),
    ENERGY_ALIGNMENT("Aligning monster energies..."),
    FUSION("Fusing monster essences..."),
    STABILIZATION("Stabilizing new form..."),
    COMPLETION("Synthesis complete!")
}
