package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsBrakeWearCurrentValue
import kurou.kodriver.domain.engine.LmuWindowsManualReadoutText
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyCurrentValue
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX_ROUNDING_TOLERANCE
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_EMPTY_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_EMPTY_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsBrakeTemperatureReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsBrakeWearReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsClassCarGapReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsMyBestLapReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsPitTimingReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsTyreWearReadoutText
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemDefinitions
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemName
import kurou.kodriver.domain.model.LmuWindowsManualReadoutItem
import kurou.kodriver.domain.model.LmuWindowsManualReadoutValue
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.MANUAL_READOUT_NO_DATA_TEXT
import kurou.kodriver.domain.model.MANUAL_READOUT_SET_ITEM_MAX
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutClassCarGapItem
import kurou.kodriver.domain.model.joinManualReadoutTexts
import kurou.kodriver.domain.model.manualReadoutNoDataText
import kotlin.math.roundToInt

private const val LMU_WINDOWS_PERCENTAGE_SCALE = 100.0

class DetermineLmuWindowsManualReadoutUseCase {
    /** 対応項目の現在値を保存順に連結し、警告済み状態を維持する。 */
    fun determineManualReadout(
        state: LmuWindowsNarratorState,
        items: List<LmuWindowsManualReadoutItem>,
    ): LmuWindowsNarratorReadoutDecision {
        val supportedItems =
            items.filter { it.key in lmuWindowsManualReadoutItemKeys() }.take(MANUAL_READOUT_SET_ITEM_MAX)
        val firstKey = supportedItems.firstOrNull()?.key
        val key =
            lmuWindowsManualReadoutItemDefinitions[firstKey]?.eventKey as? LmuWindowsReadoutItemKey
                ?: LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root
        val text = joinManualReadoutTexts(supportedItems.map { item -> manualReadoutItemText(item) })
        return LmuWindowsNarratorReadoutDecision(
            state = state,
            events = listOf(LmuWindowsManualReadoutText(key, text)),
        )
    }

    /** 自動判定の条件や状態を変更せず、蓄積済みのエネルギー消費量から残り周回数を読む。 */
    fun manualPitTimingVirtualEnergyRemainingLaps(state: LmuWindowsNarratorState): Int? =
        manualPitTimingRemainingLaps(state.pitTimingVirtualEnergyTrackingState)

    /** 自動判定の条件や状態を変更せず、蓄積済みのタイヤ摩耗量から残り周回数を読む。 */
    fun manualPitTimingTyreWearRemainingLaps(state: LmuWindowsNarratorState): Int? =
        manualPitTimingRemainingLaps(state.pitTimingTyreWearTrackingState)

    /** 自動判定の条件や状態を変更せず、蓄積済みの燃料消費量から残り周回数を読む。 */
    fun manualFuelRemainingLaps(state: LmuWindowsNarratorState): Int? =
        manualPitTimingRemainingLaps(state.fuelLapsTrackingState)

    private fun manualPitTimingRemainingLaps(tracking: LmuWindowsPitTimingTrackingState): Int? {
        if (tracking.currentLap == -1) return null
        val consumption = tracking.lastValidLapConsumption ?: return null
        if (!consumption.isFinite() || consumption <= 0.0) return null
        if (!tracking.currentValue.isFinite() || tracking.currentValue < 0.0) return null
        val remainingLaps = tracking.currentValue / consumption
        if (!remainingLaps.isFinite()) return null
        val maximumLaps = LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX.toDouble()
        if (remainingLaps > maximumLaps) {
            val roundingExcess = remainingLaps - maximumLaps
            if (roundingExcess > LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX_ROUNDING_TOLERANCE) return null
            return LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX
        }
        return remainingLaps.toInt()
    }

    private fun isManualRemainingLapsItem(key: String): Boolean =
        key == LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value ||
            key == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value ||
            key == LmuWindowsReadoutItemKey.RemainingFuel.Laps.value

    private fun manualRemainingLapsText(
        item: LmuWindowsManualReadoutItem,
        remainingLaps: Int,
    ): String? =
        remainingLaps.takeIf { it >= 0 }?.let {
            if (it == 0 && item.textTemplate.isNotBlank()) {
                if (item.key == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value) {
                    LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_EMPTY_TEXT_DEFAULT
                } else if (item.key == LmuWindowsReadoutItemKey.RemainingFuel.Laps.value) {
                    LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT
                } else {
                    LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_EMPTY_TEXT_DEFAULT
                }
            } else {
                formatLmuWindowsPitTimingReadoutText(item.textTemplate, it)
            }
        }

    /** クラス内前後車とのギャップの文。トップ・最後尾は固定文言（空欄テンプレートは空文字）。データ無しは null。 */
    private fun manualClassCarGapText(
        item: LmuWindowsManualReadoutItem,
        data: LmuWindowsClassCarGapData,
    ): String? {
        val isAhead = item.key == LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value
        val isEdge =
            if (isAhead) {
                data.classPosition == 1
            } else {
                data.classPosition != null && data.classPosition == data.classVehicleCount
            }
        if (isEdge) {
            if (item.textTemplate.isBlank()) return ""
            return if (isAhead) {
                LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT
            } else {
                LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT
            }
        }
        val seconds = (if (isAhead) data.gapAheadSeconds else data.gapBehindSeconds) ?: return null
        if (!seconds.isFinite() || seconds < 0.0) return null
        return formatLmuWindowsClassCarGapReadoutText(item.textTemplate, seconds)
    }

