package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.engine.DebugReadoutText
import kurou.kodriver.domain.engine.LmuWindowsBlueFlag
import kurou.kodriver.domain.engine.LmuWindowsBrakeOverheat
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearCurrentValue
import kurou.kodriver.domain.engine.LmuWindowsBrakeWearLow
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.LmuWindowsCarLeftSustained
import kurou.kodriver.domain.engine.LmuWindowsCarRight
import kurou.kodriver.domain.engine.LmuWindowsCarRightSustained
import kurou.kodriver.domain.engine.LmuWindowsFlagSpeechEvent
import kurou.kodriver.domain.engine.LmuWindowsFullCourseYellow
import kurou.kodriver.domain.engine.LmuWindowsManualReadoutText
import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.engine.LmuWindowsOverheating
import kurou.kodriver.domain.engine.LmuWindowsPartDetached
import kurou.kodriver.domain.engine.LmuWindowsPitTimingWarning
import kurou.kodriver.domain.engine.LmuWindowsRedFlag
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyCurrentValue
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyWarning
import kurou.kodriver.domain.engine.LmuWindowsSpeechEvent
import kurou.kodriver.domain.engine.LmuWindowsThresholdWarningSpeechEvent
import kurou.kodriver.domain.engine.LmuWindowsTyreCold
import kurou.kodriver.domain.engine.LmuWindowsTyreDetached
import kurou.kodriver.domain.engine.LmuWindowsTyreOverheat
import kurou.kodriver.domain.engine.LmuWindowsTyreWearWarning
import kurou.kodriver.domain.engine.LmuWindowsVehicleApproachSpeechEvent
import kurou.kodriver.domain.engine.LmuWindowsVehicleDamageSpeechEvent
import kurou.kodriver.domain.engine.LmuWindowsYellowFlag
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
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * フラッグ・車両接近・ピットタイミング・バーチャルエナジー残量警告・タイヤ摩耗警告・ブレーキ過熱警告・タイヤ温度警告・車両故障警告・自己ベストラップ更新の自由文字列をOS標準TTSで読み上げる、
 * [NarratorEngine][kurou.kodriver.core.narrator.NarratorEngine] 用のフック。
 * 空欄またはTTSが利用できない場合は本文を読み上げない。
 * LMU イベントと [DebugReadoutText] を対象とし、LMU の各グループを網羅的に解決する。
 * 他シミュレーターのイベントは解決済み本文があっても読み上げない。
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
 * - [LmuWindowsRemainingVirtualEnergyCurrentValue] : 現在の残量の解決済み文言（未指定なら既定文言）
 * - [LmuWindowsRemainingVirtualEnergyWarning] : 設定した残量閾値の警告
 * - [LmuWindowsOverheating] : オーバーヒート
 * - [LmuWindowsPartDetached] : 部品脱落
 * - [LmuWindowsTyreDetached] : タイヤ脱落
 * - [LmuWindowsBrakeOverheat] : 設定した温度閾値の警告
 * - [LmuWindowsBrakeWearLow] : 設定したブレーキ残量閾値の警告
 * - [LmuWindowsTyreWearWarning] : 設定した残存率閾値の警告
 * - [LmuWindowsPitTimingWarning] : バーチャルエナジー・タイヤ摩耗由来のピットタイミング
 */
