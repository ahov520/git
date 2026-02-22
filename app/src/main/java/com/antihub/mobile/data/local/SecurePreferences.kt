package com.antihub.mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun saveAuthToken(
        accessToken: String,
        refreshToken: String?,
        expiresAtEpochSeconds: Long?,
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT_EPOCH_SECONDS, expiresAtEpochSeconds ?: 0L)
            .apply()
    }

    fun clearAuthToken() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_AT_EPOCH_SECONDS)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getExpiresAtEpochSeconds(): Long? {
        val value = prefs.getLong(KEY_EXPIRES_AT_EPOCH_SECONDS, 0L)
        return value.takeIf { it > 0L }
    }

    fun savePendingPkce(
        state: String,
        codeVerifier: String,
    ) {
        prefs.edit()
            .putString(KEY_OAUTH_STATE, state)
            .putString(KEY_OAUTH_CODE_VERIFIER, codeVerifier)
            .apply()
    }

    fun clearPendingPkce() {
        prefs.edit()
            .remove(KEY_OAUTH_STATE)
            .remove(KEY_OAUTH_CODE_VERIFIER)
            .apply()
    }

    fun getPendingOauthState(): String? = prefs.getString(KEY_OAUTH_STATE, null)

    fun getPendingCodeVerifier(): String? = prefs.getString(KEY_OAUTH_CODE_VERIFIER, null)

    companion object {
        private const val FILE_NAME = "secure_prefs"
        private const val KEY_ACCESS_TOKEN = "key_access_token"
        private const val KEY_REFRESH_TOKEN = "key_refresh_token"
        private const val KEY_EXPIRES_AT_EPOCH_SECONDS = "key_expires_at_epoch_seconds"
        private const val KEY_OAUTH_STATE = "key_oauth_state"
        private const val KEY_OAUTH_CODE_VERIFIER = "key_oauth_code_verifier"
    }
}