    /** 項目キーと値の種別が対応しない場合や値が無い場合は null（データ無し）にする。 */
    private fun manualReadoutItemText(item: LmuWindowsManualReadoutItem): String {
        val text =
            when (val value = item.value) {
                is LmuWindowsManualReadoutValue.Ratio -> {
                    manualRatioText(item, value.value)
                }

                is LmuWindowsManualReadoutValue.Celsius -> {
                    manualCelsiusText(item, value.value)
                }

                is LmuWindowsManualReadoutValue.LapTimeMs -> {
                    manualLapTimeText(item, value.value)
                }

                is LmuWindowsManualReadoutValue.RemainingLaps -> {
                    manualRemainingLapsText(item, value.value).takeIf { isManualRemainingLapsItem(item.key) }
                }

                is LmuWindowsManualReadoutValue.ClassCarGap -> {
                    manualClassCarGapText(
                        item,
                        value.data,
                    ).takeIf { isLmuWindowsManualReadoutClassCarGapItem(item.key) }
                }

                null -> {
                    null
                }
            }
        return text ?: manualReadoutNoDataText(lmuWindowsManualReadoutItemName(item.key))
    }

    private fun manualLapTimeText(
        item: LmuWindowsManualReadoutItem,
        lapTimeMs: Long,
    ): String? =
        lapTimeMs
            .takeIf { it > 0 && item.key == LmuWindowsReadoutItemKey.MyBestLap.Root.value }
            ?.let { formatLmuWindowsMyBestLapReadoutText(item.textTemplate, it) }

    private fun manualCelsiusText(
        item: LmuWindowsManualReadoutItem,
        celsius: Float,
    ): String? =
        when (item.key) {
            LmuWindowsReadoutItemKey.TyreTemperature.Root.value -> {
                formatLmuWindowsTyreTemperatureReadoutText(item.textTemplate, celsius.roundToInt())
            }

            LmuWindowsReadoutItemKey.BrakeTemperature.Root.value -> {
                formatLmuWindowsBrakeTemperatureReadoutText(item.textTemplate, celsius.roundToInt())
            }

            else -> {
                null
            }
        }

    private fun manualRatioText(
        item: LmuWindowsManualReadoutItem,
        ratio: Double,
    ): String? {
        val percentage = (ratio * LMU_WINDOWS_PERCENTAGE_SCALE).roundToInt()
        return when (item.key) {
            LmuWindowsReadoutItemKey.RemainingFuel.Root.value -> {
                formatLmuWindowsRemainingFuelReadoutText(item.textTemplate, percentage)
            }

            LmuWindowsReadoutItemKey.BrakeWear.Root.value -> {
                formatLmuWindowsBrakeWearReadoutText(item.textTemplate, percentage)
            }

            LmuWindowsReadoutItemKey.TyreWear.Root.value -> {
                formatLmuWindowsTyreWearReadoutText(item.textTemplate, percentage)
            }

            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value -> {
                formatLmuWindowsRemainingVirtualEnergyReadoutText(item.textTemplate, percentage)
            }

            else -> {
                null
            }
        }
    }

    /** 自動読み上げスイッチに関係なく現在の残量（未受信・鮮度切れならデータ無し）を読み上げ、警告済み状態を維持する。 */
    fun determineManualRemainingVirtualEnergyCurrentValue(
        state: LmuWindowsNarratorState,
        data: LmuWindowsVirtualEnergyData?,
        textTemplate: String? = null,
    ): LmuWindowsNarratorReadoutDecision =
        LmuWindowsNarratorReadoutDecision(
            state = state,
            events =
                listOf(
                    LmuWindowsRemainingVirtualEnergyCurrentValue(
                        data?.let { (it.remainingRatio.value * LMU_WINDOWS_PERCENTAGE_SCALE).roundToInt() } ?: 0,
                        resolvedText =
                            if (data == null) {
                                MANUAL_READOUT_NO_DATA_TEXT
                            } else {
                                textTemplate?.let {
                                    formatLmuWindowsRemainingVirtualEnergyReadoutText(
                                        it,
                                        (data.remainingRatio.value * LMU_WINDOWS_PERCENTAGE_SCALE).roundToInt(),
                                    )
                                }
                            },
                    ),
                ),
        )

    /** 最も残量が少ないブレーキの現在値を読み上げ、警告済み状態を維持する。 */
    fun determineManualBrakeWearCurrentValue(
        state: LmuWindowsNarratorState,
        data: LmuWindowsBrakeWearRemainingData?,
        textTemplate: String? = null,
    ): LmuWindowsNarratorReadoutDecision {
        val minimum = if (data == null) null else data.wheels.values.minOfOrNull { it.remainingPercent }
        val percentage = if (minimum == null) null else minimum.roundToInt()
        val resolvedText =
            if (percentage == null) {
                MANUAL_READOUT_NO_DATA_TEXT
            } else if (textTemplate == null) {
                null
            } else {
                formatLmuWindowsBrakeWearReadoutText(textTemplate, percentage)
            }
        return LmuWindowsNarratorReadoutDecision(
            state = state,
            events = listOf(LmuWindowsBrakeWearCurrentValue(percentage ?: 0, resolvedText)),
        )
    }
}
