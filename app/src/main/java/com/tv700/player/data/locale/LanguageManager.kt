package com.tv700.player.data.locale

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.localeDataStore by preferencesDataStore("locale_prefs")

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    ARABIC("ar", "Arabic", "العربية")
}

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
    }

    val currentLanguage: Flow<AppLanguage> = context.localeDataStore.data.map { prefs ->
        val code = prefs[KEY_LANGUAGE] ?: AppLanguage.ARABIC.code
        AppLanguage.values().firstOrNull { it.code == code } ?: AppLanguage.ARABIC
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.localeDataStore.edit { it[KEY_LANGUAGE] = language.code }
    }

    suspend fun getCurrentLanguage(): AppLanguage {
        return currentLanguage.first()
    }

    /**
     * Apply locale to a Context. Call this in Activity.attachBaseContext.
     */
    fun applyLocale(base: Context): Context {
        // Read synchronously from shared prefs as fallback
        val sharedPrefs = base.getSharedPreferences("locale_sync", Context.MODE_PRIVATE)
        val langCode = sharedPrefs.getString("lang", AppLanguage.ARABIC.code) ?: AppLanguage.ARABIC.code
        return wrapContext(base, langCode)
    }

    fun persistLocaleSync(context: Context, language: AppLanguage) {
        context.getSharedPreferences("locale_sync", Context.MODE_PRIVATE)
            .edit()
            .putString("lang", language.code)
            .apply()
    }

    private fun wrapContext(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return context.createConfigurationContext(config)
    }
}
