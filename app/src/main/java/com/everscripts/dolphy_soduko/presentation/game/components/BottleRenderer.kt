package com.everscripts.dolphy_soduko.presentation.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.everscripts.dolphy_soduko.model.Bottle
import com.everscripts.dolphy_soduko.model.Segment
import com.everscripts.dolphy_soduko.presentation.theme.GameSkin
import kotlin.math.sin

/**
 * Renders the animated leaping dolphin between bottles.
 */
@Composable
fun FluidPourStream(
    source: Offset,
    target: Offset,
    color: Color,
    targetSlotIndex: Int = 0,
    maxCapacity: Int = 4,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val height = 240.dp.toPx() // Approximation of bottle height
        val flareHeight = height * 0.1f
        val playableHeight = height - flareHeight
        val segmentHeight = playableHeight / maxCapacity
        
        // Calculate exact landing Y: Middle of the specific slot
        val adjustedTarget = Offset(
            target.x,
            target.y + flareHeight + (maxCapacity - targetSlotIndex - 0.5f) * segmentHeight
        )

        // Control points for a high parabolic dive
        val cpX = (source.x + adjustedTarget.x) / 2
        val cpY = minOf(source.y, adjustedTarget.y) - 250.dp.toPx() 
        
        val t = progress.value
        val pos = getPointOnQuadraticBezier(t, source, Offset(cpX, cpY), adjustedTarget)
        val tangent = getTangentOnQuadraticBezier(t, source, Offset(cpX, cpY), adjustedTarget)
        
        // Calculate rotation to face the direction of movement
        val angle = Math.toDegrees(Math.atan2(tangent.y.toDouble(), tangent.x.toDouble())).toFloat()

        // Fade out the leaping fish as it "dives" into the water
        val alpha = if (t > 0.8f) (1f - (t - 0.8f) * 5f).coerceIn(0f, 1f) else 1f

        if (alpha > 0f) {
            rotate(angle, pos) {
                drawDolphy(pos, color.copy(alpha = alpha), size = Size(50.dp.toPx(), 25.dp.toPx()))
            }
        }
    }
}

