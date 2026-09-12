package com.everscripts.dolphy_soduko.presentation.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CoinCollectionAnimation(
    startOffset: Offset,
    targetOffset: Offset,
    onComplete: () -> Unit
) {
    val coinCount = 8
    val scope = rememberCoroutineScope()
    
    // We create state for each coin's position and alpha
    val coins = remember { List(coinCount) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        coins.forEachIndexed { index, animatable ->
            scope.launch {
                delay(index * 80L) // Staggered start
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
                if (index == coinCount - 1) {
                    onComplete()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        coins.forEach { anim ->
            val progress = anim.value
            if (progress > 0f && progress < 1f) {
                // Calculate current position with some curve/burst logic
                // Initial burst outward, then glide to target
                val burstX = (progress * 2f).coerceAtMost(1f)
                val lerpX = startOffset.x + (targetOffset.x - startOffset.x) * progress
                val lerpY = startOffset.y + (targetOffset.y - startOffset.y) * progress
                
                // Add a little randomness to individual paths if desired
                val offsetX = (1f - progress) * (Random.nextInt(-100, 100))
                val offsetY = (1f - progress) * (Random.nextInt(-100, 100))

                Box(
                    modifier = Modifier
                        .offset { IntOffset((lerpX + offsetX).toInt(), (lerpY + offsetY).toInt()) }
                        .size(24.dp)
                        .scale(0.5f + progress * 0.5f)
                        .alpha(1f - (progress * 0.2f))
                        .background(Color(0xFFFFD166), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF7A4F00),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
