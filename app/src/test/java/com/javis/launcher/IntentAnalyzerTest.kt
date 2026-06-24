package com.javis.launcher

import com.javis.launcher.ai.IntentAnalyzer
import com.javis.launcher.models.IntentType
import org.junit.Assert.*
import org.junit.Test

class IntentAnalyzerTest {
    @Test fun `open app intent detected`() {
        val r = IntentAnalyzer.analyze("open YouTube")
        assertEquals(IntentType.OPEN_APP, r.type)
        assertEquals("youtube", r.params["app"])
    }
    @Test fun `call intent detected`() {
        val r = IntentAnalyzer.analyze("call Mom")
        assertEquals(IntentType.CALL_CONTACT, r.type)
        assertEquals("mom", r.params["contact"])
    }
    @Test fun `alarm intent 7am`() {
        val r = IntentAnalyzer.analyze("set alarm for 7am")
        assertEquals(IntentType.SET_ALARM, r.type)
        assertEquals("7", r.params["hour"])
    }
    @Test fun `alarm intent 7pm`() {
        val r = IntentAnalyzer.analyze("set alarm for 7pm")
        assertEquals(IntentType.SET_ALARM, r.type)
        assertEquals("19", r.params["hour"])
    }
    @Test fun `chat fallback`() {
        val r = IntentAnalyzer.analyze("what is the weather today")
        assertEquals(IntentType.CHAT_GENERAL, r.type)
    }
    @Test fun `notification intent`() {
        val r = IntentAnalyzer.analyze("show my notifications")
        assertEquals(IntentType.GET_NOTIFICATIONS, r.type)
    }
}
