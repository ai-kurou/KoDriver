package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/** 燃料残り周回数の自由文言をOS標準TTSで読み上げる。空白・TTS利用不可なら読み上げない。 */
internal class Gt7Ps5ReadoutTextSpeaker(
    private val observeRemainingFuelLapsReadoutText: ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase,
    private val observeRemainingFuelLapsEmptyReadoutText: ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase,
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    private val speakText: SpeakTextUseCase,
) {
    suspend operator fun invoke(
        event: SpeechEvent,
        volume: Int,
    ) {
        val text = readoutText(event) ?: return
        speakText(text, volume = volume)
    }

    /** 判定時の解決済み文言を優先する。空白・TTS利用不可・対象外イベントは null。 */
    suspend fun readoutText(event: SpeechEvent): String? {
        if (event !is SpeechEvent.RemainingFuelLapsWarning) return null
        val text =
            event.resolvedText
                ?: if (event.laps <= 0) {
                    observeRemainingFuelLapsEmptyReadoutText().first()
                } else {
                    formatGt7Ps5RemainingFuelLapsReadoutText(observeRemainingFuelLapsReadoutText().first(), event.laps)
                }
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }
}
