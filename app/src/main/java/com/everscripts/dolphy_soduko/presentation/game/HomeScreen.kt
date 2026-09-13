package com.everscripts.dolphy_soduko.presentation.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.everscripts.dolphy_soduko.presentation.game.components.PremiumActionButton
import com.everscripts.dolphy_soduko.presentation.game.components.PremiumCoinBalance

@Composable
fun HomeScreen(viewModel: GameViewModel) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LivingBackground()

        // 1. Top Header Row: Unified Coin Box (Top Right)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            PremiumCoinBalance(balance = state.starsBalance)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 2. Centered Game Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FISHY",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp
                )
                Text(
                    text = "PUZZLE",
                    color = Color(0xFFB3E5FC),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // 3. Global Progress Bar
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "0",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LEVEL PROGRESS (${state.level - 1}/300)",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "300",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { (state.level - 1).toFloat() / 300f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFF03A9F4), // Theme matching blue
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // 4. Navigation Buttons (Simplified matching theme)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumActionButton(
                    text = "DAILY CHALLENGE",
                    onClick = { viewModel.loadDailyChallenge() },
                    containerColor = Color(0xFF0288D1), // Slightly different blue for challenge
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )

                PremiumActionButton(
                    text = "START GAME",
                    onClick = { viewModel.enterGame() },
                    containerColor = Color.White,
                    contentColor = Color(0xFF0D47A1),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )

                TextButton(
                    onClick = { viewModel.quitApp() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "QUIT GAME",
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
