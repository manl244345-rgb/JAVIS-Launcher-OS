package com.javis.launcher

import com.javis.launcher.ai.IntentAnalyzer
import com.javis.launcher.ai.IntentType
import org.junit.Assert.*
import org.junit.Test

class IntentAnalyzerTest {
    @Test fun `open app intent detected`() {
        val r = IntentAnalyzer.analyze("Open WhatsApp")
        assertEquals(IntentType.OPEN_APP, r.type)
    }
    @Test fun `call contact intent`() {
        val r = IntentAnalyzer.analyze("Call Musa")
        assertEquals(IntentType.CALL_CONTACT, r.type)
        assertEquals("musa", r.params["contact"])
    }
    @Test fun `set alarm AM`() {
        val r = IntentAnalyzer.analyze("Set alarm for 7:30 AM")
        assertEquals(IntentType.SET_ALARM, r.type)
        assertEquals("7", r.params["hour"])
        assertEquals("30", r.params["minute"])
    }
    @Test fun `set alarm PM`() {
        val r = IntentAnalyzer.analyze("Set alarm for 6 PM")
        assertEquals(IntentType.SET_ALARM, r.type)
        assertEquals("18", r.params["hour"])
    }
    @Test fun `wake me up`() {
        val r = IntentAnalyzer.analyze("Wake me up at 5 AM")
        assertEquals(IntentType.SET_ALARM, r.type)
    }
    @Test fun `notifications`() {
        val r = IntentAnalyzer.analyze("What are my notifications?")
        assertEquals(IntentType.GET_NOTIFICATIONS, r.type)
    }
    @Test fun `general chat fallback`() {
        val r = IntentAnalyzer.analyze("What is the meaning of life?")
        assertEquals(IntentType.CHAT_GENERAL, r.type)
    }
    @Test fun `time parse 12AM becomes 0`() {
        val (h, _) = IntentAnalyzer.parseTime("12:00 AM")
        assertEquals(0, h)
    }
    @Test fun `time parse 12PM stays 12`() {
        val (h, m) = IntentAnalyzer.parseTime("12:30 PM")
        assertEquals(12, h); assertEquals(30, m)
    }
    @Test fun `delete alarm`() {
        val r = IntentAnalyzer.analyze("Delete alarm for 7 AM")
        assertEquals(IntentType.DELETE_ALARM, r.type)
    }
    @Test fun `memory query`() {
        val r = IntentAnalyzer.analyze("What do you remember about me?")
        assertEquals(IntentType.GET_MEMORY, r.type)
    }
}
