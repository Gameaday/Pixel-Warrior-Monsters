package com.pixelwarrior.monsters.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import com.pixelwarrior.monsters.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Reusable UI components for the game
 */

/**
 * Game message dialog for showing temporary messages
 */
@Composable
fun GameMessageDialog(
    message: String?,
    onDismiss: () -> Unit
) {
    if (message != null) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Auto-dismiss after 2 seconds
        LaunchedEffect(message) {
            delay(2000)
            onDismiss()
        }
    }
}

/**
 * Loading screen component
 */
@Composable
fun LoadingScreen(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PixelBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = PixelBlue,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

/**
 * Experience bar component
 */
@Composable
fun ExperienceBar(
    currentExp: Long,
    expToNext: Long,
    modifier: Modifier = Modifier
) {
    val expPercentage = if (expToNext > 0) currentExp.toFloat() / expToNext else 0f
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "EXP",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
            Text(
                text = "$currentExp / $expToNext",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(PixelBlack, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(expPercentage.coerceAtMost(1f))
                    .fillMaxHeight()
                    .background(ExpYellow, RoundedCornerShape(3.dp))
            )
        }
    }
}

/**
 * Monster type badge component
 */
@Composable
fun MonsterTypeBadge(
    type: com.pixelwarrior.monsters.data.model.MonsterType,
    modifier: Modifier = Modifier
) {
    val typeColor = when (type) {
        com.pixelwarrior.monsters.data.model.MonsterType.FIRE -> Color(0xFFF08030)
        com.pixelwarrior.monsters.data.model.MonsterType.WATER -> Color(0xFF6890F0)
        com.pixelwarrior.monsters.data.model.MonsterType.GRASS -> Color(0xFF78C850)
        com.pixelwarrior.monsters.data.model.MonsterType.ELECTRIC -> Color(0xFFF8D030)
        com.pixelwarrior.monsters.data.model.MonsterType.ICE -> Color(0xFF98D8D8)
        com.pixelwarrior.monsters.data.model.MonsterType.FIGHTING -> Color(0xFFC03028)
        com.pixelwarrior.monsters.data.model.MonsterType.POISON -> Color(0xFFA040A0)
        com.pixelwarrior.monsters.data.model.MonsterType.GROUND -> Color(0xFFE0C068)
        com.pixelwarrior.monsters.data.model.MonsterType.FLYING -> Color(0xFFA890F0)
        com.pixelwarrior.monsters.data.model.MonsterType.PSYCHIC -> Color(0xFFF85888)
        com.pixelwarrior.monsters.data.model.MonsterType.BUG -> Color(0xFFA8B820)
        com.pixelwarrior.monsters.data.model.MonsterType.ROCK -> Color(0xFFB8A038)
        com.pixelwarrior.monsters.data.model.MonsterType.GHOST -> Color(0xFF705898)
        com.pixelwarrior.monsters.data.model.MonsterType.DRAGON -> Color(0xFF7038F8)
        com.pixelwarrior.monsters.data.model.MonsterType.DARK -> Color(0xFF705848)
        com.pixelwarrior.monsters.data.model.MonsterType.STEEL -> Color(0xFFB8B8D0)
        com.pixelwarrior.monsters.data.model.MonsterType.NORMAL -> Color(0xFFA8A878)
    }
    
    Box(
        modifier = modifier
            .background(typeColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = type.name,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Game info panel showing player stats
 */
@Composable
fun GameInfoPanel(
    playerName: String,
    gold: Long,
    playtime: Long,
    partySize: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Gold", gold.toString())
                InfoItem("Party", "$partySize/4")
            }
            
            InfoItem("Playtime", formatPlaytime(playtime))
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PixelLightGray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatPlaytime(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

/**
 * Confirmation dialog
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = PixelDarkGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelLightGray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = PixelGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(cancelText, color = Color.White)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = PixelBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(confirmText, color = Color.White)
                    }
                }
            }
        }
    }
}