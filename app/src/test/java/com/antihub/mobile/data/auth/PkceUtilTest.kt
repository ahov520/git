package com.antihub.mobile.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PkceUtilTest {

    @Test
    fun generateCodeVerifier_hasExpectedLength() {
        val verifier = PkceUtil.generateCodeVerifier()
        assertEquals(64, verifier.length)
    }

    @Test
    fun generateState_notBlank() {
        val state = PkceUtil.generateState()
        assertFalse(state.isBlank())
    }

    @Test
    fun codeChallenge_isStableForSameVerifier() {
        val verifier = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~"
        val challenge1 = PkceUtil.codeChallenge(verifier)
        val challenge2 = PkceUtil.codeChallenge(verifier)

        assertEquals(challenge1, challenge2)
        assertTrue(challenge1.isNotBlank())
    }
}
