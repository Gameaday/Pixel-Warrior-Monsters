@file:OptIn(ExperimentalMaterial3Api::class)

package com.pixelwarrior.monsters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.game.qol.*
import com.pixelwarrior.monsters.ui.theme.*

/**
 * Quality of Life Screen for achievements, statistics, voice/animation settings
 */
@Composable
fun QualityOfLifeScreen(
    qolSystem: QualityOfLifeSystem,
    onBackPress: () -> Unit
) {
    val achievements by qolSystem.achievements.collectAsState()
    val statistics by qolSystem.statistics.collectAsState()
    val voiceSettings by qolSystem.voiceSettings.collectAsState()
    val animationSettings by qolSystem.animationSettings.collectAsState()
    
    var selectedTab by remember { mutableStateOf(QoLTab.ACHIEVEMENTS) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelDarkBlue)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPress) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = PixelWhite
                )
            }
            
            Text(
                "Quality of Life",
                style = MaterialTheme.typography.headlineMedium,
                color = PixelWhite,
                fontWeight = FontWeight.Bold
            )
            
            // Placeholder for header balance
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QoLTab.values().forEach { tab ->
                QoLTabButton(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTab) {
            QoLTab.ACHIEVEMENTS -> AchievementsContent(achievements, qolSystem)
            QoLTab.STATISTICS -> StatisticsContent(statistics)
            QoLTab.VOICE_SETTINGS -> VoiceSettingsContent(voiceSettings, qolSystem::updateVoiceSettings)
            QoLTab.ANIMATIONS -> AnimationSettingsContent(animationSettings, qolSystem::updateAnimationSettings)
        }
    }
}

