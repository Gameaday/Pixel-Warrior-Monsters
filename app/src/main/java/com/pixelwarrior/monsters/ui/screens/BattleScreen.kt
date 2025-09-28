package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelwarrior.monsters.audio.AudioViewModel
import com.pixelwarrior.monsters.data.model.*
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Battle screen that displays the turn-based combat interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleScreen(
    battleState: BattleState,
    onBattleAction: (BattleActionData) -> Unit,
    onBattleEnd: () -> Unit,
    audioViewModel: AudioViewModel
) {
    var selectedAction by remember { mutableStateOf<BattleAction?>(null) }
    var selectedSkill by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelBlack)
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section - Enemy monster
        EnemyMonsterDisplay(
            monster = battleState.enemyMonsters[battleState.currentEnemyMonster],
            modifier = Modifier.weight(0.3f)
        )
        
        // Middle section - Battle log and current action
        BattleLogDisplay(
            lastAction = battleState.lastAction,
            phase = battleState.battlePhase,
            modifier = Modifier.weight(0.2f)
        )
        
        // Bottom section - Player monster and controls
        Column(
            modifier = Modifier.weight(0.5f)
        ) {
            PlayerMonsterDisplay(
                monster = battleState.playerMonsters[battleState.currentPlayerMonster]
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Battle controls
            when (battleState.battlePhase) {
                BattlePhase.SELECTION -> {
                    if (selectedAction == BattleAction.SKILL) {
                        SkillSelectionPanel(
                            skills = battleState.playerMonsters[battleState.currentPlayerMonster].skills,
                            onSkillSelected = { skillId ->
                                audioViewModel.playMenuSelectSound()
                                val actionData = BattleActionData(
                                    action = BattleAction.SKILL,
                                    skillId = skillId,
                                    targetIndex = 0,
                                    actingMonster = battleState.playerMonsters[battleState.currentPlayerMonster],
                                    priority = 0
                                )
                                onBattleAction(actionData)
                                selectedAction = null
                                selectedSkill = null
                            },
                            onBack = { 
                                audioViewModel.playMenuBackSound()
                                selectedAction = null 
                            }
                        )
                    } else {
                        BattleActionsPanel(
                            onAction = { action ->
                                audioViewModel.playMenuSelectSound()
                                when (action) {
                                    BattleAction.SKILL -> {
                                        selectedAction = action
                                    }
                                    else -> {
                                        val actionData = BattleActionData(
                                            action = action,
                                            skillId = null,
                                            targetIndex = 0,
                                            actingMonster = battleState.playerMonsters[battleState.currentPlayerMonster],
                                            priority = 0
                                        )
                                        onBattleAction(actionData)
                                    }
                                }
                            }
                        )
                    }
                }
                BattlePhase.ANIMATION, BattlePhase.RESOLUTION -> {
                    BattleProcessingDisplay()
                }
                BattlePhase.VICTORY -> {
                    BattleResultDisplay(
                        result = "Victory!",
                        onContinue = onBattleEnd
                    )
                }
                BattlePhase.DEFEAT -> {
                    BattleResultDisplay(
                        result = "Defeat...",
                        onContinue = onBattleEnd
                    )
                }
                BattlePhase.MONSTER_JOINED -> {
                    BattleResultDisplay(
                        result = "Monster Wants to Join!",
                        onContinue = onBattleEnd
                    )
                }
            }
        }
    }
}

@Composable
fun EnemyMonsterDisplay(
    monster: Monster,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = monster.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Enemy monster placeholder (would show sprite)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(PixelBlue, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monster.name.first().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // HP bar
                MonsterHealthBar(
                    currentHp = monster.currentHp,
                    maxHp = monster.currentStats.maxHp,
                    showNumbers = false
                )
            }
        }
    }
}

@Composable
fun PlayerMonsterDisplay(
    monster: Monster,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Monster sprite placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(PixelGreen, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = monster.name.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = monster.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // HP bar
                MonsterHealthBar(
                    currentHp = monster.currentHp,
                    maxHp = monster.currentStats.maxHp,
                    showNumbers = true
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // MP bar
                MonsterManaBar(
                    currentMp = monster.currentMp,
                    maxMp = monster.currentStats.maxMp,
                    showNumbers = true
                )
            }
        }
    }
}

@Composable
fun MonsterHealthBar(
    currentHp: Int,
    maxHp: Int,
    showNumbers: Boolean,
    modifier: Modifier = Modifier
) {
    val hpPercentage = if (maxHp > 0) currentHp.toFloat() / maxHp else 0f
    
    Column(modifier = modifier) {
        if (showNumbers) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
                Text(
                    text = "$currentHp / $maxHp",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(PixelBlack, RoundedCornerShape(4.dp))
                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(hpPercentage)
                    .fillMaxHeight()
                    .background(HpRed, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun MonsterManaBar(
    currentMp: Int,
    maxMp: Int,
    showNumbers: Boolean,
    modifier: Modifier = Modifier
) {
    val mpPercentage = if (maxMp > 0) currentMp.toFloat() / maxMp else 0f
    
    Column(modifier = modifier) {
        if (showNumbers) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
                Text(
                    text = "$currentMp / $maxMp",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(PixelBlack, RoundedCornerShape(4.dp))
                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(mpPercentage)
                    .fillMaxHeight()
                    .background(MpBlue, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun BattleLogDisplay(
    lastAction: String,
    phase: BattlePhase,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (phase) {
                    BattlePhase.SELECTION -> "Choose your action!"
                    BattlePhase.ANIMATION -> lastAction.ifEmpty { "Battle in progress..." }
                    BattlePhase.RESOLUTION -> "Calculating results..."
                    BattlePhase.VICTORY -> "You won the battle!"
                    BattlePhase.DEFEAT -> "You were defeated..."
                    BattlePhase.MONSTER_JOINED -> "Monster wants to join your party!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BattleActionsPanel(
    onAction: (BattleAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BattleActionButton(
            text = "Attack",
            onClick = { onAction(BattleAction.ATTACK) },
            modifier = Modifier.weight(1f)
        )
        BattleActionButton(
            text = "Skills",
            onClick = { onAction(BattleAction.SKILL) },
            modifier = Modifier.weight(1f)
        )
        BattleActionButton(
            text = "Defend",
            onClick = { onAction(BattleAction.DEFEND) },
            modifier = Modifier.weight(1f)
        )
        BattleActionButton(
            text = "Run",
            onClick = { onAction(BattleAction.RUN) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SkillSelectionPanel(
    skills: List<String>,
    onSkillSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Choose a skill:",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            skills.forEach { skillId ->
                BattleActionButton(
                    text = skillId.replaceFirstChar { it.uppercase() },
                    onClick = { onSkillSelected(skillId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
            
            BattleActionButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun BattleActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = PixelBlue,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BattleProcessingDisplay(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = PixelBlue,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun BattleResultDisplay(
    result: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = result,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BattleActionButton(
                text = "Continue",
                onClick = onContinue,
                modifier = Modifier.width(120.dp)
            )
        }
    }
}