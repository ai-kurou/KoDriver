package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.engine.SpeechEvent

/**
 * ピットタイミング警告を同一ラップ内で「予想残り周回数が最も低いもの」だけ読み上げるためのゲート。
 *
 * バーチャルエナジー・タイヤ摩耗の警告が別tickで先着しても、後着の警告の方が緊急（残り周回数が小さい）なら通し、
 * 同等以上なら二重読み上げを防ぐために捨てる。ラップが変わればリセットされる。
 */
internal class PitTimingLapGate {
    private var lastAnnouncedLap: Int = -1
    private var lastAnnouncedLaps: Int = Int.MAX_VALUE

    fun filter(
        currentLap: Int,
        events: List<SpeechEvent>,
    ): List<SpeechEvent> {
        val event = events.filterIsInstance<SpeechEvent.PitTimingWarning>().firstOrNull() ?: return emptyList()
        if (currentLap == lastAnnouncedLap && event.laps >= lastAnnouncedLaps) return emptyList()
        lastAnnouncedLap = currentLap
        lastAnnouncedLaps = event.laps
        return listOf(event)
    }
}
