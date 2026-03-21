package com.iptv.player.ui.parental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.player.data.parental.ParentalControlManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentalViewModel @Inject constructor(
    private val parentalManager: ParentalControlManager
) : ViewModel() {

    val isPinEnabled = parentalManager.isPinEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), false
    )

    val lockAdultContent = parentalManager.lockAdultContent.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), false
    )

    suspend fun verifyPin(pin: String): Boolean = parentalManager.verifyPin(pin)

    fun setPin(pin: String) = viewModelScope.launch { parentalManager.setPin(pin) }

    fun clearPin() = viewModelScope.launch { parentalManager.clearPin() }

    fun setLockAdult(lock: Boolean) = viewModelScope.launch { parentalManager.setLockAdult(lock) }

    /**
     * Handle digit entry with inline callback (avoids needing coroutine scope in UI).
     * This is a simplified helper — in production prefer using a state machine.
     */
    fun onDigitEntered(
        digit: String,
        currentPin: String,
        onResult: (newPin: String, isCorrect: Boolean) -> Unit
    ) {
        val newPin = currentPin + digit
        if (newPin.length < 4) {
            onResult(newPin, false)
            return
        }
        viewModelScope.launch {
            val correct = parentalManager.verifyPin(newPin)
            onResult(if (correct) newPin else "", correct)
        }
    }
}
