package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.formatLmuWindowsPitTimingReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * フラッグ・車両接近・ピットタイミング・バーチャルエナジー残量警告・タイヤ過熱警告の自由文字列をOS標準TTSで読み上げる、
 * [WavNarratorEngine][kurou.kodriver.core.narrator.WavNarratorEngine] 用のフック。
 * 空欄またはTTSが利用できない場合は本文を読み上げない。
 * イベントごとの文言取得とTTSの依存を明示する。
 *
 * 対象イベントと文言の対応:
 * - [SpeechEvent.TyreOverheat] : タイヤ過熱警告
 * - [SpeechEvent.TyreCold] : タイヤ低温警告
 * - [SpeechEvent.YellowFlag] : セクターイエロー
 * - [SpeechEvent.BlueFlag] : ブルー
 * - [SpeechEvent.FullCourseYellow] : フルコースイエロー
 * - [SpeechEvent.RedFlag] : レッド
 * - [SpeechEvent.CarLeft] : 左車両接近開始
 * - [SpeechEvent.CarRight] : 右車両接近開始
 * - [SpeechEvent.CarLeftSustained] : 左車両接近継続
 * - [SpeechEvent.CarRightSustained] : 右車両接近継続
 * - [SpeechEvent.RemainingVirtualEnergyWarning] : 設定した残量閾値の警告
 * - [SpeechEvent.PitTimingWarning] : バーチャルエナジー・タイヤ摩耗由来のピットタイミング
 */
@Suppress("LongParameterList")
internal class LmuWindowsReadoutTextSpeaker(
    private val observeSectorYellowFlagReadoutText: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase,
    private val observeBlueFlagReadoutText: ObserveLmuWindowsBlueFlagReadoutTextUseCase,
    private val observeFullCourseYellowFlagReadoutText: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase,
    private val observeRedFlagReadoutText: ObserveLmuWindowsRedFlagReadoutTextUseCase,
    private val observeStartLeftReadoutText: ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase,
    private val observeStartRightReadoutText: ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase,
    private val observeSustainedLeftReadoutText: ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase,
    private val observeSustainedRightReadoutText: ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase,
    private val observePitTimingVirtualEnergyReadoutText: ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase,
    private val observePitTimingVirtualEnergyImminentReadoutText:
        ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase,
    private val observePitTimingTyreWearReadoutText: ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase,
    private val observePitTimingTyreWearImminentReadoutText:
        ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase,
    private val observeRemainingVirtualEnergyReadoutText: ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase,
    private val observeTyreOverheatReadoutText: ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase,
    private val observeTyreColdReadoutText: ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase,
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    private val speakText: SpeakTextUseCase,
) {
    /**
     * @param volume アプリの読み上げ音量（0〜100）。
     *   [event] が対象外イベント、文言が空白、またはTTSが利用不可の場合は何も読み上げない。
     */
    suspend operator fun invoke(
        event: SpeechEvent,
        volume: Int,
    ) {
        val text = readoutText(event) ?: return
        speakText(text, volume = volume)
    }

    /** 現在の読み上げ文言。空欄・TTS利用不可・対象外イベントは null。 */
    suspend fun readoutText(event: SpeechEvent): String? {
        val text = eventText(event) ?: return null
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }

    private suspend fun eventText(event: SpeechEvent): String? =
        when (event) {
            SpeechEvent.TyreOverheat -> observeTyreOverheatReadoutText().first()
            SpeechEvent.TyreCold -> observeTyreColdReadoutText().first()
            SpeechEvent.YellowFlag -> observeSectorYellowFlagReadoutText().first()
            SpeechEvent.BlueFlag -> observeBlueFlagReadoutText().first()
            SpeechEvent.FullCourseYellow -> observeFullCourseYellowFlagReadoutText().first()
            SpeechEvent.RedFlag -> observeRedFlagReadoutText().first()
            SpeechEvent.CarLeft -> observeStartLeftReadoutText().first()
            SpeechEvent.CarLeftSustained -> observeSustainedLeftReadoutText().first()
            SpeechEvent.CarRight -> observeStartRightReadoutText().first()
            SpeechEvent.CarRightSustained -> observeSustainedRightReadoutText().first()
            is SpeechEvent.RemainingVirtualEnergyWarning -> remainingVirtualEnergyText(event)
            is SpeechEvent.PitTimingWarning -> pitTimingText(event)
            else -> null
        }

    private suspend fun remainingVirtualEnergyText(event: SpeechEvent.RemainingVirtualEnergyWarning): String =
        event.resolvedText
            ?: formatLmuWindowsRemainingVirtualEnergyReadoutText(
                observeRemainingVirtualEnergyReadoutText().first(),
                event.percentage,
            )

    private suspend fun pitTimingText(event: SpeechEvent.PitTimingWarning): String? {
        if (event.source == PitTimingSource.TyreWear) {
            return if (event.laps <= 0) {
                observePitTimingTyreWearImminentReadoutText().first()
            } else {
                formatLmuWindowsPitTimingReadoutText(observePitTimingTyreWearReadoutText().first(), event.laps)
            }
        }
        return if (event.laps <= 0) {
            observePitTimingVirtualEnergyImminentReadoutText().first()
        } else {
            formatLmuWindowsPitTimingReadoutText(
                observePitTimingVirtualEnergyReadoutText().first(),
                event.laps,
            )
        }
    }
}
