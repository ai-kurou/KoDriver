package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.Gt7Ps5MyBestLap
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsWarning
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelWarning
import kurou.kodriver.domain.engine.Gt7Ps5TyreOverheat
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.formatGt7Ps5MyBestLapReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.model.formatGt7Ps5TyreTemperatureReadoutText
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/** 自己ベストラップ更新・タイヤ過熱・燃料残量・残り周回数の自由文言をOS標準TTSで読み上げる。空白は読み上げず、TTS利用可否は文言解決時に確認する。 */
internal class Gt7Ps5ReadoutTextSpeaker(
    private val readoutTexts: Gt7Ps5ReadoutTextUseCases,
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    private val speakText: SpeakTextUseCase,
) {
    suspend operator fun invoke(
        event: SpeechEvent,
        volume: Int,
    ) {
        val readout = eventText(event) ?: return
        if (readout.text.isBlank()) return
        // Processorで利用可否を確認した解決済み本文は、そのまま再生する。
        if (!readout.isResolved && !checkTextToSpeechAvailable()) return
        speakText(readout.text, volume = volume)
    }

    /** 判定時の解決済み文言を優先する。空白・TTS利用不可・対象外イベントは null。 */
    suspend fun readoutText(event: SpeechEvent): String? {
        val text = eventText(event)?.text ?: return null
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }

    private data class ReadoutText(
        val text: String,
        val isResolved: Boolean,
    )

    private suspend fun eventText(event: SpeechEvent): ReadoutText? =
        when (event) {
            is DebugReadoutText -> {
                ReadoutText(event.resolvedText, true)
            }

            is Gt7Ps5RemainingFuelWarning -> {
                ReadoutText(
                    remainingFuelText(event),
                    event.resolvedText != null,
                )
            }

            is Gt7Ps5RemainingFuelLapsWarning -> {
                ReadoutText(
                    remainingFuelLapsText(event),
                    event.resolvedText != null,
                )
            }

            is Gt7Ps5MyBestLap -> {
                ReadoutText(
                    myBestLapText(event),
                    event.resolvedText != null,
                )
            }

            is Gt7Ps5TyreOverheat -> {
                ReadoutText(
                    tyreOverheatText(event),
                    event.resolvedText != null,
                )
            }

            else -> {
                null
            }
        }

    private suspend fun remainingFuelText(event: Gt7Ps5RemainingFuelWarning): String =
        event.resolvedText
            ?: formatGt7Ps5RemainingFuelReadoutText(readoutTexts.remainingFuel().first(), event.percent)

    private suspend fun remainingFuelLapsText(event: Gt7Ps5RemainingFuelLapsWarning): String =
        event.resolvedText
            ?: if (event.laps <= 0) {
                readoutTexts.remainingFuelLapsEmpty().first()
            } else {
                formatGt7Ps5RemainingFuelLapsReadoutText(readoutTexts.remainingFuelLaps().first(), event.laps)
            }

    private suspend fun tyreOverheatText(event: Gt7Ps5TyreOverheat): String =
        event.resolvedText
            ?: formatGt7Ps5TyreTemperatureReadoutText(readoutTexts.tyreOverheat().first(), event.celsius)

    private suspend fun myBestLapText(event: Gt7Ps5MyBestLap): String =
        event.resolvedText
            ?: formatGt7Ps5MyBestLapReadoutText(readoutTexts.myBestLap().first(), event.lapTimeMs)
}
