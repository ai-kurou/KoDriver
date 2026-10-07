package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.AceWindowsReadoutTextEvent
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.formatAceWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.model.formatAceWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * ACE の保存した自由文言をOS標準TTSで読み上げる。空白・TTS利用不可なら読み上げない。
 * 対象イベント: Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes・
 * VehicleApproach・TyreOverheat・RemainingFuelWarning。
 */
@Suppress("LongParameterList")
internal class AceWindowsReadoutTextSpeaker(
    private val observeCheckeredFlagReadoutText: ObserveAceWindowsCheckeredFlagReadoutTextUseCase,
    private val observeWhiteFlagReadoutText: ObserveAceWindowsWhiteFlagReadoutTextUseCase,
    private val observeGreenFlagReadoutText: ObserveAceWindowsGreenFlagReadoutTextUseCase,
    private val observeRedFlagReadoutText: ObserveAceWindowsRedFlagReadoutTextUseCase,
    private val observeBlueFlagReadoutText: ObserveAceWindowsBlueFlagReadoutTextUseCase,
    private val observeYellowFlagReadoutText: ObserveAceWindowsYellowFlagReadoutTextUseCase,
    private val observeBlackFlagReadoutText: ObserveAceWindowsBlackFlagReadoutTextUseCase,
    private val observeBlackWhiteFlagReadoutText: ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase,
    private val observeOrangeCircleFlagReadoutText: ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase,
    private val observeRedYellowStripesFlagReadoutText: ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase,
    private val observeVehicleApproachReadoutText: ObserveAceWindowsVehicleApproachReadoutTextUseCase,
    private val observeTyreOverheatReadoutText: ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase,
    private val observeRemainingFuelReadoutText: ObserveAceWindowsRemainingFuelReadoutTextUseCase,
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
        val text = eventText(event)?.text ?: return null
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }

    private data class ReadoutText(
        val text: String,
        val isResolved: Boolean,
    )

    private suspend fun eventText(event: SpeechEvent): ReadoutText? {
        val text =
            if (event is SpeechEvent.AceWindowsTyreOverheat) {
                event.resolvedText
                    ?: formatAceWindowsTyreTemperatureReadoutText(
                        observeTyreOverheatReadoutText().first(),
                        event.celsius,
                    )
            } else if (event is SpeechEvent.AceWindowsRemainingFuelWarning) {
                event.resolvedText
                    ?: formatAceWindowsRemainingFuelReadoutText(
                        observeRemainingFuelReadoutText().first(),
                        event.percent,
                    )
            } else {
                savedReadoutText(event) ?: return null
            }
        return ReadoutText(text, event is AceWindowsReadoutTextEvent && event.resolvedText != null)
    }

    /** 判定時の解決を伴わない、保存済み固定文言（フラッグ・車両接近）。対象外イベントは null。 */
    private suspend fun savedReadoutText(event: SpeechEvent): String? =
        when (event) {
            SpeechEvent.AceWindowsCheckeredFlag -> observeCheckeredFlagReadoutText().first()
            SpeechEvent.AceWindowsWhiteFlag -> observeWhiteFlagReadoutText().first()
            SpeechEvent.AceWindowsGreenFlag -> observeGreenFlagReadoutText().first()
            SpeechEvent.AceWindowsRedFlag -> observeRedFlagReadoutText().first()
            SpeechEvent.AceWindowsBlueFlag -> observeBlueFlagReadoutText().first()
            SpeechEvent.AceWindowsYellowFlag -> observeYellowFlagReadoutText().first()
            SpeechEvent.AceWindowsBlackFlag -> observeBlackFlagReadoutText().first()
            SpeechEvent.AceWindowsBlackWhiteFlag -> observeBlackWhiteFlagReadoutText().first()
            SpeechEvent.AceWindowsOrangeCircleFlag -> observeOrangeCircleFlagReadoutText().first()
            SpeechEvent.AceWindowsRedYellowStripesFlag -> observeRedYellowStripesFlagReadoutText().first()
            SpeechEvent.AceWindowsVehicleApproach -> observeVehicleApproachReadoutText().first()
            else -> null
        }
}

/** 自由文言対象の判定をNarratorと本文再生で共有する。 */
internal fun isAceWindowsCustomSpeakEvent(event: SpeechEvent): Boolean =
    when (event) {
        SpeechEvent.AceWindowsWhiteFlag,
        SpeechEvent.AceWindowsGreenFlag,
        SpeechEvent.AceWindowsRedFlag,
        SpeechEvent.AceWindowsBlueFlag,
        SpeechEvent.AceWindowsYellowFlag,
        SpeechEvent.AceWindowsBlackFlag,
        SpeechEvent.AceWindowsBlackWhiteFlag,
        SpeechEvent.AceWindowsOrangeCircleFlag,
        SpeechEvent.AceWindowsRedYellowStripesFlag,
        SpeechEvent.AceWindowsCheckeredFlag,
        SpeechEvent.AceWindowsVehicleApproach,
        -> true

        is SpeechEvent.AceWindowsTyreOverheat,
        is SpeechEvent.AceWindowsRemainingFuelWarning,
        -> true

        else -> false
    }
