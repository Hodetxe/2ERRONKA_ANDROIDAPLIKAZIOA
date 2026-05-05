package com.example.androidapp.presentation.txata

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMessageParserTest {
    @Test
    fun `testu mezua parseatzen du`() {
        val parsed = ChatViewModel.parseRawMessage("Jon: Kaixo", currentUser = "Jon")

        assertEquals("Jon", parsed.senderName)
        assertEquals("Kaixo", parsed.contentText)
        assertNull(parsed.imageBase64)
        assertTrue(parsed.isMe)
    }

    @Test
    fun `irudi mezua parseatzen du`() {
        val parsed = ChatViewModel.parseRawMessage(
            "Ane: ${ChatViewModel.IMAGE_PREFIX}YWJj",
            currentUser = "Jon"
        )

        assertEquals("Ane", parsed.senderName)
        assertNull(parsed.contentText)
        assertEquals("YWJj", parsed.imageBase64)
        assertFalse(parsed.isMe)
    }

    @Test
    fun `formatu zaharreko mezua onartzen du`() {
        val parsed = ChatViewModel.parseRawMessage("txat-ean sartu da!", currentUser = "Jon")

        assertEquals("", parsed.senderName)
        assertEquals("txat-ean sartu da!", parsed.contentText)
        assertNull(parsed.imageBase64)
        assertFalse(parsed.isMe)
    }
}
