package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.game.endgame.EndgameSystem
import com.pixelwarrior.monsters.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndgameContentScreen(
    playerLevel: Int,
    completedMainStory: Boolean,
    defeatedChampion: Boolean,
    achievements: List<String>,
    statistics: Map<String, Int>,
    currentPlaythrough: Int,
    onNavigateToPostGameDungeon: (EndgameSystem.PostGameDungeon) -> Unit,
    onNavigateToAdditionalWorld: (EndgameSystem.AdditionalWorld) -> Unit,
    onStartNewGamePlus: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val endgameSystem = remember { EndgameSystem() }
    var selectedTab by remember { mutableStateOf(0) }
    
    val postGameUnlocked = endgameSystem.arePostGameDungeonsUnlocked(
        playerLevel, completedMainStory, defeatedChampion
    )
    
    val availablePostGameDungeons = endgameSystem.getAvailablePostGameDungeons(playerLevel)
    val unlockedAdditionalWorlds = endgameSystem.getUnlockedAdditionalWorlds(achievements, statistics)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = PixelRed),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Back", color = PixelWhite, fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "Endgame Content",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PixelBlue
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PixelLightGray,
            contentColor = PixelBlack
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Post-Game", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Additional Worlds", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("New Game+", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Legendaries", fontSize = 12.sp) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTab) {
            0 -> PostGameDungeonsTab(
                postGameUnlocked = postGameUnlocked,
                availableDungeons = availablePostGameDungeons,
                playerLevel = playerLevel,
                onNavigateToPostGameDungeon = onNavigateToPostGameDungeon
            )
            1 -> AdditionalWorldsTab(
                unlockedWorlds = unlockedAdditionalWorlds,
                onNavigateToAdditionalWorld = onNavigateToAdditionalWorld
            )
            2 -> NewGamePlusTab(
                currentPlaythrough = currentPlaythrough,
                playerLevel = playerLevel,
                endgameSystem = endgameSystem,
                achievements = achievements,
                onStartNewGamePlus = onStartNewGamePlus
            )
            3 -> LegendariesTab(
                achievements = achievements,
                playerLevel = playerLevel
            )
        }
    }
}