@Composable
private fun QoLTabButton(
    tab: QoLTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PixelGold else PixelGray
    val textColor = if (isSelected) PixelBlack else PixelWhite
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(2.dp, PixelWhite, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                tab.icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                tab.title,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AchievementsContent(
    achievements: List<Achievement>,
    qolSystem: QualityOfLifeSystem
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (achievements.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Star,
                    title = "No Achievements Yet",
                    description = "Complete battles, catch monsters, and explore dungeons to unlock achievements!"
                )
            }
        } else {
            items(achievements) { achievement ->
                AchievementCard(achievement = achievement)
            }
        }
        
        // Show some available achievements that haven't been unlocked
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Available Achievements",
                style = MaterialTheme.typography.titleMedium,
                color = PixelGold,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Preview of upcoming achievements
        val upcomingAchievements = listOf(
            "First Victory - Win your first battle",
            "Monster Master - Catch 50 different monsters",
            "Breeding Expert - Successfully breed 25 monsters",
            "Tournament Champion - Win 10 tournaments",
            "Dungeon Explorer - Clear 5 different dungeons"
        )
        
        items(upcomingAchievements) { achievement ->
            UpcomingAchievementCard(description = achievement)
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    val rarityColor = when (achievement.rarity) {
        AchievementRarity.COMMON -> PixelWhite
        AchievementRarity.RARE -> PixelBlue
        AchievementRarity.EPIC -> PixelPurple
        AchievementRarity.LEGENDARY -> PixelGold
        AchievementRarity.MYTHIC -> PixelRed
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, rarityColor, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) PixelGreen else PixelGray
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = PixelBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixelBlack
                    )
                }
                
                Icon(
                    if (achievement.isUnlocked) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (achievement.isUnlocked) "Unlocked" else "Locked",
                    tint = rarityColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            if (!achievement.isUnlocked) {
                val progress = (achievement.progress.toFloat() / achievement.requirement).coerceIn(0f, 1f)
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Progress: ${achievement.progress}/${achievement.requirement}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PixelBlack
                        )
                        Text(
                            "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = PixelBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = rarityColor,
                        trackColor = PixelDarkBlue
                    )
                }
            }
            
            // Reward display
            Spacer(modifier = Modifier.height(8.dp))
            when (val reward = achievement.reward) {
                is AchievementReward.GOLD -> Text(
                    "Reward: ${reward.amount} Gold",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelGold,
                    fontWeight = FontWeight.Bold
                )
                is AchievementReward.ITEM -> Text(
                    "Reward: ${reward.itemName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelBlue,
                    fontWeight = FontWeight.Bold
                )
                is AchievementReward.TITLE -> Text(
                    "Reward: Title \"${reward.titleName}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelPurple,
                    fontWeight = FontWeight.Bold
                )
                is AchievementReward.SPECIAL -> Text(
                    "Reward: ${reward.specialReward}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PixelRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UpcomingAchievementCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelGray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Locked",
                tint = PixelWhite.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = PixelWhite.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StatisticsContent(statistics: GameStatistics) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Battle Statistics
        StatisticsSection(
            title = "Battle Statistics",
            icon = Icons.Default.Shield,
            stats = listOf(
                StatItem("Battles Won", statistics.battlesWon.toString(), PixelGreen),
                StatItem("Battles Lost", statistics.battlesLost.toString(), PixelRed),
                StatItem("Win Rate", "${(statistics.winRate * 100).toInt()}%", PixelGold),
                StatItem("Perfect Battles", statistics.perfectBattles.toString(), PixelPurple)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Collection Statistics
        StatisticsSection(
            title = "Collection Statistics",
            icon = Icons.Default.Pets,
            stats = listOf(
                StatItem("Monsters Caught", statistics.monstersCaught.toString(), PixelBlue),
                StatItem("Monsters Bred", statistics.monstersBred.toString(), PixelGreen),
                StatItem("Synthesis Performed", statistics.synthesisPerformed.toString(), PixelPurple)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Statistics
        StatisticsSection(
            title = "Progress Statistics",
            icon = Icons.Default.TrendingUp,
            stats = listOf(
                StatItem("Dungeons Cleared", statistics.dungeonsCleared.toString(), PixelGold),
                StatItem("Tournaments Won", statistics.tournamentsWon.toString(), PixelRed),
                StatItem("Experience Gained", "${statistics.experienceGained / 1000}K", PixelBlue),
                StatItem("Gold Earned", "${statistics.goldEarned / 1000}K", PixelGold)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time Statistics
        StatisticsSection(
            title = "Time Statistics",
            icon = Icons.Default.Schedule,
            stats = listOf(
                StatItem("Playtime", "${statistics.playtimeMinutes / 60}h ${statistics.playtimeMinutes % 60}m", PixelWhite),
                StatItem("Average Battle Time", "${statistics.averageBattleTime.toInt()}m", PixelGray)
            )
        )
    }
}

@Composable
private fun StatisticsSection(
    title: String,
    icon: ImageVector,
    stats: List<StatItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PixelGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PixelWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            stats.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stat.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixelWhite
                    )
                    Text(
                        stat.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = stat.color,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun VoiceSettingsContent(
    voiceSettings: VoiceSettings,
    onUpdateSettings: (VoiceSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Voice Acting Settings
        SettingsCard(
            title = "Voice Acting",
            icon = Icons.Default.RecordVoiceOver
        ) {
            Column {
                SwitchSetting(
                    label = "Enable Voice Acting",
                    checked = voiceSettings.enableVoiceActing,
                    onCheckedChange = { 
                        onUpdateSettings(voiceSettings.copy(enableVoiceActing = it))
                    }
                )
                
                SliderSetting(
                    label = "Voice Volume",
                    value = voiceSettings.voiceVolume,
                    onValueChange = {
                        onUpdateSettings(voiceSettings.copy(voiceVolume = it))
                    },
                    enabled = voiceSettings.enableVoiceActing
                )
                
                // Language Selection
                Text(
                    "Voice Language",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelWhite,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                VoiceLanguage.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = voiceSettings.enableVoiceActing) {
                                onUpdateSettings(voiceSettings.copy(voiceLanguage = language))
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = voiceSettings.voiceLanguage == language,
                            onClick = null,
                            enabled = voiceSettings.enableVoiceActing,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PixelGold,
                                unselectedColor = PixelWhite
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            language.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (voiceSettings.enableVoiceActing) PixelWhite else PixelGray
                        )
                    }
                }
            }
        }
        
        // Monster Cries Settings
        SettingsCard(
            title = "Monster Cries",
            icon = Icons.Default.Pets
        ) {
            Column {
                SwitchSetting(
                    label = "Enable Monster Cries",
                    checked = voiceSettings.enableMonsterCries,
                    onCheckedChange = { 
                        onUpdateSettings(voiceSettings.copy(enableMonsterCries = it))
                    }
                )
                
                SliderSetting(
                    label = "Effects Volume",
                    value = voiceSettings.effectsVolume,
                    onValueChange = {
                        onUpdateSettings(voiceSettings.copy(effectsVolume = it))
                    },
                    enabled = voiceSettings.enableMonsterCries
                )
            }
        }
    }
}

@Composable
private fun AnimationSettingsContent(
    animationSettings: AnimationSettings,
    onUpdateSettings: (AnimationSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // General Animation Settings
        SettingsCard(
            title = "General Animations",
            icon = Icons.Default.Animation
        ) {
            Column {
                SwitchSetting(
                    label = "Enable Animations",
                    checked = animationSettings.enableAnimations,
                    onCheckedChange = { 
                        onUpdateSettings(animationSettings.copy(enableAnimations = it))
                    }
                )
                
                SliderSetting(
                    label = "Animation Speed",
                    value = animationSettings.animationSpeed,
                    onValueChange = {
                        onUpdateSettings(animationSettings.copy(animationSpeed = it))
                    },
                    enabled = animationSettings.enableAnimations,
                    valueRange = 0.5f..2.0f
                )
            }
        }
        
        // Specific Animation Types
        SettingsCard(
            title = "Animation Types",
            icon = Icons.Default.MovieFilter
        ) {
            Column {
                SwitchSetting(
                    label = "Battle Animations",
                    checked = animationSettings.enableBattleAnimations,
                    onCheckedChange = { 
                        onUpdateSettings(animationSettings.copy(enableBattleAnimations = it))
                    },
                    enabled = animationSettings.enableAnimations
                )
                
                SwitchSetting(
                    label = "UI Animations",
                    checked = animationSettings.enableUIAnimations,
                    onCheckedChange = { 
                        onUpdateSettings(animationSettings.copy(enableUIAnimations = it))
                    },
                    enabled = animationSettings.enableAnimations
                )
                
                SwitchSetting(
                    label = "Particle Effects",
                    checked = animationSettings.enableParticleEffects,
                    onCheckedChange = { 
                        onUpdateSettings(animationSettings.copy(enableParticleEffects = it))
                    },
                    enabled = animationSettings.enableAnimations
                )
            }
        }
        
        // Animation Quality
        SettingsCard(
            title = "Animation Quality",
            icon = Icons.Default.HighQuality
        ) {
            Column {
                Text(
                    "Quality Level",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelWhite,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                AnimationQuality.values().forEach { quality ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = animationSettings.enableAnimations) {
                                onUpdateSettings(animationSettings.copy(animationQuality = quality))
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = animationSettings.animationQuality == quality,
                            onClick = null,
                            enabled = animationSettings.enableAnimations,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PixelGold,
                                unselectedColor = PixelWhite
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            quality.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (animationSettings.enableAnimations) PixelWhite else PixelGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PixelGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PixelWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SwitchSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) PixelWhite else PixelGray
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PixelGold,
                checkedTrackColor = PixelGold.copy(alpha = 0.5f),
                uncheckedThumbColor = PixelWhite,
                uncheckedTrackColor = PixelGray
            )
        )
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) PixelWhite else PixelGray
            )
            Text(
                "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = PixelGold,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = PixelGold,
                activeTrackColor = PixelGold,
                inactiveTrackColor = PixelGray
            )
        )
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PixelGray.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PixelWhite.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = PixelWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = PixelWhite.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper classes
enum class QoLTab(val title: String, val icon: ImageVector) {
    ACHIEVEMENTS("Achievements", Icons.Default.EmojiEvents),
    STATISTICS("Statistics", Icons.Default.Analytics),
    VOICE_SETTINGS("Voice", Icons.Default.RecordVoiceOver),
    ANIMATIONS("Animations", Icons.Default.Animation)
}

data class StatItem(
    val name: String,
    val value: String,
    val color: Color
)