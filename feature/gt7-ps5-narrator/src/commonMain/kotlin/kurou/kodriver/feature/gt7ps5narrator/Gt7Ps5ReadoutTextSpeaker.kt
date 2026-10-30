package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.Gt7Ps5ManualReadoutText
import kurou.kodriver.domain.engine.Gt7Ps5MyBestLap
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelCurrentValue
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsCurrentValue
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsWarning
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelWarning
import kurou.kodriver.domain.engine.Gt7Ps5SpeechEvent
import kurou.kodriver.domain.engine.Gt7Ps5TyreOverheat
import kurou.kodriver.domain.engine.Gt7Ps5TyreTemperatureCurrentValue
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5MyBestLapReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5TyreTemperatureReadoutText
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

            is Gt7Ps5SpeechEvent -> {
                gt7EventText(event)
            }

            else -> {
                null
            }
        }

    /**
     * GT7イベント型の追加時は、網羅的な when により未対応をコンパイルエラーにする。
     * 解決済み文言があればそれを優先し、設定を読み直さない。
     */
    private suspend fun gt7EventText(event: Gt7Ps5SpeechEvent): ReadoutText {
        val resolved = event.resolvedText
        val text =
            resolved ?: when (event) {
                is Gt7Ps5RemainingFuelLapsCurrentValue -> event.narratedText
                is Gt7Ps5TyreTemperatureCurrentValue -> event.narratedText
                is Gt7Ps5RemainingFuelCurrentValue -> event.narratedText
                is Gt7Ps5ManualReadoutText -> event.text
                is Gt7Ps5RemainingFuelWarning -> remainingFuelText(event.percent)
                is Gt7Ps5RemainingFuelLapsWarning -> remainingFuelLapsText(event.laps)
                is Gt7Ps5MyBestLap -> myBestLapText(event.lapTimeMs)
                is Gt7Ps5TyreOverheat -> tyreOverheatText(event.celsius)
            }
        return ReadoutText(text, resolved != null)
    }

    private suspend fun remainingFuelText(percent: Int): String =
        formatGt7Ps5RemainingFuelReadoutText(readoutTexts.remainingFuel().first(), percent)

    private suspend fun remainingFuelLapsText(laps: Int): String =
        if (laps <= 0) {
            readoutTexts.remainingFuelLapsEmpty().first()
        } else {
            formatGt7Ps5RemainingFuelLapsReadoutText(readoutTexts.remainingFuelLaps().first(), laps)
        }

    private suspend fun myBestLapText(lapTimeMs: Int): String =
        formatGt7Ps5MyBestLapReadoutText(readoutTexts.myBestLap().first(), lapTimeMs)

    private suspend fun tyreOverheatText(celsius: Int): String =
        formatGt7Ps5TyreTemperatureReadoutText(readoutTexts.tyreOverheat().first(), celsius)
}
