package com.iptv.player.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptv.player.domain.model.Playlist
import com.iptv.player.domain.model.PlaylistType
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: AddPlaylistViewModel = hiltViewModel()
) {
    var selectedType by remember { mutableStateOf(PlaylistType.XTREAM) }
    var name       by remember { mutableStateOf("") }
    var serverUrl  by remember { mutableStateOf("") }
    var username   by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var m3uUrl     by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaylistType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(if (type == PlaylistType.XTREAM) "Xtream Codes" else "M3U URL") }
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (selectedType == PlaylistType.XTREAM) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL (e.g. http://server:8080)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            } else {
                OutlinedTextField(
                    value = m3uUrl,
                    onValueChange = { m3uUrl = it },
                    label = { Text("M3U URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val playlist = Playlist(
                        id = UUID.randomUUID().toString(),
                        name = name.ifBlank { "My Playlist" },
                        type = selectedType,
                        serverUrl = serverUrl.trimEnd('/'),
                        username = username,
                        password = password,
                        m3uUrl = m3uUrl
                    )
                    viewModel.addPlaylist(playlist) { onAdded() }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Connect")
            }
        }
    }
}
