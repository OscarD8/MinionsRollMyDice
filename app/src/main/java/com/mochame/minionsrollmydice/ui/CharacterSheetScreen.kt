package com.mochame.minionsrollmydice.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mochame.minionsrollmydice.domain.AttributeType
import kotlinx.coroutines.delay

@Composable
fun CharacterSheetScreen(
    viewModel: CharacterSheetViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            delay(3000)
            viewModel.resetSuccessState()
        }
    }

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
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "ATTRIBUTES & SKILLS",
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

            Text(
                text = "Spend your 5 attribute points to empower wisdom, insight, and inspired skills. The totals directly increase your Honour Die outcomes.",
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif,
                color = TextDimGothic,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Budget Remaining Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(CardIron, BgObsidian)
                        )
                    )
                    .border(2.dp, HonourGold, CircleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.attributes.remainingPoints().toString(),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = HonourGold
                    )
                    Text(
                        text = "UNSPENT PTS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TextDimGothic
                    )
                }
            }

            // Attributes Cards List
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AttributeRow(
                    name = "Wisdom",
                    description = "Augments tactical combat metrics and magical outcomes.",
                    value = state.attributes.wisdom,
                    canIncrement = state.attributes.canIncrement(AttributeType.WISDOM),
                    canDecrement = state.attributes.canDecrement(AttributeType.WISDOM),
                    onIncrement = { viewModel.incrementAttribute(AttributeType.WISDOM) },
                    onDecrement = { viewModel.decrementAttribute(AttributeType.WISDOM) }
                )

                AttributeRow(
                    name = "Insight",
                    description = "Amplifies perception, passive traps discovery, and checks.",
                    value = state.attributes.insight,
                    canIncrement = state.attributes.canIncrement(AttributeType.INSIGHT),
                    canDecrement = state.attributes.canDecrement(AttributeType.INSIGHT),
                    onIncrement = { viewModel.incrementAttribute(AttributeType.INSIGHT) },
                    onDecrement = { viewModel.decrementAttribute(AttributeType.INSIGHT) }
                )

                AttributeRow(
                    name = "Inspired",
                    description = "Grants divine rolls, crit safety thresholds, and inspiration.",
                    value = state.attributes.inspired,
                    canIncrement = state.attributes.canIncrement(AttributeType.INSPIRED),
                    canDecrement = state.attributes.canDecrement(AttributeType.INSPIRED),
                    onIncrement = { viewModel.incrementAttribute(AttributeType.INSPIRED) },
                    onDecrement = { viewModel.decrementAttribute(AttributeType.INSPIRED) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error display
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color(0xFFCF6679),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Save Button
            Button(
                onClick = { viewModel.saveAttributes() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.5.dp, HonourGold, RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(CardIron, CardGlow)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(color = HonourGold, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = "ENGRAVE ATTRIBUTES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = HonourGold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // Success Toast Notification overlay
        AnimatedVisibility(
            visible = state.saveSuccess,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(CardIron, RoundedCornerShape(8.dp))
                    .border(1.dp, HonourGold, RoundedCornerShape(8.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = HonourGold
                )
                Text(
                    text = "Character attributes saved to the registry!",
                    color = TextGothic,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AttributeRow(
    name: String,
    description: String,
    value: Int,
    canIncrement: Boolean,
    canDecrement: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardIron),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AncientBronze.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .shadow(6.dp, shape = RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = HonourGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = TextDimGothic
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Decrement Button
                IconButton(
                    onClick = onDecrement,
                    enabled = canDecrement,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (canDecrement) CardGlow else Color.Transparent
                        )
                        .border(1.dp, if (canDecrement) HonourGold else AncientBronze.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = if (canDecrement) HonourGold else TextDimGothic.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Point value
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.width(32.dp)
                ) {
                    Text(
                        text = value.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = if (value > 0) GlowingAmber else TextDimGothic
                    )
                }

                // Increment Button
                IconButton(
                    onClick = onIncrement,
                    enabled = canIncrement,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (canIncrement) CardGlow else Color.Transparent
                        )
                        .border(1.dp, if (canIncrement) HonourGold else AncientBronze.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = if (canIncrement) HonourGold else TextDimGothic.copy(alpha = 0.2f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
