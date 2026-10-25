package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_NO_DATA_TEXT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.acewindows.model.AceWindowsBestLapTimeData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagData
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagType
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.acewindows.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.acewindows.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemKeys
import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemName
import kurou.kodriver.domain.acewindows.model.formatAceWindowsMyBestLapReadoutText
import kurou.kodriver.domain.acewindows.model.formatAceWindowsRemainingFuelLapsReadoutText
import kurou.kodriver.domain.acewindows.model.formatAceWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.acewindows.model.formatAceWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.engine.AceWindowsBlackFlag
import kurou.kodriver.domain.engine.AceWindowsBlackWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsBlueFlag
import kurou.kodriver.domain.engine.AceWindowsCheckeredFlag
import kurou.kodriver.domain.engine.AceWindowsGreenFlag
import kurou.kodriver.domain.engine.AceWindowsManualReadoutText
import kurou.kodriver.domain.engine.AceWindowsMyBestLap
import kurou.kodriver.domain.engine.AceWindowsMyBestLapCurrentValue
import kurou.kodriver.domain.engine.AceWindowsOrangeCircleFlag
import kurou.kodriver.domain.engine.AceWindowsRedFlag
import kurou.kodriver.domain.engine.AceWindowsRedYellowStripesFlag
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelCurrentValue
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsCurrentValue
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelLapsWarning
import kurou.kodriver.domain.engine.AceWindowsRemainingFuelWarning
import kurou.kodriver.domain.engine.AceWindowsTyreOverheat
import kurou.kodriver.domain.engine.AceWindowsTyreTemperatureCurrentValue
import kurou.kodriver.domain.engine.AceWindowsVehicleApproach
import kurou.kodriver.domain.engine.AceWindowsWhiteFlag
import kurou.kodriver.domain.engine.AceWindowsYellowFlag
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.model.AceWindowsManualReadoutItem
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.MANUAL_READOUT_NO_ITEMS_TEXT
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.usecase.determineHysteresisState
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * ACE 向け読み上げ判定の継続状態。
 *
 * 同じ低燃料警告・同じフラッグ状態・同じタイヤ過熱状態・同じ車両接近状態・同じ燃料残り周回数を連続で読み上げないため、
 * 前回の判定結果を保持する。
 */
data class AceWindowsNarratorState(
    val remainingFuelWarned: Boolean = false,
    val previousFlag: AceWindowsFlagType? = null,
    val tyreOverheating: Boolean = false,
    val vehicleApproaching: Boolean = false,
    val personalBestMs: Int = Int.MAX_VALUE,
    val previousBestLapTimeMs: Int? = null,
    /** 直近に観測した現在ラップ番号。減少を新しいセッションの開始とみなし、自己ベストの状態をリセットする。 */
    val previousLapCount: Int? = null,
    /** 直近に読み上げた（または給油後に基準とした）燃料残り周回数。閾値を上回っている間は null。 */
    val lastRemainingFuelLaps: Int? = null,
)

/** ACE 向け読み上げ判定で参照するユーザー設定。 */
data class AceWindowsNarratorReadoutSettings(
    val enabledStates: Map<ReadoutItemKey, Boolean>,
    val remainingFuelThresholdPercentage: Int,
    val tyreTemperatureHighThresholdCelsius: Celsius = ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
    val vehicleApproachThresholdMeters: Double = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
    val remainingFuelLapsThreshold: Int = ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT,
)

/** ACE 向け読み上げ判定の結果。次回へ渡す状態と、今回再生すべきイベントを含む。 */
data class AceWindowsNarratorReadoutDecision(
    val state: AceWindowsNarratorState,
    val events: List<ReadoutTextEvent>,
)

