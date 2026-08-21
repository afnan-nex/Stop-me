package com.afnan.stopme.core.common.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `toMinuteString converts 0 to 0m`() {
        assertEquals("0m", 0L.toMinuteString())
    }

    @Test
    fun `toMinuteString converts 30 minutes`() {
        assertEquals("30m", (30L * 60 * 1000).toMinuteString())
    }

    @Test
    fun `toMinuteString converts 1 hour 18 minutes`() {
        val millis = (78L * 60 * 1000) // 78 minutes
        assertEquals("1h 18m", millis.toMinuteString())
    }

    @Test
    fun `toCountdownString formats 29 seconds`() {
        assertEquals("00:29", (29_000L).toCountdownString())
    }

    @Test
    fun `toCountdownString formats 0`() {
        assertEquals("00:00", 0L.toCountdownString())
    }

    @Test
    fun `toCountdownString clamps negative to 0`() {
        assertEquals("00:00", (-5000L).toCountdownString())
    }

    @Test
    fun `toRemainingString shows 0m for exhausted`() {
        assertEquals("0m left", 0L.toRemainingString())
    }

    @Test
    fun `DAILY_LIMIT_MILLIS is 30 minutes`() {
        assertEquals(30L * 60 * 1000, DAILY_LIMIT_MILLIS)
    }
}