@Composable
private fun PostGameDungeonsTab(
    postGameUnlocked: Boolean,
    availableDungeons: List<EndgameSystem.PostGameDungeon>,
    playerLevel: Int,
    onNavigateToPostGameDungeon: (EndgameSystem.PostGameDungeon) -> Unit
) {
    LazyColumn {
        if (!postGameUnlocked) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PixelLightGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Post-Game Content Locked",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Requirements:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelBlack
                        )
                        Text("• Reach level 70", fontSize = 12.sp, color = PixelBlack)
                        Text("• Complete main story", fontSize = 12.sp, color = PixelBlack)
                        Text("• Defeat the champion", fontSize = 12.sp, color = PixelBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Current Level: $playerLevel",
                            fontSize = 12.sp,
                            color = PixelBlue
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Post-Game Dungeons",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelBlue
                )
                Text(
                    text = "Ultra-high difficulty areas for master trainers",
                    fontSize = 12.sp,
                    color = PixelDarkGray
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            items(EndgameSystem.PostGameDungeon.values().toList()) { dungeon ->
                val isAvailable = availableDungeons.contains(dungeon)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAvailable) PixelWhite else PixelLightGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dungeon.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAvailable) PixelBlack else PixelDarkGray
                                )
                                Text(
                                    text = dungeon.description,
                                    fontSize = 12.sp,
                                    color = if (isAvailable) PixelDarkGray else PixelLightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text(
                                        text = "Theme: ${dungeon.theme}",
                                        fontSize = 10.sp,
                                        color = PixelBlue
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Floors: ${dungeon.floors}",
                                        fontSize = 10.sp,
                                        color = PixelBlue
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Required Level: ${dungeon.requiredLevel}",
                                        fontSize = 10.sp,
                                        color = if (playerLevel >= dungeon.requiredLevel) PixelGreen else PixelRed
                                    )
                                }
                            }
                            
                            if (isAvailable) {
                                Button(
                                    onClick = { onNavigateToPostGameDungeon(dungeon) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Enter", color = PixelWhite, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdditionalWorldsTab(
    unlockedWorlds: List<EndgameSystem.AdditionalWorld>,
    onNavigateToAdditionalWorld: (EndgameSystem.AdditionalWorld) -> Unit
) {
    LazyColumn {
        item {
            Text(
                text = "Additional Worlds",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PixelBlue
            )
            Text(
                text = "Unique themed areas with special monsters",
                fontSize = 12.sp,
                color = PixelDarkGray
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        items(EndgameSystem.AdditionalWorld.values().toList()) { world ->
            val isUnlocked = unlockedWorlds.contains(world)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) PixelWhite else PixelLightGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = world.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUnlocked) PixelBlack else PixelDarkGray
                            )
                            Text(
                                text = "Theme: ${world.theme}",
                                fontSize = 12.sp,
                                color = if (isUnlocked) PixelDarkGray else PixelLightGray
                            )
                            Text(
                                text = "Level Range: ${world.levelRange.first}-${world.levelRange.last}",
                                fontSize = 12.sp,
                                color = if (isUnlocked) PixelBlue else PixelDarkGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isUnlocked) "✅ Unlocked" else "🔒 ${world.unlockRequirement}",
                                fontSize = 10.sp,
                                color = if (isUnlocked) PixelGreen else PixelRed
                            )
                        }
                        
                        if (isUnlocked) {
                            Button(
                                onClick = { onNavigateToAdditionalWorld(world) },
                                colors = ButtonDefaults.buttonColors(containerColor = PixelGreen),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Visit", color = PixelWhite, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewGamePlusTab(
    currentPlaythrough: Int,
    playerLevel: Int,
    endgameSystem: EndgameSystem,
    achievements: List<String>,
    onStartNewGamePlus: () -> Unit
) {
    Column {
        Text(
            text = "New Game Plus",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PixelBlue
        )
        Text(
            text = "Start over with bonuses and increased difficulty",
            fontSize = 12.sp,
            color = PixelDarkGray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PixelWhite),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Playthrough: ${currentPlaythrough + 1}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelBlack
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "New Game+ Benefits:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelBlue
                )
                
                val retainedGold = (50 + (currentPlaythrough * 10)).coerceAtMost(90)
                val bonusLevel = (currentPlaythrough * 5).coerceAtMost(25)
                val difficultyIncrease = (currentPlaythrough * 15)
                
                Text("• Retain ${retainedGold}% of current gold", fontSize = 12.sp, color = PixelBlack)
                Text("• Starter monsters begin at level ${bonusLevel + 5}", fontSize = 12.sp, color = PixelBlack)
                Text("• Keep all key items and medals", fontSize = 12.sp, color = PixelBlack)
                Text("• Unlock special breeding options", fontSize = 12.sp, color = PixelBlack)
                
                if (currentPlaythrough >= 1) {
                    Text("• Advanced breeding laboratory access", fontSize = 12.sp, color = PixelGreen)
                }
                if (currentPlaythrough >= 2) {
                    Text("• Choice of legendary starter monster", fontSize = 12.sp, color = PixelGreen)
                }
                if (currentPlaythrough >= 3) {
                    Text("• Master difficulty mode available", fontSize = 12.sp, color = PixelGreen)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Increased Challenge:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PixelRed
                )
                Text("• Enemy monsters ${difficultyIncrease}% stronger", fontSize = 12.sp, color = PixelRed)
                Text("• Enhanced AI battle strategies", fontSize = 12.sp, color = PixelRed)
                Text("• New rare monster variants", fontSize = 12.sp, color = PixelRed)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onStartNewGamePlus,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                    shape = RoundedCornerShape(4.dp),
                    enabled = playerLevel >= 50 // Minimum level requirement
                ) {
                    Text("Start New Game Plus", color = PixelWhite, fontSize = 14.sp)
                }
                
                if (playerLevel < 50) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Requires level 50+ (Current: $playerLevel)",
                        fontSize = 10.sp,
                        color = PixelRed
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendariesTab(
    achievements: List<String>,
    playerLevel: Int
) {
    val legendaryMonsters = listOf(
        "Void Dragon" to "Void Nexus (Level 80+)",
        "Primal Behemoth" to "Primal Depths (Level 85+)",
        "Celestial Phoenix" to "Celestial Tower (Level 90+)",
        "Time Serpent" to "Temporal Maze (Level 95+)",
        "Omega Destroyer" to "Infinity Realm (Level 99+)"
    )
    
    LazyColumn {
        item {
            Text(
                text = "Legendary Monsters",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PixelBlue
            )
            Text(
                text = "Ultra-rare monsters with unique abilities",
                fontSize = 12.sp,
                color = PixelDarkGray
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        items(legendaryMonsters) { (name, location) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = PixelWhite),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PixelBlack
                        )
                        Text(
                            text = location,
                            fontSize = 12.sp,
                            color = PixelDarkGray
                        )
                    }
                    
                    val captured = achievements.contains("Captured_${name.replace(" ", "_")}")
                    Text(
                        text = if (captured) "✅ Captured" else "❌ Not Found",
                        fontSize = 12.sp,
                        color = if (captured) PixelGreen else PixelRed
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PixelLightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Legendary Hunt Tips:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PixelBlue
                    )
                    Text("• Legendary monsters have <1% encounter rates", fontSize = 12.sp, color = PixelBlack)
                    Text("• Each has specific spawn conditions", fontSize = 12.sp, color = PixelBlack)
                    Text("• Time of day and weather affect spawning", fontSize = 12.sp, color = PixelBlack)
                    Text("• Some require other legendaries to be caught first", fontSize = 12.sp, color = PixelBlack)
                    Text("• Use your strongest monsters and best capture items", fontSize = 12.sp, color = PixelBlack)
                }
            }
        }
    }
}