internal class LmuWindowsReadoutTextSpeaker(
    private val readoutTexts: LmuWindowsReadoutTextUseCases,
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
        val text =
            when (event) {
                is DebugReadoutText -> event.resolvedText
                is LmuWindowsSpeechEvent -> event.resolvedText ?: lmuWindowsText(event)
                else -> null
            } ?: return null
        return text.takeIf { it.isNotBlank() && checkTextToSpeechAvailable() }
    }

    private suspend fun lmuWindowsText(event: LmuWindowsSpeechEvent): String =
        when (event) {
            is LmuWindowsFlagSpeechEvent -> flagText(event)
            is LmuWindowsVehicleApproachSpeechEvent -> vehicleApproachText(event)
            is LmuWindowsVehicleDamageSpeechEvent -> vehicleDamageText(event)
            is LmuWindowsThresholdWarningSpeechEvent -> thresholdWarningText(event)
            is LmuWindowsMyBestLap -> myBestLapText(event)
            is LmuWindowsRemainingVirtualEnergyCurrentValue -> event.narratedText
            is LmuWindowsBrakeWearCurrentValue -> event.narratedText
            is LmuWindowsManualReadoutText -> event.narratedText
            is LmuWindowsPitTimingWarning -> pitTimingText(event)
        }

    private suspend fun flagText(event: LmuWindowsFlagSpeechEvent): String =
        when (event) {
            is LmuWindowsYellowFlag -> {
                readoutTexts.flag.sectorYellow().first()
            }

            is LmuWindowsBlueFlag -> {
                readoutTexts.flag.blue().first()
            }

            is LmuWindowsFullCourseYellow -> {
                readoutTexts.flag.fullCourseYellow().first()
            }

            is LmuWindowsRedFlag -> {
                readoutTexts.flag.red().first()
            }
        }

    private suspend fun vehicleApproachText(event: LmuWindowsVehicleApproachSpeechEvent): String =
        when (event) {
            is LmuWindowsCarLeft -> {
                readoutTexts.vehicleApproach.startLeft().first()
            }

            is LmuWindowsCarLeftSustained -> {
                readoutTexts.vehicleApproach.sustainedLeft().first()
            }

            is LmuWindowsCarRight -> {
                readoutTexts.vehicleApproach.startRight().first()
            }

            is LmuWindowsCarRightSustained -> {
                readoutTexts.vehicleApproach.sustainedRight().first()
            }
        }

    /** 閾値や温度を文言に埋め込む警告（残量・タイヤ摩耗・ブレーキ温度・タイヤ温度）の文言。 */
    private suspend fun thresholdWarningText(event: LmuWindowsThresholdWarningSpeechEvent): String =
        when (event) {
            is LmuWindowsTyreOverheat -> {
                tyreTemperatureText(event.celsius) { readoutTexts.tyre.overheat() }
            }

            is LmuWindowsTyreCold -> {
                tyreTemperatureText(event.celsius) { readoutTexts.tyre.cold() }
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
        }

    private suspend fun vehicleDamageText(event: LmuWindowsVehicleDamageSpeechEvent): String =
        when (event) {
            is LmuWindowsOverheating -> readoutTexts.vehicleDamage.overheat().first()
            is LmuWindowsPartDetached -> readoutTexts.vehicleDamage.partDetached().first()
            is LmuWindowsTyreDetached -> readoutTexts.vehicleDamage.tyreDetached().first()
        }

    private suspend fun myBestLapText(event: LmuWindowsMyBestLap): String =
        formatLmuWindowsMyBestLapReadoutText(readoutTexts.myBestLap().first(), event.lapTimeMs)

    private suspend fun tyreTemperatureText(
        celsius: Int,
        observeTemplate: () -> Flow<String>,
    ): String = formatLmuWindowsTyreTemperatureReadoutText(observeTemplate().first(), celsius)

    private suspend fun remainingVirtualEnergyText(event: LmuWindowsRemainingVirtualEnergyWarning): String =
        formatLmuWindowsRemainingVirtualEnergyReadoutText(
            readoutTexts.remainingVirtualEnergy().first(),
            event.percentage,
        )

    private suspend fun brakeTemperatureText(event: LmuWindowsBrakeOverheat): String =
        formatLmuWindowsBrakeTemperatureReadoutText(
            readoutTexts.brake.temperature().first(),
            event.celsius,
        )

    private suspend fun brakeWearText(event: LmuWindowsBrakeWearLow): String =
        formatLmuWindowsBrakeWearReadoutText(
            readoutTexts.brake.wear().first(),
            event.percent,
        )

    private suspend fun tyreWearText(event: LmuWindowsTyreWearWarning): String =
        formatLmuWindowsTyreWearReadoutText(
            readoutTexts.tyre.wear().first(),
            event.percentage,
        )

    private suspend fun pitTimingText(event: LmuWindowsPitTimingWarning): String {
        if (event.source == PitTimingSource.TyreWear) {
            return if (event.laps <= 0) {
                readoutTexts.pitTiming.tyreWearImminent().first()
            } else {
                formatLmuWindowsPitTimingReadoutText(readoutTexts.pitTiming.tyreWear().first(), event.laps)
            }
        }
        return if (event.laps <= 0) {
            readoutTexts.pitTiming.virtualEnergyImminent().first()
        } else {
            formatLmuWindowsPitTimingReadoutText(
                readoutTexts.pitTiming.virtualEnergy().first(),
                event.laps,
            )
        }
    }
}

/** DebugReadoutText と LMU 由来の全イベントを自由文言対象とし、判定をNarratorと本文再生で共有する。 */
internal fun isLmuWindowsCustomSpeakEvent(event: SpeechEvent): Boolean =
    event is DebugReadoutText || event is LmuWindowsSpeechEvent
