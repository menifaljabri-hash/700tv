package com.tv700.player.ui.settings

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tv700.player.R
import com.tv700.player.data.config.ServerConfig
import com.tv700.player.data.locale.AppLanguage
import com.tv700.player.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentLang by viewModel.language.collectAsState()
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    var showPinSetup by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Navy900,
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Navy800, Navy700, Navy800)))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Gold500)
                    }
                    Text(stringResource(R.string.settings_title),
                        color = Gold500, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── Language ─────────────────────────────────────────────────
            item {
                SectionHeader(icon = Icons.Default.Language, title = stringResource(R.string.language))
            }
            item {
                LanguageSelector(
                    current = currentLang,
                    onSelect = { lang ->
                        viewModel.setLanguage(lang)
                        // Recreate activity to apply locale change
                        (context as? Activity)?.recreate()
                    }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            // ── Server / DNS ──────────────────────────────────────────────
            item {
                SectionHeader(icon = Icons.Default.Dns, title = stringResource(R.string.server_info))
            }
            item {
                ServerInfoCard()
            }

            item { Spacer(Modifier.height(8.dp)) }

            // ── Parental Controls ─────────────────────────────────────────
            item {
                SectionHeader(icon = Icons.Default.Lock, title = stringResource(R.string.parental_controls))
            }
            item {
                SettingsToggle(
                    icon    = if (isPinEnabled) Icons.Default.LockOpen else Icons.Default.Lock,
                    title   = stringResource(R.string.pin_enabled),
                    checked = isPinEnabled,
                    onToggle = {
                        if (isPinEnabled) viewModel.clearPin()
                        else showPinSetup = true
                    }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            // ── About ─────────────────────────────────────────────────────
            item {
                SectionHeader(icon = Icons.Default.Info, title = stringResource(R.string.about))
            }
            item {
                InfoCard(
                    label = stringResource(R.string.version),
                    value = "700TV v${ServerConfig.APP_VERSION}"
                )
            }
        }
    }

    // PIN setup dialog
    if (showPinSetup) {
        PinSetupDialog(
            onSet = { pin ->
                viewModel.setPin(pin)
                showPinSetup = false
            },
            onDismiss = { showPinSetup = false }
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Gold500, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, color = Gold500, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
        Spacer(Modifier.weight(1f))
        HorizontalDivider(Modifier.weight(1f), color = Navy600, thickness = 1.dp)
    }
}

@Composable
private fun LanguageSelector(current: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AppLanguage.values().forEach { lang ->
            val selected = current == lang
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(Gold600, Gold500))
                        else Brush.horizontalGradient(listOf(Navy700, Navy700))
                    )
                    .border(
                        1.dp,
                        if (selected) Color.Transparent else Navy500,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { onSelect(lang) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (lang) {
                            AppLanguage.ENGLISH -> "🇬🇧"
                            AppLanguage.ARABIC  -> "🇸🇦"
                        },
                        fontSize = 24.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = lang.nativeName,
                        color = if (selected) Navy900 else SharkWhite,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerInfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy700),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ServerRow(label = "DNS / Host", value = ServerConfig.DEFAULT_SERVER_HOST)
            ServerRow(label = "Port", value = ServerConfig.DEFAULT_SERVER_PORT.toString())
            ServerRow(label = "Protocol", value = "HTTP")
            ServerRow(label = "Full URL", value = ServerConfig.DEFAULT_SERVER_URL)
        }
    }
}

@Composable
private fun ServerRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = SharkGray, fontSize = 13.sp)
        Text(value, color = SharkWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy700),
        shape  = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Gold500, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(title, color = SharkWhite, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor  = Navy900,
                    checkedTrackColor  = Gold500,
                    uncheckedThumbColor = SharkGray,
                    uncheckedTrackColor = Navy600
                )
            )
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy700),
        shape  = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = SharkGray, fontSize = 13.sp)
            Text(value, color = Gold500, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PinSetupDialog(onSet: (String) -> Unit, onDismiss: () -> Unit) {
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Navy700,
        title = { Text(stringResource(R.string.set_pin), color = Gold500, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pin1, onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pin1 = it },
                    label = { Text("New PIN (4 digits)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold500, unfocusedBorderColor = Navy500, focusedLabelColor = Gold500),
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = pin2, onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pin2 = it },
                    label = { Text("Confirm PIN") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold500, unfocusedBorderColor = Navy500, focusedLabelColor = Gold500),
                    visualTransformation = PasswordVisualTransformation()
                )
                if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    pin1.length != 4 -> error = "PIN must be 4 digits"
                    pin1 != pin2     -> error = "PINs do not match"
                    else             -> onSet(pin1)
                }
            }) {
                Text(stringResource(R.string.confirm), color = Gold500, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = SharkGray)
            }
        }
    )
}
