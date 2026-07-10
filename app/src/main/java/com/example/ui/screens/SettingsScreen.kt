package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassPanel
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val speed by viewModel.speakingSpeed.collectAsState()
    val pitch by viewModel.pitchVariance.collectAsState()
    val profile by viewModel.activeVoiceProfile.collectAsState()
    val autoRecord by viewModel.autoRecord.collectAsState()

    var speedSliderVal by remember { mutableStateOf(speed) }
    var pitchSliderVal by remember { mutableStateOf(pitch) }
    var selectedProfile by remember { mutableStateOf(profile) }
    var autoRecordToggle by remember { mutableStateOf(autoRecord) }

    // Waveform bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1326))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HISTORY) }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFB8C3FF)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Voice Settings",
                color = Color(0xFFB8C3FF),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Permission Status Panel
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x1FB8C3FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Mic Status",
                                tint = Color(0xFFB8C3FF)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Microphone Status",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Authorized & Ready",
                                color = Color(0xFF8E90A2),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Green pulsing status bubble
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x1F81C784))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF81C784))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Voice Tuning Controls
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Voice Tuning",
                        color = Color(0xFFB8C3FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Slider 1: Speed
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speaking Speed", color = Color(0xFFC4C5D9), fontSize = 14.sp)
                            Text(
                                String.format(java.util.Locale.US, "%.1fx", speedSliderVal),
                                color = Color(0xFFB8C3FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Slider(
                            value = speedSliderVal,
                            onValueChange = { speedSliderVal = it },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFB8C3FF),
                                activeTrackColor = Color(0xFF2E5BFF),
                                inactiveTrackColor = Color(0x338E90A2)
                            ),
                            modifier = Modifier.testTag("speed_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Slower", color = Color(0xFF8E90A2), fontSize = 10.sp)
                            Text("Normal", color = Color(0xFF8E90A2), fontSize = 10.sp)
                            Text("Faster", color = Color(0xFF8E90A2), fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Slider 2: Pitch
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pitch Variance", color = Color(0xFFC4C5D9), fontSize = 14.sp)
                            Text(
                                if (pitchSliderVal == 1.0f) "Default" else String.format(java.util.Locale.US, "%.1fx", pitchSliderVal),
                                color = Color(0xFFB8C3FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Slider(
                            value = pitchSliderVal,
                            onValueChange = { pitchSliderVal = it },
                            valueRange = 0.5f..1.5f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFB8C3FF),
                                activeTrackColor = Color(0xFF2E5BFF),
                                inactiveTrackColor = Color(0x338E90A2)
                            ),
                            modifier = Modifier.testTag("pitch_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lower", color = Color(0xFF8E90A2), fontSize = 10.sp)
                            Text("Balanced", color = Color(0xFF8E90A2), fontSize = 10.sp)
                            Text("Higher", color = Color(0xFF8E90A2), fontSize = 10.sp)
                        }
                    }
                }
            }

            // Toggles
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-record sessions",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Automatically capture voice inputs when the assistant is active.",
                            color = Color(0xFF8E90A2),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = autoRecordToggle,
                        onCheckedChange = { autoRecordToggle = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2E5BFF),
                            uncheckedThumbColor = Color(0xFF8E90A2),
                            uncheckedTrackColor = Color(0x1F8E90A2)
                        ),
                        modifier = Modifier.testTag("auto_record_switch")
                    )
                }
            }

            // Voice Profiles Selection
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Voice Profiles",
                        color = Color(0xFFB8C3FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Profile 1: Professional
                        ProfileOptionItem(
                            title = "Professional",
                            description = "Clear & Authoritative",
                            icon = Icons.Default.Work,
                            isSelected = selectedProfile == "Professional",
                            onClick = { selectedProfile = "Professional" },
                            onPreview = {
                                viewModel.userSpeakText("Neural professional co-pilot voice is ready.")
                            }
                        )

                        // Profile 2: Friendly
                        ProfileOptionItem(
                            title = "Friendly",
                            description = "Warm & Engaging",
                            icon = Icons.Default.Mood,
                            isSelected = selectedProfile == "Friendly",
                            onClick = { selectedProfile = "Friendly" },
                            onPreview = {
                                viewModel.userSpeakText("Aetheric friendly co-pilot voice is enabled!")
                            }
                        )

                        // Profile 3: Neutral
                        ProfileOptionItem(
                            title = "Neutral",
                            description = "Unbiased & Steady",
                            icon = Icons.Default.Balance,
                            isSelected = selectedProfile == "Neutral",
                            onClick = { selectedProfile = "Neutral" },
                            onPreview = {
                                viewModel.userSpeakText("Steady standard neutral profile is active.")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color(0x1F2E5BFF)), RoundedCornerShape(8.dp))
                            .background(Color(0x0A2E5BFF))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "\"High-fidelity neural voices are processed locally to ensure your privacy remains the top priority.\"",
                            color = Color(0xBFB8C3FF),
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Glowing Waveform Visualization Panel
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                borderStroke = BorderStroke(1.dp, Color(0x33B8C3FF))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw a wave simulation using simple columns of bouncing heights
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..20) {
                            val phase = i * 0.4f + waveOffset
                            val heightFraction = (sin(phase.toDouble()) * 0.4f + 0.6f).toFloat()
                            val barHeight = (48 * heightFraction).dp

                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(barHeight)
                                    .clip(CircleShape)
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color(0xFF89CEFF), Color(0xFF2E5BFF))
                                        )
                                    )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xCC0B1326), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "VOICE PREVIEW WAVEFORM",
                            color = Color(0xFFB8C3FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Save CTA Button
            Button(
                onClick = {
                    viewModel.updateSettings(
                        speed = speedSliderVal,
                        pitch = pitchSliderVal,
                        profile = selectedProfile,
                        autoRecordVal = autoRecordToggle
                    )
                    viewModel.navigateTo(AppScreen.HISTORY)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_settings_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save settings")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply & Save Settings", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPreview: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x262E5BFF) else Color(0x0AFFFFFF))
            .border(
                BorderStroke(
                    1.dp,
                    if (isSelected) Color(0x66B8C3FF) else Color(0x12FFFFFF)
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0x33B8C3FF) else Color(0x12FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) Color(0xFFB8C3FF) else Color(0xFF8E90A2),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        color = if (isSelected) Color.White else Color(0xFFDAE2FD),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        color = Color(0xFF8E90A2),
                        fontSize = 12.sp
                    )
                }
            }

            // Preview Play button
            IconButton(
                onClick = { onPreview() },
                modifier = Modifier
                    .size(36.dp)
                    .border(BorderStroke(1.dp, if (isSelected) Color(0x66B8C3FF) else Color(0x1F8E90A2)), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Preview voice",
                    tint = if (isSelected) Color(0xFFB8C3FF) else Color(0xFF8E90A2),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