/**
 * ACE の燃料残量・フラッグ状態・タイヤカーカス温度・周辺車両接近から、今回読み上げるべき音声イベントを決定する UseCase。
 *
 * 注意: ACE の共有メモリには自車の向きに相当するフィールドが存在せず、周辺車両との直線距離
 * （[AceWindowsVehicleApproachData.nearbyVehicles]）のみが取得できるため、LMU（`LmuWindowsCarLeft`/
 * `LmuWindowsCarRight` 等）のような左右を区別した接近アナウンスはできない。単一の閾値
 * （[AceWindowsNarratorReadoutSettings.vehicleApproachThresholdMeters]）を下回る車両が1台でもいれば、
 * 左右を区別しない [AceWindowsVehicleApproach] を読み上げる。
 *
 * 燃料残り周回数（[AceWindowsReadoutItemKey.RemainingFuelLaps.Root]）は、GT7 のように燃料消費を自前で追跡せず、
 * ACE が算出した [AceWindowsRemainingFuelLapsData.remainingLaps] の整数部を使う（[determineRemainingFuelLaps]）。
 * 閾値以下になった時点と、以降1周減るごとに読み上げる。給油で周回数が増えた場合は基準を更新し、
 * 整数境界付近の揺れで同じ周回数を繰り返し読み上げないよう、基準の次の整数から0.5周以上の増加のみ給油とみなす。
 *
 * 自己ベストラップ（[AceWindowsReadoutItemKey.MyBestLap.Root]）は [AceWindowsBestLapTimeData.bestLapTimeMs] の
 * 更新を [determineMyBestLap] で判定する。GT7（[DetermineGt7Ps5NarratorReadoutUseCase.determineMyBestLap]）と
 * 同じく、共有メモリのベストラップタイムは Int（milliseconds）でセッション内の値を都度返すため、
 * 前回値との比較（[AceWindowsNarratorState.previousBestLapTimeMs]）と読み上げ済みベスト
 * （[AceWindowsNarratorState.personalBestMs]）の2段構えで二重読み上げを防ぐ。
 */
class DetermineAceWindowsNarratorReadoutUseCase {
    /** セットの対応項目を保存順に連結し、自動読み上げの警告済み状態を維持する。 */
    fun determineManualReadout(
        state: AceWindowsNarratorState,
        items: List<AceWindowsManualReadoutItem>,
    ): AceWindowsNarratorReadoutDecision {
        val supportedKeys = aceWindowsManualReadoutItemKeys()
        val supportedItems = items.filter { it.key in supportedKeys }.take(MANUAL_READOUT_SET_ITEM_MAX)
        val text =
            if (supportedItems.isEmpty()) {
                MANUAL_READOUT_NO_ITEMS_TEXT
            } else {
                supportedItems
                    .map { item -> manualCurrentValue(item).let { it.resolvedText ?: it.narratedText } }
                    .filter { it.isNotBlank() }
                    .joinToString("、")
            }
        return AceWindowsNarratorReadoutDecision(
            state = state,
            events = listOf(AceWindowsManualReadoutText(AceWindowsReadoutItemKey.RemainingFuel.Root, text)),
        )
    }

    private fun manualCurrentValue(item: AceWindowsManualReadoutItem): ReadoutTextEvent =
        when (item.key) {
            AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value -> manualRemainingFuelLapsValue(item)
            AceWindowsReadoutItemKey.MyBestLap.Root.value -> manualMyBestLapValue(item)
            AceWindowsReadoutItemKey.TyreTemperature.Root.value -> manualTyreTemperatureValue(item)
            else -> manualRemainingFuelValue(item)
        }

    private fun manualTyreTemperatureValue(item: AceWindowsManualReadoutItem): ReadoutTextEvent {
        val celsius = item.value ?: 0
        val event =
            AceWindowsTyreTemperatureCurrentValue(
                celsius,
                formatAceWindowsTyreTemperatureReadoutText(item.textTemplate, celsius),
            )
        return withManualNoDataText(item, event)
    }

