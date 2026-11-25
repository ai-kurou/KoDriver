package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemTextDefault
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsManualReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsManualReadoutItem
import kurou.kodriver.domain.model.LmuWindowsManualReadoutValue
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.NARRATOR_CURRENT_VALUE_MAX_AGE_MS
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutClassCarGapItem
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutTemperatureItem

/** 受信した最新値と受信時刻。未受信は [value] が null。 */
internal data class LmuWindowsReceivedValue<T : Any>(
    val value: T?,
    val receivedAtMs: Long,
) {
    /** 受信から [NARRATOR_CURRENT_VALUE_MAX_AGE_MS] 以内なら値を返し、超えていれば null。 */
    fun freshValue(observedAtMs: Long): T? =
        value?.takeIf { observedAtMs - receivedAtMs <= NARRATOR_CURRENT_VALUE_MAX_AGE_MS }
}

/** 手動読み上げの組み立てに使う、ある時点の最新テレメトリ。 */
internal data class LmuWindowsManualReadoutSnapshot(
    val virtualEnergy: LmuWindowsReceivedValue<LmuWindowsVirtualEnergyData>,
    val fuel: LmuWindowsReceivedValue<LmuWindowsRemainingFuelData>,
    val classCarGap: LmuWindowsReceivedValue<LmuWindowsClassCarGapData>,
    val tyreWear: LmuWindowsReceivedValue<LmuWindowsTyreWearData>,
    val brakeWear: LmuWindowsReceivedValue<LmuWindowsBrakeWearRemainingData>,
    val brakeTemperature: LmuWindowsReceivedValue<LmuWindowsBrakeTemperatureData>,
    val tyreTemperature: LmuWindowsReceivedValue<LmuWindowsTyreCarcassTemperatureData>,
    val bestLapTimeMs: LmuWindowsReceivedValue<Long>,
    val pitTimingUpdatedAtMs: Long,
)

