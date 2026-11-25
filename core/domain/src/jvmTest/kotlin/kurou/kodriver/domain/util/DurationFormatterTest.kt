package kurou.kodriver.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

    @Test
    fun `ラップタイムは秒を2桁・ミリ秒を3桁にゼロ埋めして分秒ミリ秒で表示する`() {
        assertEquals("1:23.456", formatLapTimeMs(83_456L))
        assertEquals("1:02.005", formatLapTimeMs(62_005L))
        assertEquals("0:00.000", formatLapTimeMs(0L))
    }

    @Test
    fun `読み上げ用ラップタイムは分が0より大きいとき分秒ミリ秒で表示する`() {
        assertEquals("1分23秒456", formatLapTimeReadoutMs(83_456L))
    }

    @Test
    fun `読み上げ用ラップタイムは分が0のとき秒ミリ秒で表示する`() {
        assertEquals("23秒456", formatLapTimeReadoutMs(23_456L))
        assertEquals("0秒000", formatLapTimeReadoutMs(0L))
    }

    @Test
    fun `読み上げ用ラップタイムの秒はゼロ埋めせずミリ秒は3桁にゼロ埋めする`() {
        assertEquals("1分2秒005", formatLapTimeReadoutMs(62_005L))
        assertEquals("5秒005", formatLapTimeReadoutMs(5_005L))
    }

    @Test
    fun `読み上げ用ラップタイムは59999msで分が0に、60000msで分が1になる境界を正しく表示する`() {
        assertEquals("59秒999", formatLapTimeReadoutMs(59_999L))
        assertEquals("1分0秒000", formatLapTimeReadoutMs(60_000L))
    }

    @Test
    fun `読み上げ用ラップタイムは1時間以上でも分を合計の分として表示する`() {
        assertEquals("60分0秒005", formatLapTimeReadoutMs(3_600_005L))
    }

    @Test
    fun `formatBestLapTimeMsOrNull returns null when the best lap time is null`() {
        assertNull(formatBestLapTimeMsOrNull(null))
    }

    @Test
    fun `formatBestLapTimeMsOrNull returns null when the best lap time is zero`() {
        assertNull(formatBestLapTimeMsOrNull(0L))
    }

    @Test
    fun `formatBestLapTimeMsOrNull returns null when the best lap time is negative`() {
        assertNull(formatBestLapTimeMsOrNull(-1L))
    }

    @Test
    fun `formatBestLapTimeMsOrNull formats a positive best lap time as a lap time string`() {
        assertEquals("1:23.456", formatBestLapTimeMsOrNull(83_456L))
    }
}
