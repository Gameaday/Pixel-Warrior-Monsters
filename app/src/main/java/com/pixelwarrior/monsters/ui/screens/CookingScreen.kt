package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.game.cooking.CookingSystem
import com.pixelwarrior.monsters.game.cooking.CookingResult
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Cooking screen for creating treats to improve monster affection and joining chances
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingScreen(
    playerInventory: Map<String, Int>,
    cookingSkill: CookingSkill,
    onCook: (CookingRecipe) -> Unit,
    onBack: () -> Unit
) {
    val cookingSystem = remember { CookingSystem() }
    var selectedRecipe by remember { mutableStateOf<CookingRecipe?>(null) }
    var cookingResult by remember { mutableStateOf<CookingResult?>(null) }
    
    val availableRecipes = cookingSystem.getAvailableRecipes(cookingSkill)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack)
            .padding(16.dp)
    ) {
        // Header
        CookingHeader(
            cookingSkill = cookingSkill,
            onBack = onBack
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Show cooking result if available
        cookingResult?.let { result ->
            CookingResultDialog(
                result = result,
                onDismiss = { cookingResult = null }
            )
        }
        
        // Recipe selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PixelDarkGray)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🍳 Available Recipes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PixelYellow,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (availableRecipes.isEmpty()) {
                    Text(
                        text = "Learn new recipes by exploring or talking to NPCs!",
                        color = PixelLightGray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableRecipes) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                playerInventory = playerInventory,
                                isSelected = selectedRecipe == recipe,
                                onSelect = { selectedRecipe = recipe }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cook button and selected recipe details
        selectedRecipe?.let { recipe ->
            SelectedRecipeDetails(
                recipe = recipe,
                playerInventory = playerInventory,
                onCook = {
                    onCook(recipe)
                    selectedRecipe = null
                }
            )
        }
    }
}

@Composable
private fun CookingHeader(
    cookingSkill: CookingSkill,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGreen)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = PixelBrown)
            ) {
                Text("Back", color = Color.White)
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🍳 Cooking Kitchen",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PixelYellow,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Skill Level: ${cookingSkill.level} (${cookingSkill.experience} XP)",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Icon(
                imageVector = Icons.Default.Kitchen,
                contentDescription = "Cooking",
                tint = PixelYellow,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: CookingRecipe,
    playerInventory: Map<String, Int>,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val canCook = recipe.ingredients.all { (ingredient, needed) ->
        (playerInventory[ingredient] ?: 0) >= needed
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .then(
                if (isSelected) Modifier.border(2.dp, PixelYellow, RoundedCornerShape(8.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (canCook) PixelDarkBlue else PixelDarkGray
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (canCook) Color.White else PixelLightGray,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Lv.${recipe.requiredLevel}",
                    color = PixelYellow,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (canCook) PixelLightGray else PixelGray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Ingredients
            recipe.ingredients.forEach { (ingredient, needed) ->
                val available = playerInventory[ingredient] ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ingredient.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (available >= needed) Color.White else PixelRed
                    )
                    Text(
                        text = "$available/$needed",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (available >= needed) PixelGreen else PixelRed
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedRecipeDetails(
    recipe: CookingRecipe,
    playerInventory: Map<String, Int>,
    onCook: () -> Unit
) {
    val canCook = recipe.ingredients.all { (ingredient, needed) ->
        (playerInventory[ingredient] ?: 0) >= needed
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelDarkBlue)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📋 ${recipe.name}",
                style = MaterialTheme.typography.titleLarge,
                color = PixelYellow,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "⏰ ${recipe.cookingTime / 60} minutes",
                    color = PixelLightGray,
                    style = MaterialTheme.typography.bodySmall
                )
                
                Text(
                    text = "🎯 Level ${recipe.requiredLevel} required",
                    color = PixelYellow,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCook,
                enabled = canCook,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canCook) PixelGreen else PixelGray
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (canCook) "🍳 Cook Recipe" else "❌ Missing Ingredients",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CookingResultDialog(
    result: CookingResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (result) {
                    is CookingResult.Success -> "🎉 Cooking Success!"
                    is CookingResult.PartialFailure -> "😅 Partial Success"
                    is CookingResult.Failure -> "💥 Cooking Failed"
                },
                color = when (result) {
                    is CookingResult.Success -> PixelGreen
                    is CookingResult.PartialFailure -> PixelYellow
                    is CookingResult.Failure -> PixelRed
                }
            )
        },
        text = {
            Text(
                text = when (result) {
                    is CookingResult.Success -> "${result.message}\n\nYou gained ${result.experienceGained} cooking XP!"
                    is CookingResult.PartialFailure -> "${result.message}\n\nYou gained ${result.experienceGained} cooking XP."
                    is CookingResult.Failure -> result.message
                },
                color = Color.White
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PixelBlue)
            ) {
                Text("OK", color = Color.White)
            }
        },
        containerColor = PixelDarkGray
    )
}