    /** 未受信・鮮度切れ・全輪が非有限または0以下の場合はデータなしとして扱う。 */
    fun currentMaxTyreTemperatureCelsius(data: AceWindowsTyreCarcassTemperatureData?): Int? {
        val wheels = data?.wheels ?: return null
        return wheels.values
            .map { it.value }
            .filter { it.isFinite() && it > 0f }
            .maxOrNull()
            ?.roundToInt()
    }

    private fun manualMyBestLapValue(item: AceWindowsManualReadoutItem): ReadoutTextEvent {
        val lapTimeMs = item.value ?: 0
        val event =
            AceWindowsMyBestLapCurrentValue(
                lapTimeMs,
                formatAceWindowsMyBestLapReadoutText(item.textTemplate, lapTimeMs),
            )
        return withManualNoDataText(item, event)
    }

    private fun manualRemainingFuelValue(item: AceWindowsManualReadoutItem): ReadoutTextEvent {
        val value = item.value ?: 0
        val event =
            AceWindowsRemainingFuelCurrentValue(
                value,
                formatAceWindowsRemainingFuelReadoutText(item.textTemplate, value),
            )
        return withManualNoDataText(item, event)
    }

    private fun manualRemainingFuelLapsValue(item: AceWindowsManualReadoutItem): ReadoutTextEvent {
        val laps = item.value ?: 0
        val event =
            AceWindowsRemainingFuelLapsCurrentValue(
                laps,
                formatAceWindowsRemainingFuelLapsReadoutText(item.textTemplate, laps),
            )
        return withManualNoDataText(item, event)
    }

    private fun withManualNoDataText(
        item: AceWindowsManualReadoutItem,
        event: ReadoutTextEvent,
    ): ReadoutTextEvent =
        when {
            item.textTemplate.isBlank() -> {
                event.withResolvedText("")
            }

            item.value == null -> {
                event.withResolvedText(
                    "${aceWindowsManualReadoutItemName(item.key)}は$ACE_WINDOWS_MANUAL_READOUT_NO_DATA_TEXT",
                )
            }

            else -> {
                event
            }
        }

    /** 未受信・鮮度切れ・範囲外や非有限の燃料値は手動読み上げのデータなしとして扱う。 */
    fun currentRemainingFuelPercent(data: AceWindowsFuelData?): Int? {
        val percent = data?.remainingPercent?.value ?: return null
        if (!percent.isFinite() || percent < 0.0 || percent > 100.0) return null
        return percent.roundToInt()
    }

    /** 未受信または未計測の自己ベストラップは手動読み上げのデータなしとして扱う。 */
    fun currentBestLapTimeMs(data: AceWindowsBestLapTimeData?): Int? = data?.bestLapTimeMs?.takeIf { it > 0 }

    /** 未受信・鮮度切れ・非有限・0以下・1,000周超の値はデータなしとし、1周未満の正数は整数部へ切り捨てる。 */
    fun currentRemainingFuelLaps(data: AceWindowsRemainingFuelLapsData?): Int? {
        val remaining = data?.remainingLaps ?: return null
        if (
            !remaining.isFinite() ||
            remaining <= 0f ||
            remaining > ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX
        ) {
            return null
        }
        return floor(remaining).toInt()
    }

    fun determineMyBestLap(
        state: AceWindowsNarratorState,
        data: AceWindowsBestLapTimeData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val current = data.bestLapTimeMs
        val currentLap = data.currentLap
        val sessionState =
            if (state.previousLapCount != null && currentLap < state.previousLapCount) {
                state.copy(personalBestMs = Int.MAX_VALUE, previousBestLapTimeMs = null)
            } else {
                state
            }
        val stateWithCurrentBestLap = sessionState.copy(previousBestLapTimeMs = current, previousLapCount = currentLap)
        val previous = sessionState.previousBestLapTimeMs
        if (previous == null) return AceWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (current <= 0) return AceWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        if (previous > 0 && current >= previous) {
            return AceWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (current >= sessionState.personalBestMs) {
            return AceWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }
        if (!settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.MyBestLap.Root) ||
            !settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.MyBestLap.DetailEnabled)
        ) {
            return AceWindowsNarratorReadoutDecision(stateWithCurrentBestLap, emptyList())
        }

