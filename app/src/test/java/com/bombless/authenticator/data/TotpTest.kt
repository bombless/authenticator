package com.bombless.authenticator.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TotpTest {
    @Test
    fun matchesRfc6238Sha1ExampleAt59Seconds() {
        val entry = OtpEntry(
            issuer = "Example",
            account = "alice",
            secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
            algorithm = "SHA1",
            digits = 6,
            period = 30
        )
        assertEquals("287082", Totp.code(entry, 59_000L))
    }

    @Test
    fun formatsEightDigitCodes() {
        val entry = OtpEntry(
            issuer = "Example",
            account = "alice",
            secret = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ",
            digits = 8,
            period = 30
        )
        assertEquals(8, Totp.code(entry, 59_000L).length)
    }
}
