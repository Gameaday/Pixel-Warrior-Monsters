package com.pixelwarrior.monsters.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelwarrior.monsters.data.model.MonsterType
import com.pixelwarrior.monsters.ui.theme.*

/**
 * DQM-style window frame component
 */
@Composable
fun DQMWindow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(2.dp, DQMRoyalBlue, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = PixelDarkGray.copy(alpha = 0.95f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DQMDeepBlue.copy(alpha = 0.3f),
                            PixelDarkGray.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            content()
        }
    }
}

/**
 * Classic DQM-style button
 */
@Composable
fun DQMButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = true
) {
    val buttonColor = if (primary) DQMRoyalBlue else DQMForestGreen
    val textColor = if (enabled) PixelWhite else PixelGray
    
    Button(
        onClick = onClick,
        modifier = modifier
            .border(1.dp, if (enabled) buttonColor.copy(alpha = 0.8f) else PixelGray, RoundedCornerShape(6.dp)),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = PixelDarkGray
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/**
 * DQM-style stat bar (HP, MP, Experience, Affection)
 */
@Composable
fun DQMStatBar(
    label: String,
    current: Int,
    maximum: Int,
    barColor: Color,
    modifier: Modifier = Modifier,
    showNumbers: Boolean = true
) {
    val percentage = if (maximum > 0) current.toFloat() / maximum.toFloat() else 0f
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = PixelWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (showNumbers) {
                Text(
                    text = "$current/$maximum",
                    color = PixelLightGray,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(PixelBlack, RoundedCornerShape(4.dp))
                .border(1.dp, PixelGray, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage.coerceIn(0f, 1f))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                barColor,
                                barColor.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * DQM-style type chip for monster types
 */
@Composable
fun DQMTypeChip(
    type: MonsterType,
    modifier: Modifier = Modifier
) {
    val typeColor = when (type) {
        MonsterType.NORMAL -> TypeNormal
        MonsterType.FIRE -> TypeFire
        MonsterType.WATER -> TypeWater
        MonsterType.GRASS -> TypeGrass
        MonsterType.ELECTRIC -> TypeElectric
        MonsterType.ICE -> TypeIce
        MonsterType.FIGHTING -> TypeFighting
        MonsterType.POISON -> TypePoison
        MonsterType.GROUND -> TypeGround
        MonsterType.FLYING -> TypeFlying
        MonsterType.PSYCHIC -> TypePsychic
        MonsterType.BUG -> TypeBug
        MonsterType.ROCK -> TypeRock
        MonsterType.GHOST -> TypeGhost
        MonsterType.DRAGON -> TypeDragon
        MonsterType.DARK -> TypeDark
        MonsterType.STEEL -> TypeSteel
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = typeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = type.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = PixelWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * DQM-style title header
 */
@Composable
fun DQMTitleHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = DQMGoldenYellow,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = PixelLightGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Animated DQM-style loading indicator
 */
@Composable
fun DQMLoadingIndicator(
    modifier: Modifier = Modifier,
    message: String = "Loading..."
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(DQMRoyalBlue, DQMDeepBlue, PixelBlack),
                        radius = 24f
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(2.dp, DQMGoldenYellow, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐉",
                fontSize = 24.sp,
                modifier = Modifier
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            color = PixelWhite,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * DQM-style affection heart display
 */
@Composable
fun DQMAffectionDisplay(
    affection: Int,
    modifier: Modifier = Modifier
) {
    val hearts = (affection / 20).coerceIn(0, 5) // 0-5 hearts based on affection 0-100
    val heartColor = when {
        affection >= 80 -> AffectionPink
        affection >= 60 -> DQMAmber
        affection >= 40 -> DQMGoldenYellow
        affection >= 20 -> PixelLightGray
        else -> PixelDarkGray
    }
    
    Row(modifier = modifier) {
        repeat(5) { index ->
            Text(
                text = if (index < hearts) "♥" else "♡",
                color = if (index < hearts) heartColor else PixelDarkGray,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 1.dp)
            )
        }
    }
}

/**
 * DQM-style synthesis energy indicator
 */
@Composable
fun DQMSynthesisEnergyIndicator(
    energy: Int,
    maxEnergy: Int,
    modifier: Modifier = Modifier
) {
    val energyPercentage = if (maxEnergy > 0) energy.toFloat() / maxEnergy else 0f
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⚡",
            color = DQMGoldenYellow,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(6.dp)
                .background(PixelBlack, RoundedCornerShape(3.dp))
                .border(1.dp, DQMGoldenYellow, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(energyPercentage.coerceIn(0f, 1f))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(DQMGoldenYellow, DQMAmber)
                        ),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = "$energy/$maxEnergy",
            color = PixelWhite,
            fontSize = 10.sp
        )
    }
}