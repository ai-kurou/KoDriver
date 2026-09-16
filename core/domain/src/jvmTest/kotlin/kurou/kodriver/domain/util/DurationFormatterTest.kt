package kurou.kodriver.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DurationFormatterTest {
    @Test
    fun `decomposeDurationMs returns zero components for zero milliseconds`() {
        val actual = decomposeDurationMs(0L)

        assertEquals(DurationComponents(hours = 0, minutes = 0, seconds = 0, millis = 0), actual)
    }

    @Test
    fun `decomposeDurationMs decomposes milliseconds below one second`() {
        val actual = decomposeDurationMs(456L)

        assertEquals(DurationComponents(hours = 0, minutes = 0, seconds = 0, millis = 456), actual)
    }

    @Test
    fun `decomposeDurationMs decomposes milliseconds spanning minutes and seconds`() {
        val actual = decomposeDurationMs(83_456L)

        assertEquals(DurationComponents(hours = 0, minutes = 1, seconds = 23, millis = 456), actual)
    }

    @Test
    fun `decomposeDurationMs decomposes milliseconds at exactly one hour`() {
        val actual = decomposeDurationMs(MILLISECONDS_PER_HOUR)

        assertEquals(DurationComponents(hours = 1, minutes = 0, seconds = 0, millis = 0), actual)
    }

    @Test
    fun `decomposeDurationMs decomposes milliseconds spanning hours, minutes, seconds and millis`() {
        val actual = decomposeDurationMs(5_025_678L)

        assertEquals(DurationComponents(hours = 1, minutes = 23, seconds = 45, millis = 678), actual)
    }

    @Test
    fun `padStartZero pads with leading zeros to the given length`() {
        assertEquals("05", 5L.padStartZero(2))
        assertEquals("005", 5L.padStartZero(3))
    }

    @Test
    fun `padStartZero does not truncate values longer than the given length`() {
        assertEquals("123", 123L.padStartZero(2))
    }
}