@Composable
fun BottleView(
    bottle: Bottle,
    skin: GameSkin,
    isSelected: Boolean,
    isHintSource: Boolean = false,
    isHintTarget: Boolean = false,
    hideTopSegments: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bottleAnimations")
    
    // Swimming drift animation
    val swimOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swimOffset"
    )

    // Water wave animation
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val selectionOffset by animateDpAsState(
        targetValue = if (isSelected) (-15).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "selectionOffset"
    )

    Box(
        modifier = modifier
            .offset(y = selectionOffset)
            .graphicsLayer {
                cameraDistance = 12f * density
            }
            .aspectRatio(0.4f)
            .clickable(onClick = onClick)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val flareRatio = 0.15f
            val flareHeight = height * 0.1f
            val bodyWidth = width * (1f - flareRatio)
            val leftX = width * (flareRatio / 2f)
            val rightX = width - leftX
            val cornerRadius = bodyWidth / 2f // Perfect U-shape bottom

            // 0. Ground Shadow
            val shadowY = height + 5.dp.toPx()
            val shadowAlpha = if (isSelected) 0.08f else 0.15f
            drawOval(
                color = Color.Black.copy(alpha = shadowAlpha),
                topLeft = Offset(leftX, shadowY),
                size = Size(bodyWidth, 6.dp.toPx())
            )

            val bottlePath = Path().apply {
                moveTo(0f, 0f)
                quadraticTo(leftX, flareHeight / 2, leftX, flareHeight)
                lineTo(leftX, height - cornerRadius)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(leftX, height - bodyWidth, rightX, height),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false
                )
                lineTo(rightX, flareHeight)
                quadraticTo(rightX, flareHeight / 2, width, 0f)
            }
            
            // 1. Static Hint Highlight (Simple, non-animated)
            val hintColor = when {
                isHintSource -> Color.Yellow
                isHintTarget -> Color.Cyan
                else -> null
            }
            
            if (hintColor != null) {
                drawPath(
                    path = bottlePath,
                    color = hintColor,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            // 2. 3D Glass Body
            val glassBrush = Brush.linearGradient(
                colors = listOf(
                    skin.bottleFillColor.copy(alpha = 0.4f),
                    skin.bottleFillColor.copy(alpha = 0.1f),
                    skin.bottleFillColor.copy(alpha = 0.5f)
                ),
                start = Offset(0f, 0f),
                end = Offset(width, 0f)
            )
            drawPath(path = bottlePath, brush = glassBrush)

            // 3. Static Water (Clipped to bottle shape)
            val waterColor = skin.waterColor
            val waterTopY = flareHeight
            val fullWaterPath = Path().apply {
                moveTo(0f, height)
                lineTo(width, height)
                lineTo(width, waterTopY)
                
                // Wave top
                for (x in width.toInt() downTo 0 step 5) {
                    val y = waterTopY + sin(x * 0.05f + wavePhase) * 3.dp.toPx()
                    lineTo(x.toFloat(), y)
                }
                close()
            }
            
            clipPath(bottlePath) {
                drawPath(
                    path = fullWaterPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(waterColor.copy(alpha = 0.6f), waterColor.copy(alpha = 0.8f)),
                        startY = waterTopY,
                        endY = height
                    )
                )
            }

            // 4. Render Dolphins (Excluding those currently leaping to fix cloning)
            val totalPlayableHeight = height - flareHeight
            val segmentHeight = totalPlayableHeight / bottle.maxCapacity
            
            // Draw visible segments, hiding those currently in leap
            val visibleSegments = bottle.segments.dropLast(hideTopSegments)
            
            visibleSegments.forEachIndexed { index, gameSegment ->
                val segmentY = height - (index + 1) * segmentHeight
                
                // Determine fish visual color (Silhouette for hidden fish)
                val fishColor = if (gameSegment.isHidden) {
                    Color.Black.copy(alpha = 0.4f) // Mystery Fish Silhouette
                } else {
                    skin.getSegmentColor(gameSegment.type)
                }
                
                // Swimming Dolphy Fish
                val fishX = (width / 2) + swimOffset.dp.toPx()
                val fishY = segmentY + (segmentHeight / 2)
                drawDolphy(
                    center = Offset(fishX, fishY),
                    color = fishColor,
                    size = Size(bodyWidth * 0.6f, segmentHeight * 0.4f),
                    isMystery = gameSegment.isHidden
                )
            }

            // 5. Specular Highlights
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(leftX + bodyWidth * 0.15f, flareHeight + 10.dp.toPx()),
                size = Size(2.dp.toPx(), height - flareHeight - 30.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )

            // 6. Bottle Outline
            drawPath(
                path = bottlePath,
                brush = Brush.linearGradient(
                    colors = listOf(skin.bottleStrokeColor, Color.White.copy(alpha = 0.8f), skin.bottleStrokeColor),
                    start = Offset(0f, 0f),
                    end = Offset(width, height)
                ),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Draws a stylized Dolphy (dolphin) fish.
 */
fun DrawScope.drawDolphy(center: Offset, color: Color, size: Size, isMystery: Boolean = false) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        // Body
        moveTo(center.x - w/2, center.y)
        cubicTo(
            center.x - w/4, center.y - h/2,
            center.x + w/4, center.y - h/2,
            center.x + w/2, center.y
        )
        cubicTo(
            center.x + w/4, center.y + h/2,
            center.x - w/4, center.y + h/2,
            center.x - w/2, center.y
        )
        // Tail
        moveTo(center.x - w/2, center.y)
        lineTo(center.x - w*0.7f, center.y - h/3)
        lineTo(center.x - w*0.7f, center.y + h/3)
        close()
        
        // Fin
        moveTo(center.x, center.y - h/4)
        lineTo(center.x + w/8, center.y - h*0.6f)
        lineTo(center.x + w/4, center.y - h/4)
        close()
    }
    drawPath(path = path, color = color)
    
    // Draw Eye (only for non-mystery fish)
    if (!isMystery) {
        drawCircle(
            color = Color.Black.copy(alpha = color.alpha), 
            radius = 2.dp.toPx(), 
            center = Offset(center.x + w/3, center.y - h/8)
        )
    }
}

// Math Helpers for Bezier Path
fun getPointOnQuadraticBezier(t: Float, p0: Offset, p1: Offset, p2: Offset): Offset {
    val u = 1 - t
    val tt = t * t
    val uu = u * u
    val x = uu * p0.x + 2 * u * t * p1.x + tt * p2.x
    val y = uu * p0.y + 2 * u * t * p1.y + tt * p2.y
    return Offset(x, y)
}

fun getTangentOnQuadraticBezier(t: Float, p0: Offset, p1: Offset, p2: Offset): Offset {
    val x = 2 * (1 - t) * (p1.x - p0.x) + 2 * t * (p2.x - p1.x)
    val y = 2 * (1 - t) * (p1.y - p0.y) + 2 * t * (p2.y - p1.y)
    return Offset(x, y)
}
