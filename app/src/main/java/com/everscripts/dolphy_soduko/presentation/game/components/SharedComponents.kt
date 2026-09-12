package com.everscripts.dolphy_soduko.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFFB300),
    contentColor: Color = Color.White,
    iconTint: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = iconTint
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 1.2.sp
              )
        }
    }
}

@Composable
fun PremiumCoinBalance(balance: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2233).copy(alpha = 0.88f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD166).copy(alpha = 0.7f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFFD166), Color(0xFFFFB300), Color(0xFF7A4F00))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "COINS",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "$balance",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
