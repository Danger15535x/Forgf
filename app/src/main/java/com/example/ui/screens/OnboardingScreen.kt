package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SpatialTracking
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassPanel
import com.example.ui.components.GlowingGradientButton
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val micGranted by viewModel.micPermissionGranted.collectAsState()
    val notifyGranted by viewModel.notifyPermissionGranted.collectAsState()
    val contactsGranted by viewModel.contactsPermissionGranted.collectAsState()

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateContactsPermission(isGranted)
    }

    // Smooth pulsing logo glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1326))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 64.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing background radial leak
            Box(
                modifier = Modifier
                    .size((110 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x2E2E5BFF), Color(0x00000000))
                        )
                    )
            )

            // Outer glass frame
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0x26171F33))
                    .border(BorderStroke(1.dp, Color(0x33B8C3FF)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SpatialTracking,
                    contentDescription = "Tracking Logo",
                    tint = Color(0xFFB8C3FF),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Headings
        Text(
            text = "Your Intelligent\nCo-Pilot for Voice.",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Empowering communication through real-time accessibility assistance and smart transcription.",
            color = Color(0xFFC4C5D9),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Bento Cards for Permissions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Microphone Permission
            PermissionBentoCard(
                icon = Icons.Outlined.Mic,
                title = "Enable Microphone",
                description = "Required for real-time transcription and speech analysis.",
                granted = micGranted,
                onClick = { viewModel.grantMicPermission(!micGranted) },
                iconColor = Color(0xFFB8C3FF),
                testTag = "mic_permission_card"
            )

            // Card 2: Notifications Permission
            PermissionBentoCard(
                icon = Icons.Default.NotificationsActive,
                title = "Allow Notifications",
                description = "Stay updated with summaries and action items while offline.",
                granted = notifyGranted,
                onClick = { viewModel.grantNotifyPermission(!notifyGranted) },
                iconColor = Color(0xFFC0C1FF),
                testTag = "notify_permission_card"
            )

            // Card 3: Contacts Permission
            PermissionBentoCard(
                icon = Icons.Default.Person,
                title = "Access Contacts",
                description = "Instantly connect and start silent calls with your device contacts.",
                granted = contactsGranted,
                onClick = {
                    contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                },
                iconColor = Color(0xFFC4C5D9),
                testTag = "contacts_permission_card"
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Privacy Lock Banner
        GlassPanel(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secure Lock",
                    tint = Color(0x99B8C3FF),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "PRIVACY & SECURITY",
                        color = Color(0xFFB8C3FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your voice and transcripts are end-to-end encrypted. We never share your personal data with third parties. Processing happens locally to ensure maximum confidentiality.",
                        color = Color(0xFFC4C5D9),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // CTAs
        val isReady = micGranted && notifyGranted
        GlowingGradientButton(
            onClick = {
                // If the user has empty history, populate samples for a better default experience!
                if (viewModel.callHistory.value.isEmpty()) {
                    viewModel.loadSampleData()
                }
                viewModel.navigateTo(AppScreen.HISTORY)
            },
            enabled = isReady,
            text = "Get Started",
            modifier = Modifier.testTag("get_started_button")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Maybe later",
            color = Color(0x99C4C5D9),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable {
                    // Let the user skip if they wish
                    viewModel.grantMicPermission(true)
                    viewModel.grantNotifyPermission(true)
                    if (viewModel.callHistory.value.isEmpty()) {
                        viewModel.loadSampleData()
                    }
                    viewModel.navigateTo(AppScreen.HISTORY)
                }
                .padding(8.dp)
        )
    }
}

@Composable
fun PermissionBentoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit,
    iconColor: Color,
    testTag: String
) {
    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Circle Icon Container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1F2E5BFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Body
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color(0xFFC4C5D9),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Checkbox indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (granted) Color(0xFF2E5BFF) else Color.Transparent)
                    .border(
                        BorderStroke(
                            2.dp,
                            if (granted) Color(0xFF2E5BFF) else Color(0xFF8E90A2)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (granted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Granted",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
