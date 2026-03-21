package com.tv700.player.data.parental

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

private val Context.parentalDataStore by preferencesDataStore("parental_prefs")

@Singleton
class ParentalControlManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_PIN_HASH       = stringPreferencesKey("pin_hash")
        private val KEY_PIN_ENABLED    = booleanPreferencesKey("pin_enabled")
        private val KEY_LOCK_ADULT     = booleanPreferencesKey("lock_adult")
        private val KEY_LOCK_ALL_VOD   = booleanPreferencesKey("lock_all_vod")
    }

    val isPinEnabled: Flow<Boolean> = context.parentalDataStore.data.map {
        it[KEY_PIN_ENABLED] ?: false
    }

    val lockAdultContent: Flow<Boolean> = context.parentalDataStore.data.map {
        it[KEY_LOCK_ADULT] ?: false
    }

    val lockAllVod: Flow<Boolean> = context.parentalDataStore.data.map {
        it[KEY_LOCK_ALL_VOD] ?: false
    }

    /** Set a new PIN. Stores SHA-256 hash only — never the raw PIN. */
    suspend fun setPin(pin: String) {
        require(pin.length == 4 && pin.all { it.isDigit() }) { "PIN must be 4 digits" }
        context.parentalDataStore.edit { prefs ->
            prefs[KEY_PIN_HASH]    = sha256(pin)
            prefs[KEY_PIN_ENABLED] = true
        }
    }

    /** Remove PIN protection entirely. */
    suspend fun clearPin() {
        context.parentalDataStore.edit { prefs ->
            prefs[KEY_PIN_HASH]    = ""
            prefs[KEY_PIN_ENABLED] = false
        }
    }

    /** Returns true if the entered PIN matches the stored hash. */
    suspend fun verifyPin(enteredPin: String): Boolean {
        val storedHash = context.parentalDataStore.data.first()[KEY_PIN_HASH] ?: return false
        return sha256(enteredPin) == storedHash
    }

    suspend fun setLockAdult(lock: Boolean) {
        context.parentalDataStore.edit { it[KEY_LOCK_ADULT] = lock }
    }

    suspend fun setLockAllVod(lock: Boolean) {
        context.parentalDataStore.edit { it[KEY_LOCK_ALL_VOD] = lock }
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
