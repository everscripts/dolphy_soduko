package com.everscripts.dolphy_soduko.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.everscripts.dolphy_soduko.model.Segment

interface GameSkin {
    val name: String
    val backgroundBrush: Brush
    val bottleStrokeColor: Color
    val bottleFillColor: Color
    val waterColor: Color
    
    fun getSegmentColor(segment: Segment): Color
}

object DolphySkin : GameSkin {
    override val name: String = "DOLPHY"
    override val backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2))
    )
    override val bottleStrokeColor: Color = Color.White.copy(alpha = 0.6f)
    override val bottleFillColor: Color = Color.White.copy(alpha = 0.1f)
    override var waterColor: Color = Color(0xFF2196F3)

    override fun getSegmentColor(segment: Segment): Color = segment.color
}

object JellyFishSkin : GameSkin {
    override val name: String = "JELLYFISH"
    override val backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))
    )
    override val bottleStrokeColor: Color = Color(0xFFFF4081).copy(alpha = 0.6f)
    override val bottleFillColor: Color = Color.White.copy(alpha = 0.05f)
    override var waterColor: Color = Color(0xFF00BCD4)

    override fun getSegmentColor(segment: Segment): Color = segment.color.copy(alpha = 0.8f)
}

object SkinManager {
    fun getSkin(name: String, waterColor: Color? = null): GameSkin {
        val skin = when (name) {
            "JELLYFISH" -> JellyFishSkin
            else -> DolphySkin
        }
        waterColor?.let {
            if (skin is DolphySkin) skin.waterColor = it
            if (skin is JellyFishSkin) skin.waterColor = it
        }
        return skin
    }
}
