package com.everscripts.dolphy_soduko.presentation.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everscripts.dolphy_soduko.presentation.game.components.BottleView
import com.everscripts.dolphy_soduko.presentation.game.components.FluidPourStream

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import kotlin.random.Random

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

@Composable
fun GameScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()
    val bottlePositions = remember { mutableStateMapOf<Int, Offset>() }
    val bottleSizes = remember { mutableStateMapOf<Int, IntSize>() }
    var containerOffset by remember { mutableStateOf(Offset.Zero) }
    var showSettings by remember { mutableStateOf(false) }

    val waterColors = listOf(
        "FF2196F3" to Color(0xFF2196F3), // Blue
        "FF009688" to Color(0xFF009688), // Teal
        "FF4CAF50" to Color(0xFF4CAF50), // Green
        "FF673AB7" to Color(0xFF673AB7), // Purple
        "FF3F51B5" to Color(0xFF3F51B5)  // Indigo
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { containerOffset = it.positionInRoot() }
    ) {
        // Living 3D Background
        LivingBackground()

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DOLPHY",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.exitGame() }) {
                        Text("🏠", color = Color.White.copy(alpha = 0.8f), fontSize = 20.sp)
                    }
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(onClick = { viewModel.requestHint() }, enabled = !state.isHintLoading) {
                            Text("💡", color = Color.Yellow, fontSize = 24.sp)
                        }
                        if (state.isHintLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Yellow,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.resetLevel() }) {
                        Text("🔄", color = Color.White.copy(alpha = 0.8f), fontSize = 20.sp)
                    }
                    
                    Box {
                        IconButton(onClick = { showSettings = true }) {
                            Text("⚙️", color = Color.White.copy(alpha = 0.8f), fontSize = 20.sp)
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
            }

            Text(
                text = if (state.isDailyChallenge) "DAILY CHALLENGE" else "LEVEL ${state.level}",
                color = if (state.isDailyChallenge) Color(0xFFFFB300) else Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Game Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // Row 1
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

                // Row 2
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

            Spacer(modifier = Modifier.weight(1.5f))

            if (state.isWon) {
                Button(
                    onClick = { viewModel.nextLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text("NEXT LEVEL", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Pouring Animation Stream
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
