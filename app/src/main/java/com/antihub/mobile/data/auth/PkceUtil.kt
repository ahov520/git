package com.antihub.mobile.data.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PkceUtil {
    private const val CODE_VERIFIER_LENGTH = 64
    private const val STATE_LENGTH = 24
    private val secureRandom = SecureRandom()
    private val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"

    fun generateCodeVerifier(): String = randomString(CODE_VERIFIER_LENGTH)

    fun generateState(): String = randomString(STATE_LENGTH)

    fun codeChallenge(codeVerifier: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun randomString(length: Int): String {
        return buildString(length) {
            repeat(length) {
                val index = secureRandom.nextInt(allowedChars.length)
                append(allowedChars[index])
            }
        }
    }
}
