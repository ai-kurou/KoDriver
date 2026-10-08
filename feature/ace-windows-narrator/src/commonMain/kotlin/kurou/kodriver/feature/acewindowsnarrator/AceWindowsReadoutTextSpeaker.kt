package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.formatAceWindowsMyBestLapReadoutText
import kurou.kodriver.domain.model.formatAceWindowsRemainingFuelLapsReadoutText
import kurou.kodriver.domain.model.formatAceWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.model.formatAceWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * ACE の保存した自由文言をOS標準TTSで読み上げる。空白・TTS利用不可なら読み上げない。
 * 対象イベント: Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes・
 * VehicleApproach・TyreOverheat・RemainingFuelWarning・RemainingFuelLapsWarning・MyBestLap。
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
    private val observeRemainingFuelLapsReadoutText: ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase,
    private val observeRemainingFuelLapsEmptyReadoutText: ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase,
    private val observeMyBestLapReadoutText: ObserveAceWindowsMyBestLapReadoutTextUseCase,
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
            } else if (event is SpeechEvent.AceWindowsRemainingFuelLapsWarning) {
                event.resolvedText
                    ?: if (event.laps <= 0) {
                        observeRemainingFuelLapsEmptyReadoutText().first()
                    } else {
                        formatAceWindowsRemainingFuelLapsReadoutText(
                            observeRemainingFuelLapsReadoutText().first(),
                            event.laps,
                        )
                    }
            } else if (event is SpeechEvent.AceWindowsMyBestLap) {
                event.resolvedText
                    ?: formatAceWindowsMyBestLapReadoutText(observeMyBestLapReadoutText().first(), event.lapTimeMs)
            } else {
                (event as? ReadoutTextEvent)?.resolvedText ?: savedReadoutText(event) ?: return null
            }
        return ReadoutText(text, event is ReadoutTextEvent && event.resolvedText != null)
    }

    /** 保存済み固定文言（フラッグ・車両接近）。対象外イベントは null。 */
    private suspend fun savedReadoutText(event: SpeechEvent): String? =
        when (event) {
            is SpeechEvent.AceWindowsCheckeredFlag -> {
                observeCheckeredFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsWhiteFlag -> {
                observeWhiteFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsGreenFlag -> {
                observeGreenFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsRedFlag -> {
                observeRedFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsBlueFlag -> {
                observeBlueFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsYellowFlag -> {
                observeYellowFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsBlackFlag -> {
                observeBlackFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsBlackWhiteFlag -> {
                observeBlackWhiteFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsOrangeCircleFlag -> {
                observeOrangeCircleFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsRedYellowStripesFlag -> {
                observeRedYellowStripesFlagReadoutText().first()
            }

            is SpeechEvent.AceWindowsVehicleApproach -> {
                observeVehicleApproachReadoutText().first()
            }

            else -> {
                null
            }
        }
}

/** 自由文言対象の判定をNarratorと本文再生で共有する。 */
internal fun isAceWindowsCustomSpeakEvent(event: SpeechEvent): Boolean =
    when (event) {
        is SpeechEvent.AceWindowsWhiteFlag,
        is SpeechEvent.AceWindowsGreenFlag,
        is SpeechEvent.AceWindowsRedFlag,
        is SpeechEvent.AceWindowsBlueFlag,
        is SpeechEvent.AceWindowsYellowFlag,
        is SpeechEvent.AceWindowsBlackFlag,
        is SpeechEvent.AceWindowsBlackWhiteFlag,
        is SpeechEvent.AceWindowsOrangeCircleFlag,
        is SpeechEvent.AceWindowsRedYellowStripesFlag,
        is SpeechEvent.AceWindowsCheckeredFlag,
        is SpeechEvent.AceWindowsVehicleApproach,
        -> true

        is SpeechEvent.AceWindowsTyreOverheat,
        is SpeechEvent.AceWindowsRemainingFuelWarning,
        is SpeechEvent.AceWindowsRemainingFuelLapsWarning,
        is SpeechEvent.AceWindowsMyBestLap,
        -> true

        else -> false
    }
