package com.tv700.player.ui.login

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tv700.player.R
import com.tv700.player.data.config.ServerConfig
import com.tv700.player.domain.model.Playlist
import com.tv700.player.domain.model.PlaylistType
import com.tv700.player.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: AddPlaylistViewModel = hiltViewModel()
) {
    var selectedType  by remember { mutableStateOf(PlaylistType.XTREAM) }
    var useDefaultDns by remember { mutableStateOf(true) }       // pre-tick taame.live
    var name          by remember { mutableStateOf("700TV") }
    var serverUrl     by remember { mutableStateOf(ServerConfig.DEFAULT_SERVER_URL) }
    var username      by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var m3uUrl        by remember { mutableStateOf("") }

    // When toggling default DNS, swap server URL
    LaunchedEffect(useDefaultDns) {
        serverUrl = if (useDefaultDns) ServerConfig.DEFAULT_SERVER_URL else ""
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()

    Scaffold(
        containerColor = Navy900,
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Navy800, Navy700, Navy800)))
                    .statusBarsPadding()
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Gold500)
                    }
                    Text(
                        stringResource(R.string.add_playlist_title),
                        color = Gold500, fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Type selector ─────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth()
                    .background(Navy700, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PlaylistType.values().forEach { type ->
                    val selected = selectedType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) Brush.horizontalGradient(listOf(Gold600, Gold500))
                                else Brush.horizontalGradient(listOf(Navy700, Navy700)),
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            ) { selectedType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (type == PlaylistType.XTREAM) "Xtream Codes" else "M3U URL",
                            color = if (selected) Navy900 else SharkGray,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ── Default DNS banner ────────────────────────────────────────
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (useDefaultDns) Brush.horizontalGradient(listOf(Navy600, Navy500))
                        else Brush.horizontalGradient(listOf(Navy700, Navy700))
                    )
                    .border(
                        width = 1.dp,
                        color = if (useDefaultDns) Gold500.copy(0.6f) else Navy500,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) { useDefaultDns = !useDefaultDns }
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Dns,
                        contentDescription = null,
                        tint = if (useDefaultDns) Gold500 else SharkGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.default_server),
                            color = if (useDefaultDns) Gold500 else SharkGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            ServerConfig.DEFAULT_SERVER_URL,
                            color = SharkGray.copy(0.7f),
                            fontSize = 11.sp
                        )
                    }
                    Checkbox(
                        checked = useDefaultDns,
                        onCheckedChange = { useDefaultDns = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Gold500,
                            uncheckedColor = SharkGray
                        )
                    )
                }
            }

            // ── Fields ────────────────────────────────────────────────────
            BrandedTextField(value = name, onValueChange = { name = it },
                label = stringResource(R.string.playlist_name))

            if (selectedType == PlaylistType.XTREAM) {
                BrandedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it; useDefaultDns = false },
                    label = stringResource(R.string.server_url),
                    keyboardType = KeyboardType.Uri,
                    enabled = !useDefaultDns,
                    trailingIcon = if (useDefaultDns) Icons.Default.Lock else null
                )
                BrandedTextField(value = username, onValueChange = { username = it },
                    label = stringResource(R.string.username))
                BrandedTextField(value = password, onValueChange = { password = it },
                    label = stringResource(R.string.password), isPassword = true)
            } else {
                BrandedTextField(value = m3uUrl, onValueChange = { m3uUrl = it },
                    label = stringResource(R.string.m3u_url), keyboardType = KeyboardType.Uri)
            }

            error?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Connect button ────────────────────────────────────────────
            Button(
                onClick = {
                    val finalServer = if (useDefaultDns) ServerConfig.DEFAULT_SERVER_URL else serverUrl
                    val playlist = Playlist(
                        id        = UUID.randomUUID().toString(),
                        name      = name.ifBlank { "700TV" },
                        type      = selectedType,
                        serverUrl = finalServer.trimEnd('/'),
                        username  = username,
                        password  = password,
                        m3uUrl    = m3uUrl
                    )
                    viewModel.addPlaylist(playlist) { onAdded() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !isLoading,
                colors   = ButtonDefaults.buttonColors(containerColor = Gold500),
                shape    = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Navy900, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.ConnectWithoutContact, contentDescription = null,
                        tint = Navy900, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.connect), color = Navy900,
                        fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun BrandedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = Gold500,
            unfocusedBorderColor  = Navy500,
            disabledBorderColor   = Navy600,
            focusedLabelColor     = Gold500,
            unfocusedLabelColor   = SharkGray,
            disabledLabelColor    = Navy500,
            cursorColor           = Gold500,
            focusedTextColor      = SharkWhite,
            unfocusedTextColor    = SharkWhite,
            disabledTextColor     = Navy500
        ),
        shape = RoundedCornerShape(10.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        trailingIcon = trailingIcon?.let {
            { Icon(it, contentDescription = null, tint = SharkGray, modifier = Modifier.size(18.dp)) }
        }
    )
}
