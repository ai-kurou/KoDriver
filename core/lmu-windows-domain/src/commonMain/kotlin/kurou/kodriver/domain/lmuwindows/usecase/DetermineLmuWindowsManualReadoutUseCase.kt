package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsBrakeWearCurrentValue
import kurou.kodriver.domain.engine.LmuWindowsManualReadoutText
import kurou.kodriver.domain.engine.LmuWindowsRemainingVirtualEnergyCurrentValue
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_NO_DATA_TEXT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX_ROUNDING_TOLERANCE
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_EMPTY_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_EMPTY_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsBrakeTemperatureReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsBrakeWearReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsMyBestLapReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsPitTimingReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsRemainingFuelReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsRemainingVirtualEnergyReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsTyreTemperatureReadoutText
import kurou.kodriver.domain.lmuwindows.model.formatLmuWindowsTyreWearReadoutText
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemName
import kurou.kodriver.domain.model.LmuWindowsManualReadoutItem
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.MANUAL_READOUT_NO_ITEMS_TEXT
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutTemperatureItem
import kotlin.math.roundToInt

private const val LMU_WINDOWS_PERCENTAGE_SCALE = 100.0

class DetermineLmuWindowsManualReadoutUseCase {
    /** 対応項目の現在値を保存順に連結し、警告済み状態を維持する。 */
    fun determineManualReadout(
        state: LmuWindowsNarratorState,
        items: List<LmuWindowsManualReadoutItem>,
    ): LmuWindowsNarratorReadoutDecision {
        val supportedItems = items.filter { it.key in lmuWindowsManualReadoutItemKeys() }
        val key =
            when (supportedItems.firstOrNull()?.key) {
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value,
                LmuWindowsReadoutItemKey.PitTiming.TyreWear.value,
                -> LmuWindowsReadoutItemKey.PitTiming.Root

                LmuWindowsReadoutItemKey.RemainingFuel.Laps.value,
                LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                -> LmuWindowsReadoutItemKey.RemainingFuel.Root

                LmuWindowsReadoutItemKey.TyreWear.Root.value -> LmuWindowsReadoutItemKey.TyreWear.Root

                LmuWindowsReadoutItemKey.BrakeWear.Root.value -> LmuWindowsReadoutItemKey.BrakeWear.Root

                LmuWindowsReadoutItemKey.BrakeTemperature.Root.value -> LmuWindowsReadoutItemKey.BrakeTemperature.Root

                LmuWindowsReadoutItemKey.MyBestLap.Root.value -> LmuWindowsReadoutItemKey.MyBestLap.Root

                LmuWindowsReadoutItemKey.TyreTemperature.Root.value -> LmuWindowsReadoutItemKey.TyreTemperature.Root

                else -> LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root
            }
        val text =
            when {
                supportedItems.isEmpty() -> {
                    MANUAL_READOUT_NO_ITEMS_TEXT
                }

                supportedItems.all { isManualReadoutItemWithoutData(it) } -> {
                    LMU_WINDOWS_MANUAL_READOUT_NO_DATA_TEXT
                }

                else -> {
                    supportedItems
                        .map { item -> manualReadoutItemText(item) }
                        .filter { it.isNotBlank() }
                        .joinToString("、")
                }
            }
        return LmuWindowsNarratorReadoutDecision(
            state = state,
            events = listOf(LmuWindowsManualReadoutText(key, text)),
        )
    }

    private fun isManualReadoutItemWithoutData(item: LmuWindowsManualReadoutItem): Boolean =
        when {
            isManualRemainingLapsItem(item.key) -> {
                (item.remainingLaps ?: -1) < 0
            }

            item.key == LmuWindowsReadoutItemKey.MyBestLap.Root.value -> {
                (item.lapTimeMs ?: 0L) <= 0
            }

            isLmuWindowsManualReadoutTemperatureItem(item.key) -> {
                item.celsius == null
            }

            else -> {
                item.ratio == null
            }
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

    private fun manualRemainingLapsText(item: LmuWindowsManualReadoutItem): String? =
        item.remainingLaps?.takeIf { it >= 0 }?.let {
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

    private fun isManualLapBasedItem(key: String): Boolean =
        isManualRemainingLapsItem(key) || key == LmuWindowsReadoutItemKey.MyBestLap.Root.value

    /** エネルギー・タイヤ摩耗・燃料の残り周回数と自己ベストラップの文。データ無しは null。 */
    private fun manualLapBasedText(item: LmuWindowsManualReadoutItem): String? =
        if (isManualRemainingLapsItem(item.key)) {
            manualRemainingLapsText(item)
        } else {
            item.lapTimeMs?.takeIf { it > 0 }?.let { formatLmuWindowsMyBestLapReadoutText(item.textTemplate, it) }
        }

    private fun manualReadoutItemText(item: LmuWindowsManualReadoutItem): String {
        val noDataText =
            "${lmuWindowsManualReadoutItemName(item.key)}は$LMU_WINDOWS_MANUAL_READOUT_NO_DATA_TEXT"
        if (isManualLapBasedItem(item.key)) return manualLapBasedText(item) ?: noDataText
        if (isLmuWindowsManualReadoutTemperatureItem(item.key)) {
            return item.celsius?.let {
                if (item.key == LmuWindowsReadoutItemKey.TyreTemperature.Root.value) {
                    formatLmuWindowsTyreTemperatureReadoutText(item.textTemplate, it.roundToInt())
                } else {
                    formatLmuWindowsBrakeTemperatureReadoutText(item.textTemplate, it.roundToInt())
                }
            } ?: noDataText
        }
        val percentage = item.ratio?.let { (it * LMU_WINDOWS_PERCENTAGE_SCALE).roundToInt() } ?: return noDataText
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

            else -> {
                formatLmuWindowsRemainingVirtualEnergyReadoutText(item.textTemplate, percentage)
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
                                LMU_WINDOWS_MANUAL_READOUT_NO_DATA_TEXT
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
                LMU_WINDOWS_MANUAL_READOUT_NO_DATA_TEXT
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
