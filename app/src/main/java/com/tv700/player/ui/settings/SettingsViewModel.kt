package com.tv700.player.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tv700.player.data.locale.AppLanguage
import com.tv700.player.data.locale.LanguageManager
import com.tv700.player.data.parental.ParentalControlManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val parentalManager: ParentalControlManager
) : ViewModel() {

    val language = languageManager.currentLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.ARABIC)

    val isPinEnabled = parentalManager.isPinEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setLanguage(lang: AppLanguage) = viewModelScope.launch {
        languageManager.setLanguage(lang)
        languageManager.persistLocaleSync(
            // We pass context via hilt qualifier in real app; handled in Application class
            throw UnsupportedOperationException("Use languageManager.persistLocaleSync(context, lang) from Activity")
        )
    }

    fun setPin(pin: String) = viewModelScope.launch {
        parentalManager.setPin(pin)
    }

    fun clearPin() = viewModelScope.launch {
        parentalManager.clearPin()
    }
}
