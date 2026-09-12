package com.everscripts.dolphy_soduko.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import com.everscripts.dolphy_soduko.presentation.game.components.PremiumActionButton
import com.everscripts.dolphy_soduko.presentation.game.components.PremiumCoinBalance

@Composable
fun HomeScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LivingBackground()

        PremiumCoinBalance(
            balance = state.starsBalance,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 42.dp, end = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 22.dp)
                .padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFD166), Color(0xFFFFB300))),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF7A4F00),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "DOLPHY",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp
                )
            }

            Text(
                text = "PUZZLE",
                color = Color(0xFFB8E6FF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 10.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(26.dp))

            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth(0.65f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PROGRESS",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "${state.level - 1} / 300",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PremiumActionButton(
                    text = "DAILY CHALLENGE",
                    icon = Icons.Default.Star,
                    onClick = { viewModel.loadDailyChallenge() },
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                )

                PremiumActionButton(
                    text = "START GAME",
                    icon = Icons.Default.Star,
                    onClick = { viewModel.enterGame() },
                    containerColor = Color.White,
                    contentColor = Color(0xFF0D47A1),
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                )

                OutlinedButton(
                    onClick = { viewModel.quitApp() },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("QUIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.5.sp)
                }
            }
        }
    }
}
