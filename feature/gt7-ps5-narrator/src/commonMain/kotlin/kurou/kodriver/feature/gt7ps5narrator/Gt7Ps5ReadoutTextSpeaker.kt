package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5TyreTemperatureReadoutText
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/** タイヤ過熱・燃料残量・残り周回数の自由文言をOS標準TTSで読み上げる。空白・TTS利用不可なら読み上げない。 */
internal class Gt7Ps5ReadoutTextSpeaker(
    private val observeRemainingFuelLapsReadoutText: ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase,
    private val observeRemainingFuelLapsEmptyReadoutText: ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase,
    private val observeRemainingFuelReadoutText: ObserveGt7Ps5RemainingFuelReadoutTextUseCase,
    private val observeTyreOverheatReadoutText: ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase,
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
        val text =
            when (event) {
                is SpeechEvent.Gt7Ps5RemainingFuelWarning -> remainingFuelText(event)
                is SpeechEvent.RemainingFuelLapsWarning -> remainingFuelLapsText(event)
                is SpeechEvent.Gt7Ps5TyreOverheat -> tyreOverheatText(event)
                else -> return null
            }
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }

    private suspend fun remainingFuelText(event: SpeechEvent.Gt7Ps5RemainingFuelWarning): String =
        event.resolvedText
            ?: formatGt7Ps5RemainingFuelReadoutText(observeRemainingFuelReadoutText().first(), event.percent)

    private suspend fun remainingFuelLapsText(event: SpeechEvent.RemainingFuelLapsWarning): String =
        event.resolvedText
            ?: if (event.laps <= 0) {
                observeRemainingFuelLapsEmptyReadoutText().first()
            } else {
                formatGt7Ps5RemainingFuelLapsReadoutText(observeRemainingFuelLapsReadoutText().first(), event.laps)
            }

    private suspend fun tyreOverheatText(event: SpeechEvent.Gt7Ps5TyreOverheat): String =
        event.resolvedText
            ?: formatGt7Ps5TyreTemperatureReadoutText(observeTyreOverheatReadoutText().first(), event.celsius)
}
