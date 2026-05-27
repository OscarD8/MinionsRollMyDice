package com.mochame.minionsrollmydice.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mochame.minionsrollmydice.R
import com.mochame.minionsrollmydice.domain.DiceRollerStateMachine
import com.mochame.minionsrollmydice.domain.RollResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Baldur's Gate 3 Metallic Honour Mode Color Palette
val BgObsidian = Color(0xFF0C0C0F)      // Deepest black-purple stone
val CardIron = Color(0xFF16161D)        // Wrought iron card background
val CardGlow = Color(0xFF22222E)        // Slightly brighter iron under selection
val HonourGold = Color(0xFFDFB15B)      // Golden Die of Honour Gold
val AncientBronze = Color(0xFF8C6221)   // Dark bronze metal trim
val GlowingAmber = Color(0xFFFF9E00)    // Fiery orange/amber glow
val LightGlint = Color(0xFFFFF7E6)      // Bright ivory metal glint
val TextGothic = Color(0xFFE2E2EA)      // Antique white font
val TextDimGothic = Color(0xFF9090A0)   // Faded silver description font

@Composable
fun DiceRollerScreen(
    viewModel: DiceRollerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedDiceSides by remember { mutableStateOf(20) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium Header with Gothic engraving look
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "HONOUR DICE ROLLER",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    color = HonourGold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Decorative golden line
                Canvas(modifier = Modifier.fillMaxWidth(0.6f).height(2.dp)) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, HonourGold, Color.Transparent)
                        ),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Modifier Engraving Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardIron)
                    .border(1.dp, AncientBronze.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.OfflineBolt,
                    contentDescription = "Modifier",
                    tint = GlowingAmber,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ACTIVE ROLL MODIFIER: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = TextDimGothic
                )
                Text(
                    text = "+${state.attributes.calculateTotalModifier()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HonourGold
                )
                Text(
                    text = " (W:${state.attributes.wisdom} I:${state.attributes.insight} P:${state.attributes.inspired})",
                    fontSize = 10.sp,
                    color = TextDimGothic
                )
            }

            // Real 3D-feeling Dice Tumble Panel
            RealDiceTumblePanel(
                state = state.diceState,
                sides = selectedDiceSides,
                onRollClick = { viewModel.rollDice(selectedDiceSides) }
            )

            // Dynamic Dice Selector Chips
            Text(
                text = "SELECT DIE TYPE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = HonourGold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(4, 6, 8, 10, 12, 20).forEach { sides ->
                    DiceChoiceChip(
                        label = "D$sides",
                        isSelected = selectedDiceSides == sides,
                        onClick = { selectedDiceSides = sides }
                    )
                }
            }

            // Modern Compose Horizontal Divider (Replacing deprecated Divider)
            HorizontalDivider(
                color = CardIron,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Engraved Roll History List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHRONICLE OF ROLLS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = HonourGold,
                    letterSpacing = 1.sp
                )
                if (state.diceState.history.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.clearHistory() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear",
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PURGE RECORDS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFCF6679)
                        )
                    }
                }
            }

            // History lazy column
            if (state.diceState.history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "The chronicle is blank. Cast the Honour Die to begin.",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif,
                        color = TextDimGothic.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(state.diceState.history, key = { it.id }) { item ->
                        ChronicleRow(result = item)
                    }
                }
            }
        }
    }
}

@Composable
fun RealDiceTumblePanel(
    state: DiceRollerStateMachine.State,
    sides: Int,
    onRollClick: () -> Unit
) {
    val isRolling = state is DiceRollerStateMachine.State.Rolling

    // Infinite bouncing and scaling transitions to simulate tumbling physics
    val infiniteTransition = rememberInfiniteTransition()
    
    val rollScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rollRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val rollBounceY by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = EaseInOutBounce),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardIron),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .border(1.dp, AncientBronze.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(enabled = !isRolling, onClick = onRollClick)
            .shadow(16.dp, shape = RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CardIron, BgObsidian)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is DiceRollerStateMachine.State.Idle -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(105.dp)) {
                            RealDiceRenderer(
                                sides = sides,
                                value = sides,
                                rotation = 0f,
                                scale = 1f,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "CAST THE HONOR DIE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = HonourGold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
                is DiceRollerStateMachine.State.Rolling -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .offset(y = rollBounceY.dp)
                        ) {
                            RealDiceRenderer(
                                sides = state.diceSides,
                                value = state.tempValue,
                                rotation = rollRotation,
                                scale = rollScale,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "THE FATES ARE SPINNING...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = GlowingAmber,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
                is DiceRollerStateMachine.State.Rolled -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(modifier = Modifier.size(110.dp)) {
                            RealDiceRenderer(
                                sides = sides,
                                value = state.baseRoll,
                                rotation = 0f,
                                scale = 1f,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Gothic Scoreboard
                        Column {
                            Text(
                                text = "ROLL SUCCESS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = HonourGold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.finalTotal} TOTAL",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif,
                                color = LightGlint
                            )
                            Text(
                                text = "Base Die: ${state.baseRoll} (D$sides)",
                                fontSize = 12.sp,
                                color = TextGothic
                            )
                            Text(
                                text = "Attribute Mod: +${state.modifier}",
                                fontSize = 12.sp,
                                color = GlowingAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap die to cast again",
                                fontSize = 9.sp,
                                color = TextDimGothic,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern High-Fidelity Sprite Drawing Engine loading custom 3D Honour Mode Sprites
 * instead of relying on manual canvas drawings, updating seamlessly to match active selection.
 */
@Composable
fun RealDiceRenderer(
    sides: Int,
    value: Int,
    rotation: Float,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (sides) {
        4 -> R.drawable.d4_honour
        6 -> R.drawable.d6_honour
        8 -> R.drawable.d8_honour
        10 -> R.drawable.d10_honour
        12 -> R.drawable.d12_honour
        else -> R.drawable.d20_honour
    }

    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = "3D Golden Die D$sides",
        modifier = modifier
            .scale(scale)
            .rotate(rotation)
    )
}

@Composable
fun DiceChoiceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(52.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) HonourGold else CardIron)
            .border(1.dp, if (isSelected) HonourGold else AncientBronze.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = if (isSelected) BgObsidian else TextGothic
        )
    }
}

@Composable
fun ChronicleRow(result: RollResult) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateString = formatter.format(Date(result.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = CardIron.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AncientBronze.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ROLL TOTAL: ",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Serif,
                        color = TextDimGothic
                    )
                    Text(
                        text = "${result.total}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = HonourGold
                    )
                }
                Text(
                    text = "Base: ${result.baseRoll} | Injected Mod: +${result.modifier}",
                    fontSize = 11.sp,
                    color = TextDimGothic
                )
            }
            Text(
                text = dateString,
                fontSize = 11.sp,
                color = TextDimGothic.copy(alpha = 0.5f)
            )
        }
    }
}
