package org.standardnotes.notes

import org.junit.Assert.assertEquals
import org.junit.Test
import org.standardnotes.notes.comms.Crypt
import org.standardnotes.notes.comms.data.AuthParamsResponse

class CryptTest {

    @Test
    fun isParamsSupported_ifSha512_returnsTrue() {
        val params: AuthParamsResponse = AuthParamsResponse()
        params.pwAlg = "sha512"

        assertEquals(true, Crypt.isParamsSupported(params))
    }

    @Test
    fun isParamsSupported_ifNotSha512_returnsFalse() {
        val params: AuthParamsResponse = AuthParamsResponse()
        params.pwAlg = "GO UCLA BRUINS"

        assertEquals(false, Crypt.isParamsSupported(params))
    }
}