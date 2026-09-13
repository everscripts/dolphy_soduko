package com.everscripts.dolphy_soduko.presentation.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import com.everscripts.dolphy_soduko.presentation.game.components.BottleView
import com.everscripts.dolphy_soduko.presentation.game.components.FluidPourStream
import com.everscripts.dolphy_soduko.presentation.game.components.PremiumActionButton
import com.everscripts.dolphy_soduko.presentation.game.components.PremiumCoinBalance as SharedCoinBalance
import com.everscripts.dolphy_soduko.presentation.game.components.CoinCollectionAnimation

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import kotlin.random.Random

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()
    val bottlePositions = remember { mutableStateMapOf<Int, Offset>() }
    val bottleSizes = remember { mutableStateMapOf<Int, IntSize>() }
    var containerOffset by remember { mutableStateOf(Offset.Zero) }
    var showSettings by remember { mutableStateOf(false) }
    
    // Target position for flying coins
    var coinBoxOffset by remember { mutableStateOf(Offset.Zero) }
    // Start position for flying coins (center of burst card)
    var burstCardOffset by remember { mutableStateOf(Offset.Zero) }

    val waterColors = listOf(
        "FF2196F3" to Color(0xFF2196F3),
        "FF009688" to Color(0xFF009688),
        "FF4CAF50" to Color(0xFF4CAF50),
        "FF673AB7" to Color(0xFF673AB7),
        "FF3F51B5" to Color(0xFF3F51B5)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { containerOffset = it.positionInRoot() }
    ) {
        LivingBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- REFACTORED HEADER: TWO ROWS ---
            
            // ROW 1: Level & Coin Box
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.isDailyChallenge) "DAILY CHALLENGE" else "LEVEL ${state.level}",
                    color = if (state.isDailyChallenge) Color(0xFFFFD166) else Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                SharedCoinBalance(
                    balance = state.starsBalance,
                    modifier = Modifier
                        .onGloballyPositioned {
                            val pos = it.positionInRoot()
                            coinBoxOffset = Offset(pos.x + it.size.width / 2f, pos.y + it.size.height / 2f)
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ROW 2: Action Icons (Home, Hint, Reload, Settings)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.exitGame() },
                    modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Outlined.Home, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Box(contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = { viewModel.requestHint() },
                        enabled = !state.isHintLoading,
                        modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Lightbulb, null, tint = if (state.isDailyChallenge) Color(0xFFFFD166) else Color.White, modifier = Modifier.size(if (state.isDailyChallenge) 16.dp else 18.dp))
                            if (state.isDailyChallenge) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD166), modifier = Modifier.size(8.dp))
                                    Text("20", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    if (state.isHintLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Color(0xFFFFD166), strokeWidth = 2.dp)
                    }
                }

                IconButton(
                    onClick = { viewModel.resetLevel() },
                    modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Outlined.Refresh, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Box {
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(Icons.Outlined.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    DropdownMenu(
                        expanded = showSettings,
                        onDismissRequest = { showSettings = false },
                        modifier = Modifier
                            .background(Color(0xFF1B262C).copy(alpha = 0.95f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                            .padding(4.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Ambient Music", color = Color.White, fontSize = 14.sp)
                                    Spacer(Modifier.weight(1f))
                                    Switch(checked = state.bgmEnabled, onCheckedChange = { viewModel.toggleBgm(it) })
                                }
                            },
                            onClick = {}
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Haptic Feedback", color = Color.White, fontSize = 14.sp)
                                    Spacer(Modifier.weight(1f))
                                    Switch(checked = state.hapticsEnabled, onCheckedChange = { viewModel.toggleHaptics(it) })
                                }
                            },
                            onClick = {}
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 8.dp))

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("AQUARIUM THEME", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Row {
                                        waterColors.forEach { (hex, color) ->
                                            Box(
                                                modifier = Modifier
                                                    .padding(end = 10.dp)
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(
                                                        width = if (state.waterColorHex == hex) 2.dp else 0.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { viewModel.updateWaterColor(hex) }
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {}
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LEVEL PROGRESS BAR (Bottles Solved)
            val solvedBottles = state.bottles.count { it.isSolved }
            val totalBottles = state.bottles.size
            LinearProgressIndicator(
                progress = { if (totalBottles > 0) solvedBottles.toFloat() / totalBottles.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFFB2EBF2), // Light teal
                trackColor = Color.White.copy(alpha = 0.05f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Game Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.bottles.take(5).forEach { bottle ->
                            val isSource = state.pourAnimation?.sourceId == bottle.id
                            BottleView(
                                bottle = bottle,
                                skin = state.skin,
                                isSelected = state.selectedBottleId == bottle.id,
                                isHintSource = state.hint?.fromId == bottle.id,
                                isHintTarget = state.hint?.toId == bottle.id,
                                hideTopSegments = if (isSource) state.pourAnimation?.segmentsCount ?: 0 else 0,
                                onClick = { viewModel.onBottleClick(bottle.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned {
                                        val pos = it.positionInRoot()
                                        val newOffset = Offset(pos.x - containerOffset.x, pos.y - containerOffset.y)
                                        if (bottlePositions[bottle.id] != newOffset) {
                                            bottlePositions[bottle.id] = newOffset
                                        }
                                        if (bottleSizes[bottle.id] != it.size) {
                                            bottleSizes[bottle.id] = it.size
                                        }
                                    }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.bottles.drop(5).take(5).forEach { bottle ->
                            val isSource = state.pourAnimation?.sourceId == bottle.id
                            BottleView(
                                bottle = bottle,
                                skin = state.skin,
                                isSelected = state.selectedBottleId == bottle.id,
                                isHintSource = state.hint?.fromId == bottle.id,
                                isHintTarget = state.hint?.toId == bottle.id,
                                hideTopSegments = if (isSource) state.pourAnimation?.segmentsCount ?: 0 else 0,
                                onClick = { viewModel.onBottleClick(bottle.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned {
                                        val pos = it.positionInRoot()
                                        val newOffset = Offset(pos.x - containerOffset.x, pos.y - containerOffset.y)
                                        if (bottlePositions[bottle.id] != newOffset) {
                                            bottlePositions[bottle.id] = newOffset
                                        }
                                        if (bottleSizes[bottle.id] != it.size) {
                                            bottleSizes[bottle.id] = it.size
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (state.isWon) {
                PremiumActionButton(
                    text = "NEXT LEVEL",
                    icon = Icons.Default.Star,
                    onClick = { viewModel.nextLevel() },
                    containerColor = Color.White,
                    contentColor = Color(0xFF0D47A1),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )
            }
        }

        // Win Burst Card (Stays in center/top)
        AnimatedVisibility(
            visible = state.coinBurstVisible && state.coinBurstAmount > 0,
            enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center).padding(bottom = 120.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2E45).copy(alpha = 0.95f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD166)),
                modifier = Modifier
                    .shadow(24.dp, RoundedCornerShape(20.dp))
                    .onGloballyPositioned {
                        val pos = it.positionInRoot()
                        burstCardOffset = Offset(pos.x + it.size.width / 2f, pos.y + it.size.height / 2f)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD166), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "+${state.coinBurstAmount} COINS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
            }
        }

        // Flying Coins Animation
        if (state.coinBurstVisible && burstCardOffset != Offset.Zero && coinBoxOffset != Offset.Zero) {
            CoinCollectionAnimation(
                startOffset = burstCardOffset,
                targetOffset = coinBoxOffset,
                onComplete = { viewModel.clearCoinBurst() }
            )
        }

        // Fluid Pour Animation
        state.pourAnimation?.let { anim ->
            val srcPos = bottlePositions[anim.sourceId]
            val srcSize = bottleSizes[anim.sourceId]
            val dstPos = bottlePositions[anim.targetId]
            val dstSize = bottleSizes[anim.targetId]

            if (srcPos != null && srcSize != null && dstPos != null && dstSize != null) {
                val sourceBottle = state.bottles.find { it.id == anim.sourceId }
                val targetBottle = state.bottles.find { it.id == anim.targetId }

                sourceBottle?.topSegment?.let { segment ->
                    FluidPourStream(
                        source = Offset(srcPos.x + srcSize.width / 2, srcPos.y),
                        target = Offset(dstPos.x + dstSize.width / 2, dstPos.y),
                        color = state.skin.getSegmentColor(segment.type),
                        targetSlotIndex = targetBottle?.segments?.size ?: 0,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun LivingBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "livingBackground")
    
    val rayPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "rayPhase"
    )

    // Simplified bubble animation using vertical offset
    val bubbleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "bubbleOffset"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Deep Ocean Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF001524), Color(0xFF073B4C), Color(0xFF118AB2))
            )
        )

        // 2. Procedural God Rays
        for (i in 0 until 4) {
            val offset = (rayPhase + i / 4f) % 1f
            val rayWidth = width * 0.3f
            val rayPath = Path().apply {
                moveTo(width * offset - rayWidth, 0f)
                lineTo(width * offset + rayWidth, 0f)
                lineTo(width * offset + rayWidth * 1.5f, height)
                lineTo(width * offset - rayWidth * 1.5f, height)
                close()
            }
            drawPath(path = rayPath, color = Color.White.copy(alpha = 0.03f))
        }

        // 3. Static/Simply animated bubbles
        for (i in 0 until 15) {
            val x = (i * 0.13f % 1f) * width
            val y = (height - (bubbleOffset + i * 100f) % (height + 200f))
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = 6.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
