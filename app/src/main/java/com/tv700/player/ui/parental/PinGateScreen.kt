package com.tv700.player.ui.parental

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

/**
 * Full-screen PIN gate. Shows a 4-digit keypad.
 * Calls [onUnlocked] when the correct PIN is entered.
 */
@Composable
fun PinGateScreen(
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: ParentalViewModel = hiltViewModel()
) {
    var pin by remember { mutableStateOf("") }
    var errorShake by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val haptics = LocalHapticFeedback.current

    suspend fun handlePinEntry(digit: String) {
        if (pin.length >= 4) return
        pin += digit
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)

        if (pin.length == 4) {
            val correct = viewModel.verifyPin(pin)
            if (correct) {
                onUnlocked()
            } else {
                errorMessage = "Incorrect PIN"
                errorShake = true
                delay(600)
                pin = ""
                errorShake = false
                delay(1_500)
                errorMessage = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Enter PIN",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // PIN dots
            PinDots(
                length = pin.length,
                isError = errorShake
            )

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Spacer(Modifier.height(8.dp))

            // Number pad
            NumberPad(
                onDigit = { digit ->
                    // Use LaunchedEffect workaround via ViewModel coroutine
                    viewModel.onDigitEntered(digit, pin) { newPin, isCorrect ->
                        if (isCorrect) onUnlocked()
                        else pin = newPin
                    }
                },
                onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
            )

            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun PinDots(length: Int, isError: Boolean) {
    val dotColor by animateColorAsState(
        targetValue = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300), label = "dotColor"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (index < length) dotColor else Color.Transparent)
                    .border(2.dp, dotColor, CircleShape)
            )
        }
    }
}

@Composable
private fun NumberPad(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "del")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { key ->
                    when (key) {
                        "" -> Spacer(Modifier.size(72.dp))
                        "del" -> KeypadButton(onClick = onDelete) {
                            Icon(Icons.Default.Backspace, contentDescription = "Delete")
                        }
                        else -> KeypadButton(onClick = { onDigit(key) }) {
                            Text(key, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
