package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Reusable standard styling values
val GlassCornerRadius = 16.dp
val GlassCardShape = RoundedCornerShape(GlassCornerRadius)
val PillShape = RoundedCornerShape(9999.dp)

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = GlassCardShape,
    borderStroke: BorderStroke? = BorderStroke(1.dp, Color(0x33FFFFFF)), // top-edge reflection
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1F171F33) // 12% opacity Deep Indigo/Slate
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = PillShape,
    contentColor: Color = Color(0xFFB8C3FF),
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0x2E2E5BFF), // Semi-translucent dark blue
            contentColor = contentColor,
            disabledContainerColor = Color(0x12FFFFFF),
            disabledContentColor = Color(0x4DDAE2FD)
        ),
        border = BorderStroke(1.dp, Color(0x33B8C3FF)),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        content()
    }
}

@Composable
fun GlowingGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2E5BFF), Color(0xFF124AF0))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(if (enabled) gradient else Brush.horizontalGradient(listOf(Color(0x26FFFFFF), Color(0x26FFFFFF))))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color(0xFFEFEFFF) else Color(0x66DAE2FD),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
