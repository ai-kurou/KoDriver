package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * ACE の保存した自由文言をOS標準TTSで読み上げる。空白・TTS利用不可なら読み上げない。
 * 対象イベント: Checkered・White・Green・Red・Blue・Yellow・Black・BlackWhite・OrangeCircle・RedYellowStripes・VehicleApproach。
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

    /** 現在の読み上げ文言。空白・TTS利用不可・対象外イベントは null。 */
    suspend fun readoutText(event: SpeechEvent): String? {
        val text =
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
                else -> return null
            }
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
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

        else -> false
    }
