package com.pixelwarrior.monsters.game.cooking

import com.pixelwarrior.monsters.data.model.*
import kotlin.random.Random

/**
 * Cooking system for creating treats to improve monster affection and joining chances
 * Inspired by the original Dragon Quest Monsters taming mechanics
 */
class CookingSystem {

    /**
     * Get all available cooking recipes
     */
    fun getAllRecipes(): List<CookingRecipe> {
        return listOf(
            // Basic treats
            CookingRecipe(
                id = "basic_treat_recipe",
                name = "Basic Monster Food",
                resultItem = "basic_treat",
                ingredients = mapOf("berries" to 2, "grain" to 1),
                cookingTime = 180, // 3 minutes
                requiredLevel = 1,
                description = "Simple food that monsters enjoy"
            ),
            
            // Quality treats
            CookingRecipe(
                id = "quality_treat_recipe", 
                name = "Quality Monster Treat",
                resultItem = "quality_treat",
                ingredients = mapOf("honey" to 1, "berries" to 3, "herbs" to 2),
                cookingTime = 300, // 5 minutes
                requiredLevel = 3,
                description = "A delicious treat that monsters love"
            ),
            
            // Premium treats
            CookingRecipe(
                id = "premium_treat_recipe",
                name = "Gourmet Monster Feast",
                resultItem = "premium_treat", 
                ingredients = mapOf("royal_honey" to 1, "exotic_fruit" to 2, "mystical_herbs" to 1, "quality_meat" to 1),
                cookingTime = 600, // 10 minutes
                requiredLevel = 7,
                description = "An exquisite delicacy that even legendary monsters crave"
            ),
            
            // Type-specific treats
            CookingRecipe(
                id = "fire_treat_recipe",
                name = "Spicy Fire Treat",
                resultItem = "fire_treat",
                ingredients = mapOf("spicy_peppers" to 3, "ember_crystal" to 1, "berries" to 2),
                cookingTime = 240,
                requiredLevel = 4,
                description = "A fiery treat that Fire-type monsters find irresistible"
            ),
            
            CookingRecipe(
                id = "water_treat_recipe",
                name = "Refreshing Water Treat",
                resultItem = "water_treat", 
                ingredients = mapOf("sea_salt" to 1, "kelp" to 2, "crystal_water" to 1, "berries" to 2),
                cookingTime = 240,
                requiredLevel = 4,
                description = "A cooling treat that Water-type monsters adore"
            ),
            
            CookingRecipe(
                id = "grass_treat_recipe",
                name = "Natural Grass Treat",
                resultItem = "grass_treat",
                ingredients = mapOf("fresh_leaves" to 4, "flower_nectar" to 1, "berries" to 2),
                cookingTime = 240, 
                requiredLevel = 4,
                description = "A natural treat that Grass-type monsters cherish"
            )
        )
    }

    /**
     * Attempt to cook a recipe
     */
    fun cookRecipe(
        recipe: CookingRecipe, 
        playerInventory: Map<String, Int>,
        cookingSkill: CookingSkill
    ): CookingResult {
        
        // Check skill level requirement
        if (cookingSkill.level < recipe.requiredLevel) {
            return CookingResult.Failure("Cooking skill level ${recipe.requiredLevel} required")
        }
        
        // Check if player has all ingredients
        for ((ingredient, needed) in recipe.ingredients) {
            val available = playerInventory[ingredient] ?: 0
            if (available < needed) {
                return CookingResult.Failure("Not enough $ingredient (need $needed, have $available)")
            }
        }
        
        // Calculate success chance based on skill level
        val baseSuccessChance = 0.7f
        val skillBonus = (cookingSkill.level - recipe.requiredLevel) * 0.1f
        val successChance = (baseSuccessChance + skillBonus).coerceAtMost(0.95f)
        
        return if (Random.nextFloat() < successChance) {
            val experienceGained = recipe.requiredLevel * 10 + Random.nextInt(5, 15)
            CookingResult.Success(
                resultItem = recipe.resultItem,
                experienceGained = experienceGained,
                message = "Successfully cooked ${recipe.name}!"
            )
        } else {
            // Partial failure - lose some ingredients but gain small amount of experience
            val partialExp = recipe.requiredLevel * 2
            CookingResult.PartialFailure(
                experienceGained = partialExp,
                message = "Cooking failed, but you learned something..."
            )
        }
    }
    
    /**
     * Get recipes available to player based on skill level and known recipes
     */
    fun getAvailableRecipes(cookingSkill: CookingSkill): List<CookingRecipe> {
        return getAllRecipes().filter { recipe ->
            cookingSkill.level >= recipe.requiredLevel && 
            (cookingSkill.knownRecipes.contains(recipe.id) || recipe.requiredLevel <= 3) // Basic recipes are always known
        }
    }

    /**
     * Calculate required experience for cooking skill level
     */
    fun getExperienceForLevel(level: Int): Int {
        return level * level * 50
    }
    
    /**
     * Check if cooking skill should level up
     */
    fun checkLevelUp(cookingSkill: CookingSkill): CookingSkill {
        val requiredExp = getExperienceForLevel(cookingSkill.level + 1)
        return if (cookingSkill.experience >= requiredExp) {
            cookingSkill.copy(
                level = cookingSkill.level + 1,
                experience = cookingSkill.experience - requiredExp
            )
        } else {
            cookingSkill
        }
    }
    
    /**
     * Learn a new recipe (could be from NPCs, books, experimentation)
     */
    fun learnRecipe(cookingSkill: CookingSkill, recipeId: String): CookingSkill {
        return if (!cookingSkill.knownRecipes.contains(recipeId)) {
            cookingSkill.copy(knownRecipes = cookingSkill.knownRecipes + recipeId)
        } else {
            cookingSkill
        }
    }
}

/**
 * Result of cooking attempt
 */
sealed class CookingResult {
    data class Success(
        val resultItem: String,
        val experienceGained: Int,
        val message: String
    ) : CookingResult()
    
    data class PartialFailure(
        val experienceGained: Int,
        val message: String  
    ) : CookingResult()
    
    data class Failure(
        val message: String
    ) : CookingResult()
}