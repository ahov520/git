package com.antihub.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.featureFlagsDataStore by preferencesDataStore(name = "feature_flags")

@Singleton
class FeatureFlagStore @Inject constructor(
    private val context: Context,
) {
    val flags: Flow<FeatureFlags> = context.featureFlagsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { pref ->
            FeatureFlags(
                webviewFallbackEnabled = pref[KEY_WEBVIEW_FALLBACK] ?: true,
                autoTranslateEnabled = pref[KEY_AUTO_TRANSLATE] ?: false,
            )
        }

    suspend fun setAutoTranslateEnabled(enabled: Boolean) {
        context.featureFlagsDataStore.edit { pref ->
            pref[KEY_AUTO_TRANSLATE] = enabled
        }
    }

    suspend fun setWebviewFallbackEnabled(enabled: Boolean) {
        context.featureFlagsDataStore.edit { pref ->
            pref[KEY_WEBVIEW_FALLBACK] = enabled
        }
    }

    companion object {
        val KEY_AUTO_TRANSLATE = booleanPreferencesKey("auto_translate_enabled")
        val KEY_WEBVIEW_FALLBACK = booleanPreferencesKey("webview_fallback_enabled")
    }
}

data class FeatureFlags(
    val webviewFallbackEnabled: Boolean,
    val autoTranslateEnabled: Boolean,
)