/** 手動読み上げのセットとテレメトリのスナップショットから、読み上げ項目を組み立てる。 */
internal class LmuWindowsManualReadoutComposer(
    private val manualReadoutUseCase: DetermineLmuWindowsManualReadoutUseCase,
) {
    /** 対応していない項目は除き、鮮度切れ・未受信の値は null（データ無し）にして保存順に返す。 */
    fun compose(
        set: ManualReadoutSet,
        snapshot: LmuWindowsManualReadoutSnapshot,
        state: LmuWindowsNarratorState,
        observedAtMs: Long,
    ): List<LmuWindowsManualReadoutItem> {
        val energy = snapshot.virtualEnergy.freshValue(observedAtMs)
        val fuel = snapshot.fuel.freshValue(observedAtMs)
        val classCarGap = snapshot.classCarGap.freshValue(observedAtMs)
        val tyreWear = snapshot.tyreWear.freshValue(observedAtMs)
        val brakeWear = snapshot.brakeWear.freshValue(observedAtMs)
        val brakeTemperature = snapshot.brakeTemperature.freshValue(observedAtMs)
        val tyreTemperature = snapshot.tyreTemperature.freshValue(observedAtMs)
        val bestLapTimeMs = snapshot.bestLapTimeMs.freshValue(observedAtMs)?.takeIf { it > 0 }
        val pitTimingFresh = observedAtMs - snapshot.pitTimingUpdatedAtMs <= NARRATOR_CURRENT_VALUE_MAX_AGE_MS
        return set.itemKeys.filter { it in lmuWindowsManualReadoutItemKeys() }.map { key ->
            val ratio =
                ratio(
                    key = key,
                    brakeWear = brakeWear,
                    tyreWear = tyreWear,
                    fuel = fuel,
                    energy = energy,
                )
            val celsius = celsius(key, brakeTemperature?.wheels?.values, tyreTemperature?.wheels?.values)
            val remainingLaps =
                remainingLaps(
                    key = key,
                    state = state,
                    hasEnergy = energy != null,
                    hasTyreWear = tyreWear != null,
                    hasFuel = fuel != null,
                    pitTimingFresh = pitTimingFresh,
                )
            val value =
                ratio?.let { LmuWindowsManualReadoutValue.Ratio(it) }
                    ?: celsius?.let { LmuWindowsManualReadoutValue.Celsius(it) }
                    ?: bestLapTimeMs
                        ?.takeIf { key == LmuWindowsReadoutItemKey.MyBestLap.Root.value }
                        ?.let { LmuWindowsManualReadoutValue.LapTimeMs(it) }
                    ?: classCarGap
                        ?.takeIf { isLmuWindowsManualReadoutClassCarGapItem(key) }
                        ?.let { LmuWindowsManualReadoutValue.ClassCarGap(it) }
                    ?: remainingLaps?.let { LmuWindowsManualReadoutValue.RemainingLaps(it) }
            LmuWindowsManualReadoutItem(
                key,
                set.itemTexts[key] ?: lmuWindowsManualReadoutItemTextDefault(key),
                value,
            )
        }
    }

    private fun ratio(
        key: String,
        brakeWear: LmuWindowsBrakeWearRemainingData?,
        tyreWear: LmuWindowsTyreWearData?,
        fuel: LmuWindowsRemainingFuelData?,
        energy: LmuWindowsVirtualEnergyData?,
    ): Double? =
        when {
            key == LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value ||
                key == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value ||
                key == LmuWindowsReadoutItemKey.RemainingFuel.Laps.value ||
                key == LmuWindowsReadoutItemKey.MyBestLap.Root.value ||
                isLmuWindowsManualReadoutClassCarGapItem(key) ||
                isLmuWindowsManualReadoutTemperatureItem(key) -> {
                null
            }

            key == LmuWindowsReadoutItemKey.BrakeWear.Root.value -> {
                brakeWear?.let { data ->
                    data.wheels.values
                        .minOfOrNull { it.remainingPercent }
                        ?.let { it / PERCENTAGE_SCALE }
                }
            }

            key == LmuWindowsReadoutItemKey.TyreWear.Root.value -> {
                tyreWear
                    ?.wheels
                    ?.values
                    ?.minOrNull()
                    ?.value
            }

            key == LmuWindowsReadoutItemKey.RemainingFuel.Root.value -> {
                fuel?.exactPercent?.let { it / PERCENTAGE_SCALE }
            }

            else -> {
                energy?.remainingRatio?.value
            }
        }

    private fun remainingLaps(
        key: String,
        state: LmuWindowsNarratorState,
        hasEnergy: Boolean,
        hasTyreWear: Boolean,
        hasFuel: Boolean,
        pitTimingFresh: Boolean,
    ): Int? =
        when {
            key == LmuWindowsReadoutItemKey.RemainingFuel.Laps.value -> {
                if (hasFuel) manualReadoutUseCase.manualFuelRemainingLaps(state) else null
            }

            !pitTimingFresh -> {
                null
            }

            key == LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value && hasEnergy -> {
                manualReadoutUseCase.manualPitTimingVirtualEnergyRemainingLaps(state)
            }

            key == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value && hasTyreWear -> {
                manualReadoutUseCase.manualPitTimingTyreWearRemainingLaps(state)
            }

            else -> {
                null
            }
        }

    /** 温度系の手動読み上げ項目の最高温度。温度系以外、またはデータ無しなら null。 */
    private fun celsius(
        key: String,
        brakeReadings: Collection<CelsiusReading>?,
        tyreReadings: Collection<CelsiusReading>?,
    ): Float? =
        when (key) {
            LmuWindowsReadoutItemKey.BrakeTemperature.Root.value -> brakeReadings?.let { maxFiniteCelsius(it) }
            LmuWindowsReadoutItemKey.TyreTemperature.Root.value -> tyreReadings?.let { maxFiniteCelsius(it) }
            else -> null
        }

    /** 非有限値（NaN・無限大）を除いた最高温度。有効な値が無ければ null（データ無し扱い）。 */
    private fun maxFiniteCelsius(readings: Collection<CelsiusReading>): Float? =
        readings
            .map { it.value }
            .filter { it.isFinite() }
            .maxOrNull()
}

private const val PERCENTAGE_SCALE = 100.0