        return AceWindowsNarratorReadoutDecision(
            state = stateWithCurrentBestLap.copy(personalBestMs = current),
            events = listOf(AceWindowsMyBestLap(lapTimeMs = current)),
        )
    }

    fun determineRemainingFuelLaps(
        state: AceWindowsNarratorState,
        data: AceWindowsRemainingFuelLapsData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val remaining = data.remainingLaps
        // 消費実績がない間は 0（または非有限値）のため、燃料切れと区別できず判定しない。
        if (!remaining.isFinite() || remaining <= 0f) return AceWindowsNarratorReadoutDecision(state, emptyList())
        val laps = floor(remaining).toInt()
        if (laps > settings.remainingFuelLapsThreshold) {
            return AceWindowsNarratorReadoutDecision(state.copy(lastRemainingFuelLaps = null), emptyList())
        }
        val last = state.lastRemainingFuelLaps
        if (last != null && laps >= last) {
            val refueled = remaining >= last + 1 + REFUEL_HYSTERESIS_LAPS
            val nextState = if (refueled) state.copy(lastRemainingFuelLaps = laps) else state
            return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        }
        // 無効中も基準を進め、再有効化だけで同じ残り周回数を読み直さない。
        val nextState = state.copy(lastRemainingFuelLaps = laps)
        if (!settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.RemainingFuelLaps.Root) ||
            !settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled)
        ) {
            return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        }
        return AceWindowsNarratorReadoutDecision(
            state = nextState,
            events = listOf(AceWindowsRemainingFuelLapsWarning(laps)),
        )
    }

    fun determineRemainingFuel(
        state: AceWindowsNarratorState,
        data: AceWindowsFuelData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val isLow =
            data.remainingPercent.value > 0.0 &&
                data.remainingPercent.value <= settings.remainingFuelThresholdPercentage
        val shouldAnnounce =
            !state.remainingFuelWarned && isLow &&
                settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.RemainingFuel.Root) &&
                settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled)
        return AceWindowsNarratorReadoutDecision(
            state = state.copy(remainingFuelWarned = isLow),
            events =
                if (shouldAnnounce) {
                    val rounded = data.remainingPercent.value.roundToInt()
                    val percent = rounded.coerceIn(0, 100)
                    listOf(AceWindowsRemainingFuelWarning(percent))
                } else {
                    emptyList()
                },
        )
    }

    fun determineFlag(
        state: AceWindowsNarratorState,
        data: AceWindowsFlagData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val previous = state.previousFlag
        val nextState = state.copy(previousFlag = data.flag)
        if (previous == null) return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        if (!settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.Flag.Root)) {
            return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        }
        if (data.flag == previous) return AceWindowsNarratorReadoutDecision(nextState, emptyList())
        val event =
            flagEvent(data.flag)
                ?.takeIf { (itemKey, _) ->
                    settings.enabledStates.readoutEnabled(itemKey)
                }?.second
        return AceWindowsNarratorReadoutDecision(nextState, listOfNotNull(event))
    }

    /** 全輪の最大カーカス温度。過熱判定済み（1輪以上が閾値以上）の場合のみ呼ぶ。 */
    private fun maxCelsius(data: AceWindowsTyreCarcassTemperatureData): Int {
        val maxReading = data.wheels.values.maxOf { it.value }
        return maxReading.roundToInt()
    }

    fun determineTyreTemperatureOverheat(
        state: AceWindowsNarratorState,
        data: AceWindowsTyreCarcassTemperatureData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val hotThreshold = settings.tyreTemperatureHighThresholdCelsius.value.toFloat()
        val nextOverheating =
            determineHysteresisState(
                valuesCelsius = data.wheels.values.map { it.value },
                highThresholdCelsius = hotThreshold,
                hysteresisCelsius = TYRE_OVERHEAT_HYSTERESIS_CELSIUS,
                currentState = state.tyreOverheating,
            )
        val shouldAnnounce =
            !state.tyreOverheating && nextOverheating &&
                settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.TyreTemperature.Root) &&
                settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning)
        return AceWindowsNarratorReadoutDecision(
            state = state.copy(tyreOverheating = nextOverheating),
            events = if (shouldAnnounce) listOf(AceWindowsTyreOverheat(maxCelsius(data))) else emptyList(),
        )
    }

    fun determineVehicleApproach(
        state: AceWindowsNarratorState,
        data: AceWindowsVehicleApproachData,
        settings: AceWindowsNarratorReadoutSettings,
    ): AceWindowsNarratorReadoutDecision {
        val isApproaching = data.nearbyVehicles.any { it.distanceMeters <= settings.vehicleApproachThresholdMeters }
        val shouldAnnounce =
            !state.vehicleApproaching && isApproaching &&
                settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.VehicleApproach.Root) &&
                settings.enabledStates.readoutEnabled(AceWindowsReadoutItemKey.VehicleApproach.StartReadout)
        return AceWindowsNarratorReadoutDecision(
            state = state.copy(vehicleApproaching = isApproaching),
            events = if (shouldAnnounce) listOf(AceWindowsVehicleApproach()) else emptyList(),
        )
    }

    private fun flagEvent(flag: AceWindowsFlagType): Pair<ReadoutItemKey, ReadoutTextEvent>? =
        when (flag) {
            AceWindowsFlagType.WHITE_FLAG -> {
                AceWindowsReadoutItemKey.Flag.WhiteFlag to AceWindowsWhiteFlag()
            }

            AceWindowsFlagType.GREEN_FLAG -> {
                AceWindowsReadoutItemKey.Flag.GreenFlag to AceWindowsGreenFlag()
            }

            AceWindowsFlagType.RED_FLAG -> {
                AceWindowsReadoutItemKey.Flag.RedFlag to AceWindowsRedFlag()
            }

            AceWindowsFlagType.BLUE_FLAG -> {
                AceWindowsReadoutItemKey.Flag.BlueFlag to AceWindowsBlueFlag()
            }

            AceWindowsFlagType.YELLOW_FLAG -> {
                AceWindowsReadoutItemKey.Flag.YellowFlag to AceWindowsYellowFlag()
            }

            AceWindowsFlagType.BLACK_FLAG -> {
                AceWindowsReadoutItemKey.Flag.BlackFlag to AceWindowsBlackFlag()
            }

            AceWindowsFlagType.BLACK_WHITE_FLAG -> {
                AceWindowsReadoutItemKey.Flag.BlackWhiteFlag to AceWindowsBlackWhiteFlag()
            }

            AceWindowsFlagType.CHECKERED_FLAG -> {
                AceWindowsReadoutItemKey.Flag.CheckeredFlag to AceWindowsCheckeredFlag()
            }

            AceWindowsFlagType.ORANGE_CIRCLE_FLAG -> {
                AceWindowsReadoutItemKey.Flag.OrangeCircleFlag to AceWindowsOrangeCircleFlag()
            }

            AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG -> {
                AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag to AceWindowsRedYellowStripesFlag()
            }

            AceWindowsFlagType.NO_FLAG, AceWindowsFlagType.UNKNOWN -> {
                null
            }
        }

    private companion object {
        const val TYRE_OVERHEAT_HYSTERESIS_CELSIUS = 5f

        /** 給油による燃料残り周回数の増加とみなす、前回基準の次の整数からの余裕（周）。 */
        const val REFUEL_HYSTERESIS_LAPS = 0.5f
    }
}
