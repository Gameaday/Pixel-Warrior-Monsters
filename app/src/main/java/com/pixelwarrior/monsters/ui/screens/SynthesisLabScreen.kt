package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.data.model.Monster
import com.pixelwarrior.monsters.game.synthesis.*
import com.pixelwarrior.monsters.ui.components.GameComponents
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Synthesis Lab Screen - Advanced Monster Systems from Phase 2
 * Allows players to synthesize monsters and enhance them with the Plus System
 */

@Composable
fun SynthesisLabScreen(
    availableMonsters: List<EnhancedMonster>,
    availableItems: List<String>,
    onSynthesizeMonsters: (EnhancedMonster, EnhancedMonster) -> Unit,
    onEnhanceMonster: (EnhancedMonster) -> Unit,
    onStartScoutMission: (EnhancedMonster, ScoutMissionType) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Synthesis", "Enhancement", "Scout Missions")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkGray)
            .padding(16.dp)
    ) {
        // Header
        GameComponents.GameInfoPanel(
            title = "🧪 Synthesis Laboratory",
            content = "Advanced Monster Research Facility\nCombine, enhance, and deploy monsters"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                Button(
                    onClick = { selectedTab = index },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == index) PixelBlue else PixelGray
                    )
                ) {
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        color = PixelWhite
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> SynthesisTab(
                availableMonsters = availableMonsters,
                availableItems = availableItems,
                onSynthesizeMonsters = onSynthesizeMonsters
            )
            1 -> EnhancementTab(
                availableMonsters = availableMonsters,
                availableItems = availableItems,
                onEnhanceMonster = onEnhanceMonster
            )
            2 -> ScoutMissionTab(
                availableMonsters = availableMonsters,
                onStartScoutMission = onStartScoutMission
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Back Button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PixelRed)
        ) {
            Text("← Back to Hub", color = PixelWhite, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SynthesisTab(
    availableMonsters: List<EnhancedMonster>,
    availableItems: List<String>,
    onSynthesizeMonsters: (EnhancedMonster, EnhancedMonster) -> Unit
) {
    var selectedMonster1 by remember { mutableStateOf<EnhancedMonster?>(null) }
    var selectedMonster2 by remember { mutableStateOf<EnhancedMonster?>(null) }
    var synthesisPreview by remember { mutableStateOf<List<SynthesisPreview>>(emptyList()) }
    
    val monsterSynthesis = remember { MonsterSynthesis() }
    
    // Update synthesis preview when monsters are selected
    LaunchedEffect(selectedMonster1) {
        selectedMonster1?.let { monster1 ->
            val partners = availableMonsters.filter { it != monster1 }
            synthesisPreview = monsterSynthesis.getPossibleSyntheses(monster1, partners)
        } ?: run {
            synthesisPreview = emptyList()
        }
    }
    
    Column {
        Text(
            text = "🧬 Monster Synthesis",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PixelWhite,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Combine two compatible monsters to create powerful new forms",
            fontSize = 12.sp,
            color = PixelLightGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Monster Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First Monster Selection
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "First Parent:",
                    fontSize = 14.sp,
                    color = PixelWhite,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                MonsterSelectionCard(
                    monster = selectedMonster1,
                    availableMonsters = availableMonsters,
                    onMonsterSelected = { selectedMonster1 = it }
                )
            }
            
            // Plus Symbol
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    color = PixelYellow,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Second Monster Selection
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Second Parent:",
                    fontSize = 14.sp,
                    color = PixelWhite,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                MonsterSelectionCard(
                    monster = selectedMonster2,
                    availableMonsters = availableMonsters.filter { it != selectedMonster1 },
                    onMonsterSelected = { selectedMonster2 = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Synthesis Preview
        if (synthesisPreview.isNotEmpty()) {
            Text(
                text = "Available Synthesis Options:",
                fontSize = 14.sp,
                color = PixelWhite,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(synthesisPreview) { preview ->
                    SynthesisPreviewCard(
                        preview = preview,
                        availableItems = availableItems,
                        onSynthesize = {
                            selectedMonster1?.let { m1 ->
                                onSynthesizeMonsters(m1, preview.partner)
                            }
                        }
                    )
                }
            }
        }
        
        // Synthesis Button
        if (selectedMonster1 != null && selectedMonster2 != null) {
            Button(
                onClick = { onSynthesizeMonsters(selectedMonster1!!, selectedMonster2!!) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PixelGreen)
            ) {
                Text("🧬 Synthesize Monsters", color = PixelWhite, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun EnhancementTab(
    availableMonsters: List<EnhancedMonster>,
    availableItems: List<String>,
    onEnhanceMonster: (EnhancedMonster) -> Unit
) {
    var selectedMonster by remember { mutableStateOf<EnhancedMonster?>(null) }
    val plusSystem = remember { PlusSystem() }
    
    Column {
        Text(
            text = "⭐ Monster Enhancement",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PixelWhite,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Enhance monsters with special items to increase their power level",
            fontSize = 12.sp,
            color = PixelLightGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Monster Selection for Enhancement
        Text(
            text = "Select Monster to Enhance:",
            fontSize = 14.sp,
            color = PixelWhite,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier.height(300.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(availableMonsters.filter { it.plusLevel != PlusLevel.PLUS_5 }) { monster ->
                EnhancementMonsterCard(
                    monster = monster,
                    availableItems = availableItems,
                    isSelected = selectedMonster == monster,
                    onSelect = { selectedMonster = it },
                    onEnhance = onEnhanceMonster
                )
            }
        }
        
        // Available Items Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = PixelGray)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Available Enhancement Items:",
                    fontSize = 12.sp,
                    color = PixelWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (availableItems.isNotEmpty()) {
                        availableItems.joinToString(", ")
                    } else {
                        "No enhancement items available"
                    },
                    fontSize = 10.sp,
                    color = PixelLightGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ScoutMissionTab(
    availableMonsters: List<EnhancedMonster>,
    onStartScoutMission: (EnhancedMonster, ScoutMissionType) -> Unit
) {
    var selectedMonster by remember { mutableStateOf<EnhancedMonster?>(null) }
    var selectedMission by remember { mutableStateOf<ScoutMissionType?>(null) }
    val scoutSystem = remember { ScoutSystem() }
    
    Column {
        Text(
            text = "🔍 Scout Missions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PixelWhite,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Send monsters on exploration missions to gather resources and discover new areas",
            fontSize = 12.sp,
            color = PixelLightGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Scout Selection
        Text(
            text = "Select Scout:",
            fontSize = 14.sp,
            color = PixelWhite,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier.height(150.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(availableMonsters.filter { !scoutSystem.isMonsterOnMission(it.baseMonster.id) }) { monster ->
                ScoutSelectionCard(
                    monster = monster,
                    isSelected = selectedMonster == monster,
                    onSelect = { selectedMonster = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mission Selection
        selectedMonster?.let { scout ->
            Text(
                text = "Available Missions:",
                fontSize = 14.sp,
                color = PixelWhite,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val availableMissions = scoutSystem.getAvailableMissions(scout)
            
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(availableMissions) { mission ->
                    ScoutMissionCard(
                        mission = mission,
                        isSelected = selectedMission == mission,
                        onSelect = { selectedMission = it }
                    )
                }
            }
            
            // Start Mission Button
            if (selectedMission != null) {
                Button(
                    onClick = { onStartScoutMission(scout, selectedMission!!) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PixelBlue)
                ) {
                    Text("🔍 Start Scout Mission", color = PixelWhite, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun MonsterSelectionCard(
    monster: EnhancedMonster?,
    availableMonsters: List<EnhancedMonster>,
    onMonsterSelected: (EnhancedMonster?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (monster != null) PixelBlue else PixelGray
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (monster != null) {
                Text(
                    text = "${monster.baseMonster.name} ${monster.plusLevel.displayName}",
                    fontSize = 12.sp,
                    color = PixelWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lv.${monster.baseMonster.level} ${monster.baseMonster.family}",
                    fontSize = 10.sp,
                    color = PixelLightGray
                )
                Text(
                    text = monster.personality.displayName,
                    fontSize = 10.sp,
                    color = PixelYellow
                )
            } else {
                Text(
                    text = "Select Monster...",
                    fontSize = 12.sp,
                    color = PixelLightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    if (expanded) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(PixelDarkGray)
        ) {
            DropdownMenuItem(
                text = { Text("None", color = PixelWhite) },
                onClick = {
                    onMonsterSelected(null)
                    expanded = false
                }
            )
            availableMonsters.forEach { availableMonster ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "${availableMonster.baseMonster.name} ${availableMonster.plusLevel.displayName} (Lv.${availableMonster.baseMonster.level})",
                            color = PixelWhite,
                            fontSize = 11.sp
                        )
                    },
                    onClick = {
                        onMonsterSelected(availableMonster)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SynthesisPreviewCard(
    preview: SynthesisPreview,
    availableItems: List<String>,
    onSynthesize: () -> Unit
) {
    val hasRequiredItem = preview.recipe.requiredItem?.let { availableItems.contains(it) } ?: true
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasRequiredItem) PixelGreen else PixelGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preview.recipe.resultSpeciesId.replace("_", " ").uppercase(),
                    fontSize = 12.sp,
                    color = PixelWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Success Rate: ${(preview.successRate * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = PixelLightGray
                )
                preview.recipe.requiredItem?.let { item ->
                    Text(
                        text = "Requires: $item ${if (hasRequiredItem) "✓" else "✗"}",
                        fontSize = 10.sp,
                        color = if (hasRequiredItem) PixelGreen else PixelRed
                    )
                }
            }
            
            if (hasRequiredItem) {
                Button(
                    onClick = onSynthesize,
                    colors = ButtonDefaults.buttonColors(containerColor = PixelYellow)
                ) {
                    Text("Create", fontSize = 10.sp, color = PixelBlack)
                }
            }
        }
    }
}

@Composable
private fun EnhancementMonsterCard(
    monster: EnhancedMonster,
    availableItems: List<String>,
    isSelected: Boolean,
    onSelect: (EnhancedMonster) -> Unit,
    onEnhance: (EnhancedMonster) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(monster) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PixelBlue else PixelGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${monster.baseMonster.name} ${monster.plusLevel.displayName}",
                    fontSize = 12.sp,
                    color = PixelWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Level ${monster.baseMonster.level} • ${monster.personality.displayName}",
                    fontSize = 10.sp,
                    color = PixelLightGray
                )
                
                val nextLevel = when (monster.plusLevel) {
                    PlusLevel.NORMAL -> PlusLevel.PLUS_1
                    PlusLevel.PLUS_1 -> PlusLevel.PLUS_2
                    PlusLevel.PLUS_2 -> PlusLevel.PLUS_3
                    PlusLevel.PLUS_3 -> PlusLevel.PLUS_4
                    PlusLevel.PLUS_4 -> PlusLevel.PLUS_5
                    PlusLevel.PLUS_5 -> null
                }
                
                nextLevel?.let {
                    Text(
                        text = "Next: ${it.displayName}",
                        fontSize = 10.sp,
                        color = PixelYellow
                    )
                }
            }
            
            if (monster.plusLevel != PlusLevel.PLUS_5) {
                Button(
                    onClick = { onEnhance(monster) },
                    colors = ButtonDefaults.buttonColors(containerColor = PixelYellow)
                ) {
                    Text("Enhance", fontSize = 10.sp, color = PixelBlack)
                }
            }
        }
    }
}

@Composable
private fun ScoutSelectionCard(
    monster: EnhancedMonster,
    isSelected: Boolean,
    onSelect: (EnhancedMonster) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(monster) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PixelBlue else PixelGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${monster.baseMonster.name} ${monster.plusLevel.displayName}",
                    fontSize = 12.sp,
                    color = PixelWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Level ${monster.baseMonster.level} • Agility: ${monster.getEnhancedStats().agility}",
                    fontSize = 10.sp,
                    color = PixelLightGray
                )
                Text(
                    text = monster.personality.displayName,
                    fontSize = 10.sp,
                    color = PixelYellow
                )
            }
            
            Text(
                text = if (isSelected) "✓" else "",
                fontSize = 16.sp,
                color = PixelGreen
            )
        }
    }
}

@Composable
private fun ScoutMissionCard(
    mission: ScoutMissionType,
    isSelected: Boolean,
    onSelect: (ScoutMissionType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mission) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PixelBlue else PixelGray
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = mission.displayName,
                fontSize = 12.sp,
                color = PixelWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Duration: ${mission.duration / 60000}min",
                fontSize = 10.sp,
                color = PixelLightGray
            )
            Text(
                text = "Gold: ${mission.goldReward.first}-${mission.goldReward.last}",
                fontSize = 10.sp,
                color = PixelYellow
            )
            Text(
                text = "Items: ${mission.itemRewards.joinToString(", ")}",
                fontSize = 10.sp,
                color = PixelGreen
            )
        }
    }
}