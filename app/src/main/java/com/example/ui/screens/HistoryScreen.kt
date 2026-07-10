package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CallSession
import com.example.ui.components.GlassPanel
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val callSessions by viewModel.callHistory.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showStartCallDialog by remember { mutableStateOf(false) }
    var newCallPartnerName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val contactsPermissionGranted by viewModel.contactsPermissionGranted.collectAsState()
    val deviceContacts by viewModel.deviceContacts.collectAsState()

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateContactsPermission(isGranted)
    }

    // Filter calls based on query
    val filteredSessions = remember(callSessions, searchQuery) {
        if (searchQuery.isBlank()) {
            callSessions
        } else {
            val converters = com.example.data.db.Converters()
            callSessions.filter { session ->
                session.contactName.contains(searchQuery, ignoreCase = true) ||
                        converters.fromString(session.transcriptJson).any { msg ->
                            msg.text.contains(searchQuery, ignoreCase = true)
                        }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1326))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Leave room for bottom bar
        ) {
            // Main Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x2E2E5BFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SpatialTracking,
                            contentDescription = "Logo",
                            tint = Color(0xFFB8C3FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI Assistant",
                        color = Color(0xFFB8C3FF),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFFB8C3FF)
                    )
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search conversations, contacts...", color = Color(0x66DAE2FD)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF8E90A2)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF8E90A2))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x1A171F33),
                        unfocusedContainerColor = Color(0x1A171F33),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Conversations List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Call History",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${filteredSessions.size} CALLS TOTAL",
                    color = Color(0xFFC4C5D9),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sessions List
            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhoneMissed,
                            contentDescription = "Empty History",
                            tint = Color(0x33DAE2FD),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching calls found" else "No silent calls saved yet",
                            color = Color(0x66DAE2FD),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredSessions, key = { it.id }) { session ->
                        HistoryCard(
                            session = session,
                            onDelete = { viewModel.deleteSession(session) },
                            searchQuery = searchQuery
                        )
                    }
                }
            }
        }

        // Floating action button for starting a silent voice call
        FloatingActionButton(
            onClick = { showStartCallDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 24.dp)
                .testTag("start_call_fab"),
            containerColor = Color(0xFF2E5BFF),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.PhoneInTalk,
                contentDescription = "Start Silent Call",
                modifier = Modifier.size(24.dp)
            )
        }

        // Bottom Navigation Bar (Visual Sync)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0x4D131B2E)) // Translucent container
                .border(BorderStroke(1.dp, Color(0x16FFFFFF)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Live Call
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showStartCallDialog = true }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Live call active screen",
                        tint = Color(0xFF8E90A2)
                    )
                    Text("Live", color = Color(0xFF8E90A2), fontSize = 11.sp)
                }

                // Tab 2: History (Active)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color(0x332E5BFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Call Session History",
                        tint = Color(0xFFB8C3FF)
                    )
                    Text("History", color = Color(0xFFB8C3FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Tab 3: Settings
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppScreen.SETTINGS) }
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Voice parameters settings",
                        tint = Color(0xFF8E90A2)
                    )
                    Text("Settings", color = Color(0xFF8E90A2), fontSize = 11.sp)
                }
            }
        }

        // Start Call Overlay Dialog
        if (showStartCallDialog) {
            AlertDialog(
                onDismissRequest = { showStartCallDialog = false },
                title = {
                    Text(
                        "Start Silent Call",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Enter the name of your call partner below. This is secret: no audio will be played out loud, and you'll communicate via text-to-voice.",
                            color = Color(0xFFC4C5D9),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newCallPartnerName,
                            onValueChange = { newCallPartnerName = it },
                            placeholder = { Text("Contact Name (e.g. Jordan Sterling)", color = Color(0x66DAE2FD)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contact_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0x1F171F33),
                                unfocusedContainerColor = Color(0x1F171F33),
                                focusedBorderColor = Color(0xFF2E5BFF),
                                unfocusedBorderColor = Color(0x33B8C3FF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "OR SELECT A CONTACT",
                            color = Color(0xFF8E90A2),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (!contactsPermissionGranted) {
                            Button(
                                onClick = {
                                    contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1F2E5BFF)),
                                border = BorderStroke(1.dp, Color(0x332E5BFF)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("grant_contacts_permission_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFFB8C3FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Access Device Contacts", color = Color(0xFFB8C3FF), fontSize = 14.sp)
                            }
                        } else {
                            // Filter device contacts based on newCallPartnerName
                            val filteredContacts = remember(deviceContacts, newCallPartnerName) {
                                if (newCallPartnerName.isBlank()) {
                                    deviceContacts.take(5)
                                } else {
                                    deviceContacts.filter {
                                        it.name.contains(newCallPartnerName, ignoreCase = true) ||
                                        it.phoneNumber.contains(newCallPartnerName)
                                    }.take(5)
                                }
                            }

                            if (filteredContacts.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .background(Color(0x0F171F33), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No device contacts found.", color = Color(0xFF8E90A2), fontSize = 13.sp)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x0F171F33), RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                ) {
                                    filteredContacts.forEach { contact ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    newCallPartnerName = contact.name
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x332E5BFF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val initials = contact.name.take(1).uppercase()
                                                Text(
                                                    text = initials,
                                                    color = Color(0xFFB8C3FF),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = contact.name,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = contact.phoneNumber,
                                                    color = Color(0xFF8E90A2),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showStartCallDialog = false
                            val name = newCallPartnerName.ifBlank { "Jordan Sterling" }
                            viewModel.startCall(name)
                            newCallPartnerName = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E5BFF)),
                        modifier = Modifier.testTag("confirm_call_button")
                    ) {
                        Text("Connect Session", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartCallDialog = false }) {
                        Text("Cancel", color = Color(0xFF8E90A2))
                    }
                },
                containerColor = Color(0xFF131B2E),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun HistoryCard(session: CallSession, onDelete: () -> Unit, searchQuery: String = "") {
    var expanded by remember { mutableStateOf(false) }

    // Parse Messages
    val messages = remember(session.transcriptJson) {
        com.example.data.db.Converters().fromString(session.transcriptJson)
    }

    // Snippet extraction - prefer showing matching search query context if searching
    val snippet = remember(messages, searchQuery) {
        if (searchQuery.isNotBlank()) {
            val matchingMsg = messages.firstOrNull { it.text.contains(searchQuery, ignoreCase = true) }
            matchingMsg?.text ?: (messages.lastOrNull { it.sender != "You" }?.text
                ?: messages.firstOrNull()?.text
                ?: "No text transcribed.")
        } else {
            messages.lastOrNull { it.sender != "You" }?.text
                ?: messages.firstOrNull()?.text
                ?: "No text transcribed."
        }
    }

    val formattedDate = remember(session.timestamp) {
        val sdf = SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault())
        sdf.format(Date(session.timestamp))
    }

    val formattedDuration = remember(session.durationSeconds) {
        val m = session.durationSeconds / 60
        val s = session.durationSeconds % 60
        String.format(Locale.US, "%02dm %02ds", m, s)
    }

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = EaseInOutSine
                )
            ),
        borderStroke = if (expanded) BorderStroke(1.dp, Color(0x66B8C3FF)) else BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            highlightQuery(session.contactName, searchQuery, Color(0xFF2E5BFF))
                        } else {
                            AnnotatedString(session.contactName)
                        },
                        color = Color(0xFFB8C3FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        color = Color(0xFF8E90A2),
                        fontSize = 12.sp
                    )
                }

                // Duration Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2E2E5BFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Duration",
                            tint = Color(0xFFB8C3FF),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDuration,
                            color = Color(0xFFB8C3FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body Snippet / Scrollable Transcript
            if (!expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color(0x0DFFFFFF)), RoundedCornerShape(8.dp))
                        .background(Color(0x0D171F33))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            highlightQuery(snippet, searchQuery, Color(0xFF2E5BFF))
                        } else {
                            AnnotatedString(snippet)
                        },
                        color = Color(0xFFC4C5D9),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 2
                    )
                }
            } else {
                // Display expanded full conversation log
                Text(
                    text = "CONVERSATION TRANSCRIPT LOG:",
                    color = Color(0x99B8C3FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1F0B1326), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    messages.forEach { msg ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${msg.sender}: ",
                                color = if (msg.sender == "You") Color(0xFFC0C1FF) else Color(0xFF89CEFF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    highlightQuery(msg.text, searchQuery, Color(0xFF2E5BFF))
                                } else {
                                    AnnotatedString(msg.text)
                                },
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Tray
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: Copy
                    OutlinedButton(
                        onClick = { /* Clipboard simulation */ },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC4C5D9)),
                        border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy", fontSize = 12.sp)
                    }

                    // Action 2: Delete
                    Button(
                        onClick = { onDelete() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFB4AB), contentColor = Color(0xFFFFB4AB)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete call log", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun highlightQuery(text: String, query: String, highlightColor: Color = Color(0xFF2E5BFF)): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        var startIdx = 0
        val lowerText = text.lowercase(Locale.getDefault())
        val lowerQuery = query.lowercase(Locale.getDefault())
        while (startIdx < text.length) {
            val idx = lowerText.indexOf(lowerQuery, startIdx)
            if (idx == -1) {
                append(text.substring(startIdx))
                break
            } else {
                append(text.substring(startIdx, idx))
                withStyle(SpanStyle(background = highlightColor.copy(alpha = 0.35f), color = Color.White, fontWeight = FontWeight.Bold)) {
                    append(text.substring(idx, idx + query.length))
                }
                startIdx = idx + query.length
            }
        }
    }
}
