package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TranscriptMessage
import com.example.ui.components.GlassPanel
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(viewModel: MainViewModel) {
    val partnerName by viewModel.currentPartnerName.collectAsState()
    val isCallActive by viewModel.isCallActive.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val suggestedReplies by viewModel.suggestedReplies.collectAsState()
    val isGeneratingSuggestions by viewModel.isGeneratingSuggestions.collectAsState()
    val sttStatus by viewModel.sttStatus.collectAsState()
    val timerSeconds by viewModel.callTimerSeconds.collectAsState()

    var userSpeechInput by remember { mutableStateOf("") }
    var simPartnerInput by remember { mutableStateOf("") }
    var showSimPanel by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll to bottom of conversation transcript
    LaunchedEffect(liveTranscript.size) {
        if (liveTranscript.isNotEmpty()) {
            listState.animateScrollToItem(liveTranscript.size - 1)
        }
    }

    // Format call elapsed timer
    val formattedTime = remember(timerSeconds) {
        val h = timerSeconds / 3600
        val m = (timerSeconds % 3600) / 60
        val s = timerSeconds % 60
        String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    // Pulse animation for recording status dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1326))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header: Connection Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x26171F33))
                    .border(BorderStroke(1.dp, Color(0x1AFFFFFF)), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile/Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x33B8C3FF))
                            .border(BorderStroke(1.dp, Color(0x66B8C3FF)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = partnerName.take(1).uppercase(Locale.ROOT),
                            color = Color(0xFFB8C3FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = partnerName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF81C784).copy(alpha = dotAlpha))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE CO-PILOT ACTIVE",
                                color = Color(0xFF81C784),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Digital Timer & End Call Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        color = Color(0xFFB8C3FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    Button(
                        onClick = { viewModel.endCall() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("end_call_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White)
                    }
                }
            }

            // Real-Time Transcript Message List
            if (liveTranscript.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SpatialAudio,
                            contentDescription = "Waiting for audio",
                            tint = Color(0x1AB8C3FF),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Waiting for call audio...",
                            color = Color(0x66DAE2FD),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Or use the 'Simulate Partner' drawer to mock conversations.",
                            color = Color(0x4DDAE2FD),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(liveTranscript) { msg ->
                        val isUser = msg.sender == "You"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                // Sender Label
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 4.dp, start = 8.dp, end = 8.dp)
                                ) {
                                    Text(
                                        text = msg.sender.uppercase(Locale.ROOT),
                                        color = if (isUser) Color(0xFFC0C1FF) else Color(0xFF89CEFF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                // Bubble Card
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                            )
                                        )
                                        .background(
                                            if (isUser) Color(0x3D2E5BFF) else Color(0x1F171F33)
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (isUser) Color(0x66B8C3FF) else Color(0x12FFFFFF)
                                            ),
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Smart Suggestion Tray
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x0DFFFFFF))
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI suggested replies",
                            tint = Color(0xFFB8C3FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI CO-PILOT SMART SUGGESTIONS",
                            color = Color(0xFFB8C3FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (isGeneratingSuggestions) {
                        CircularProgressIndicator(
                            color = Color(0xFFB8C3FF),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Suggestions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedReplies.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x262E5BFF))
                                .border(BorderStroke(1.dp, Color(0x4DB8C3FF)), RoundedCornerShape(12.dp))
                                .clickable {
                                    userSpeechInput = suggestion
                                    // Speak automatically on selection!
                                    viewModel.userSpeakText(suggestion)
                                    userSpeechInput = ""
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Speak suggestion out loud",
                                    tint = Color(0xFFB8C3FF),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = suggestion,
                                    color = Color(0xFFDAE2FD),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Input panel: Type to Speak through AI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131B2E))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulator Toggle
                IconButton(
                    onClick = { showSimPanel = !showSimPanel },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (showSimPanel) Color(0x33B8C3FF) else Color(0x0DFFFFFF))
                        .testTag("sim_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Simulate remote partner speech",
                        tint = if (showSimPanel) Color(0xFFB8C3FF) else Color(0xFF8E90A2)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Input box
                TextField(
                    value = userSpeechInput,
                    onValueChange = { userSpeechInput = it },
                    placeholder = { Text("Type to speak out loud in real time...", color = Color(0x66DAE2FD), fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("type_to_speak_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x1A171F33),
                        unfocusedContainerColor = Color(0x1A171F33),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        viewModel.userSpeakText(userSpeechInput)
                        userSpeechInput = ""
                        keyboardController?.hide()
                    })
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Speak CTA Button
                Button(
                    onClick = {
                        viewModel.userSpeakText(userSpeechInput)
                        userSpeechInput = ""
                        keyboardController?.hide()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("speak_submit_button")
                ) {
                    Text("Speak", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Expanded Sim Panel Drawer
            AnimatedVisibility(
                visible = showSimPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .border(BorderStroke(1.dp, Color(0x1F8E90A2)), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SIMULATE INCOMING CALL PARTNER SPEECH",
                            color = Color(0xFF89CEFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        IconButton(onClick = { showSimPanel = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E90A2), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated Input Quick Phrases
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val phrases = listOf(
                            "Hey, is everything okay?",
                            "Can you confirm the shipment ETA?",
                            "Thanks, let's talk soon!"
                        )
                        phrases.forEach { phrase ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x1A89CEFF))
                                    .border(BorderStroke(1.dp, Color(0x3389CEFF)), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.addPartnerTranscript(phrase) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(phrase, color = Color(0xFF89CEFF), fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Simulated text input
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = simPartnerInput,
                            onValueChange = { simPartnerInput = it },
                            placeholder = { Text("Enter mock phrase for Speaker A...", color = Color(0x4DDAE2FD), fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("sim_partner_speech_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0x1F171F33),
                                unfocusedContainerColor = Color(0x1F171F33),
                                focusedBorderColor = Color(0xFF89CEFF),
                                unfocusedBorderColor = Color(0x1F89CEFF)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                viewModel.addPartnerTranscript(simPartnerInput)
                                simPartnerInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3389CEFF), contentColor = Color(0xFF89CEFF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Transmit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
