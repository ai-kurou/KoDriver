package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.acewindows.model.formatAceWindowsMyBestLapReadoutText
import kurou.kodriver.domain.acewindows.model.formatAceWindowsRemainingFuelLapsReadoutText
import kurou.kodriver.domain.acewindows.model.formatAceWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.acewindows.model.formatAceWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.engine.AceWindowsBlackFlag
import kurou.kodriver.domain.engine.AceWindowsBlackWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsBlueFlag
import kurou.kodriver.domain.engine.AceWindowsCheckeredFlag
import kurou.kodriver.domain.engine.AceWindowsFlagSpeechEvent
import kurou.kodriver.domain.engine.AceWindowsGreenFlag
import kurou.kodriver.domain.engine.AceWindowsManualReadoutText
import kurou.kodriver.domain.engine.AceWindowsMyBestLap
import kurou.kodriver.domain.engine.AceWindowsOrangeCircleFlag
import kurou.kodriver.domain.engine.AceWindowsRedFlag
import kurou.kodriver.domain.engine.AceWindowsRedYellowStripesFlag
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsWarning
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelWarning
import kurou.kodriver.domain.engine.AceWindowsSpeechEvent
import kurou.kodriver.domain.engine.AceWindowsTyreOverheat
import kurou.kodriver.domain.engine.AceWindowsVehicleApproach
import kurou.kodriver.domain.engine.AceWindowsWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsYellowFlag
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * ACE の保存した自由文言をOS標準TTSで読み上げる。空白・TTS利用不可なら読み上げない。
 * 対象イベント: Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes・
 * VehicleApproach・TyreOverheat・RemainingFuelWarning・RemainingFuelLapsWarning・MyBestLap。
 */
internal class AceWindowsReadoutTextSpeaker(
    private val readoutTexts: AceWindowsReadoutTextUseCases,
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

    /** 現在の読み上げ文言。空白・TTS利用不可・対象外イベントは null。 */
    suspend fun readoutText(event: SpeechEvent): String? {
        val readout = eventText(event) ?: return null
        return readout.text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }

    private data class ReadoutText(
        val text: String,
        val isResolved: Boolean,
    )

    private suspend fun eventText(event: SpeechEvent): ReadoutText? =
        when (event) {
            is DebugReadoutText -> ReadoutText(event.resolvedText, true)
            is AceWindowsSpeechEvent -> ReadoutText(aceWindowsText(event), event.resolvedText != null)
            else -> null
        }

    private suspend fun aceWindowsText(event: AceWindowsSpeechEvent): String =
        event.resolvedText
            ?: when (event) {
                is AceWindowsManualReadoutText -> event.text
                is AceWindowsFlagSpeechEvent -> flagText(event)
                is AceWindowsVehicleApproach -> readoutTexts.vehicleApproach().first()
                is AceWindowsTyreOverheat -> tyreOverheatText(event)
                is AceWindowsRemainingFuelWarning -> remainingFuelText(event)
                is AceWindowsRemainingFuelLapsWarning -> remainingFuelLapsText(event)
                is AceWindowsMyBestLap -> myBestLapText(event)
            }

    private suspend fun tyreOverheatText(event: AceWindowsTyreOverheat): String =
        formatAceWindowsTyreTemperatureReadoutText(readoutTexts.tyreOverheat().first(), event.celsius)

    private suspend fun remainingFuelText(event: AceWindowsRemainingFuelWarning): String =
        formatAceWindowsRemainingFuelReadoutText(readoutTexts.remainingFuel.fuel().first(), event.percent)

    private suspend fun myBestLapText(event: AceWindowsMyBestLap): String =
        formatAceWindowsMyBestLapReadoutText(readoutTexts.myBestLap().first(), event.lapTimeMs)

    private suspend fun remainingFuelLapsText(event: AceWindowsRemainingFuelLapsWarning): String =
        if (event.laps <= 0) {
            readoutTexts.remainingFuel.fuelLapsEmpty().first()
        } else {
            formatAceWindowsRemainingFuelLapsReadoutText(
                readoutTexts.remainingFuel.fuelLaps().first(),
                event.laps,
            )
        }

    /** 全10種類のフラッグの保存済み固定文言。 */
    private suspend fun flagText(event: AceWindowsFlagSpeechEvent): String =
        when (event) {
            is AceWindowsCheckeredFlag -> {
                readoutTexts.flag.checkered().first()
            }

            is AceWindowsWhiteFlag -> {
                readoutTexts.flag.white().first()
            }

            is AceWindowsGreenFlag -> {
                readoutTexts.flag.green().first()
            }

            is AceWindowsRedFlag -> {
                readoutTexts.flag.red().first()
            }

            is AceWindowsBlueFlag -> {
                readoutTexts.flag.blue().first()
            }

            is AceWindowsYellowFlag -> {
                readoutTexts.flag.yellow().first()
            }

            is AceWindowsBlackFlag -> {
                readoutTexts.flag.black().first()
            }

            is AceWindowsBlackWhiteFlag -> {
                readoutTexts.flag.blackWhite().first()
            }

            is AceWindowsOrangeCircleFlag -> {
                readoutTexts.flag.orangeCircle().first()
            }

            is AceWindowsRedYellowStripesFlag -> {
                readoutTexts.flag.redYellowStripes().first()
            }
        }
}

/** DebugReadoutText と ACE 由来の全イベントを自由文言対象とし、判定をNarratorと本文再生で共有する。 */
internal fun isAceWindowsCustomSpeakEvent(event: SpeechEvent): Boolean =
    event is DebugReadoutText || event is AceWindowsSpeechEvent
