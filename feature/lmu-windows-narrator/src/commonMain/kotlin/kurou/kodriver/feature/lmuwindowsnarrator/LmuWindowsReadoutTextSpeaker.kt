package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.LmuWindowsBlueFlag
import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearLow
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.LmuWindowsCarLeftSustained
import kurou.kodriver.domain.engine.LmuWindowsCarRight
import kurou.kodriver.domain.engine.LmuWindowsCarRightSustained
import kurou.kodriver.domain.engine.LmuWindowsFullCourseYellow
import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.engine.LmuWindowsOverheating
import kurou.kodriver.domain.engine.LmuWindowsPartDetached
import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyWarning
import kurou.kodriver.domain.engine.LmuWindowsTyreCold
import kurou.kodriver.domain.engine.LmuWindowsTyreDetached
import kurou.kodriver.domain.engine.LmuWindowsTyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.engine.LmuWindowsYellowFlag
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.model.PitTimingSource
import kurou.kodriver.domain.model.formatLmuWindowsBrakeTemperatureReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsBrakeWearReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsMyBestLapReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsPitTimingReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.model.formatLmuWindowsTyreWearReadoutText
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * フラッグ・車両接近・ピットタイミング・バーチャルエナジー残量警告・タイヤ摩耗警告・ブレーキ過熱警告・タイヤ温度警告・車両故障警告・自己ベストラップ更新の自由文字列をOS標準TTSで読み上げる、
 * [NarratorEngine][kurou.kodriver.core.narrator.NarratorEngine] 用のフック。
 * 空欄またはTTSが利用できない場合は本文を読み上げない。
 * イベントごとの文言取得とTTSの依存を明示する。
 *
 * 対象イベントと文言の対応:
 * - [LmuWindowsMyBestLap] : 自己ベストラップ更新（`{laptime}` は更新後のタイム）
 * - [LmuWindowsTyreOverheat] : タイヤ過熱警告（`{celsius}` は判定時の全輪の最高カーカス温度）
 * - [LmuWindowsTyreCold] : タイヤ低温警告（`{celsius}` は判定時の全輪の最高カーカス温度）
 * - [LmuWindowsYellowFlag] : セクターイエロー
 * - [LmuWindowsBlueFlag] : ブルー
 * - [LmuWindowsFullCourseYellow] : フルコースイエロー
 * - [LmuWindowsRedFlag] : レッド
 * - [LmuWindowsCarLeft] : 左車両接近開始
 * - [LmuWindowsCarRight] : 右車両接近開始
 * - [LmuWindowsCarLeftSustained] : 左車両接近継続
 * - [LmuWindowsCarRightSustained] : 右車両接近継続
 * - [LmuWindowsRemainingVirtualEnergyWarning] : 設定した残量閾値の警告
 * - [LmuWindowsOverheating] : オーバーヒート
 * - [LmuWindowsPartDetached] : 部品脱落
 * - [LmuWindowsTyreDetached] : タイヤ脱落
 * - [LmuWindowsBrakeOverheat] : 設定した温度閾値の警告
 * - [LmuWindowsBrakeWearLow] : 設定したブレーキ残量閾値の警告
 * - [LmuWindowsTyreWearWarning] : 設定した残存率閾値の警告
 * - [LmuWindowsPitTimingWarning] : バーチャルエナジー・タイヤ摩耗由来のピットタイミング
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
    private val observeBrakeTemperatureReadoutText: ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase,
    private val observeBrakeWearReadoutText: ObserveLmuWindowsBrakeWearReadoutTextUseCase,
    private val observeTyreWearReadoutText: ObserveLmuWindowsTyreWearReadoutTextUseCase,
    private val observeTyreOverheatReadoutText: ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase,
    private val observeTyreColdReadoutText: ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase,
    private val observeOverheatReadoutText: ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase,
    private val observePartDetachedReadoutText: ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase,
    private val observeTyreDetachedReadoutText: ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase,
    private val observeMyBestLapReadoutText: ObserveLmuWindowsMyBestLapReadoutTextUseCase,
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
        val text = (event as ReadoutTextEvent).resolvedText ?: eventText(event) ?: return null
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }

    private suspend fun eventText(event: SpeechEvent): String? =
        when (event) {
            is LmuWindowsYellowFlag -> {
                observeSectorYellowFlagReadoutText().first()
            }

            is LmuWindowsBlueFlag -> {
                observeBlueFlagReadoutText().first()
            }

            is LmuWindowsFullCourseYellow -> {
                observeFullCourseYellowFlagReadoutText().first()
            }

            is LmuWindowsRedFlag -> {
                observeRedFlagReadoutText().first()
            }

            is LmuWindowsCarLeft -> {
                observeStartLeftReadoutText().first()
            }

            is LmuWindowsCarLeftSustained -> {
                observeSustainedLeftReadoutText().first()
            }

            is LmuWindowsCarRight -> {
                observeStartRightReadoutText().first()
            }

            is LmuWindowsCarRightSustained -> {
                observeSustainedRightReadoutText().first()
            }

            is LmuWindowsMyBestLap -> {
                myBestLapText(event)
            }

            is LmuWindowsPitTimingWarning -> {
                pitTimingText(event)
            }

            else -> {
                thresholdWarningText(event)
            }
        }

    /** 閾値や温度を文言に埋め込む警告（残量・タイヤ摩耗・ブレーキ温度・タイヤ温度）の文言。対象外イベントは null。 */
    private suspend fun thresholdWarningText(event: SpeechEvent): String? =
        when (event) {
            is LmuWindowsTyreOverheat -> {
                tyreTemperatureText(event.celsius) { observeTyreOverheatReadoutText() }
            }

            is LmuWindowsTyreCold -> {
                tyreTemperatureText(event.celsius) { observeTyreColdReadoutText() }
            }

            is LmuWindowsRemainingVirtualEnergyWarning -> {
                remainingVirtualEnergyText(event)
            }

            is LmuWindowsBrakeOverheat -> {
                brakeTemperatureText(event)
            }

            is LmuWindowsBrakeWearLow -> {
                brakeWearText(event)
            }

            is LmuWindowsTyreWearWarning -> {
                tyreWearText(event)
            }

            else -> {
                vehicleDamageText(event)
            }
        }

    private suspend fun vehicleDamageText(event: SpeechEvent): String? =
        when (event) {
            is LmuWindowsOverheating -> observeOverheatReadoutText().first()
            is LmuWindowsPartDetached -> observePartDetachedReadoutText().first()
            is LmuWindowsTyreDetached -> observeTyreDetachedReadoutText().first()
            else -> null
        }

    private suspend fun myBestLapText(event: LmuWindowsMyBestLap): String =
        formatLmuWindowsMyBestLapReadoutText(observeMyBestLapReadoutText().first(), event.lapTimeMs)

    private suspend fun tyreTemperatureText(
        celsius: Int,
        observeTemplate: () -> Flow<String>,
    ): String = formatLmuWindowsTyreTemperatureReadoutText(observeTemplate().first(), celsius)

    private suspend fun remainingVirtualEnergyText(event: LmuWindowsRemainingVirtualEnergyWarning): String =
        formatLmuWindowsRemainingVirtualEnergyReadoutText(
            observeRemainingVirtualEnergyReadoutText().first(),
            event.percentage,
        )

    private suspend fun brakeTemperatureText(event: LmuWindowsBrakeOverheat): String =
        formatLmuWindowsBrakeTemperatureReadoutText(
            observeBrakeTemperatureReadoutText().first(),
            event.celsius,
        )

    private suspend fun brakeWearText(event: LmuWindowsBrakeWearLow): String =
        formatLmuWindowsBrakeWearReadoutText(
            observeBrakeWearReadoutText().first(),
            event.percent,
        )

    private suspend fun tyreWearText(event: LmuWindowsTyreWearWarning): String =
        formatLmuWindowsTyreWearReadoutText(
            observeTyreWearReadoutText().first(),
            event.percentage,
        )

    private suspend fun pitTimingText(event: LmuWindowsPitTimingWarning): String? {
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
