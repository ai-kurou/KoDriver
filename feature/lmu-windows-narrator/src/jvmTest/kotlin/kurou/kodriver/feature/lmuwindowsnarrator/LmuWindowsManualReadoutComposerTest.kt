package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsClassCarGapData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearRatio
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyRatio
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemTextDefault
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsManualReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsPitTimingTrackingState
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.LmuWindowsManualReadoutValue
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsManualReadoutComposerTest {
    private val composer = LmuWindowsManualReadoutComposer(DetermineLmuWindowsManualReadoutUseCase())

    private val brakeWearKey = LmuWindowsReadoutItemKey.BrakeWear.Root.value
    private val tyreWearKey = LmuWindowsReadoutItemKey.TyreWear.Root.value
    private val fuelKey = LmuWindowsReadoutItemKey.RemainingFuel.Root.value
    private val energyKey = LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value
    private val brakeTemperatureKey = LmuWindowsReadoutItemKey.BrakeTemperature.Root.value
    private val tyreTemperatureKey = LmuWindowsReadoutItemKey.TyreTemperature.Root.value
    private val bestLapKey = LmuWindowsReadoutItemKey.MyBestLap.Root.value
    private val classCarGapKey = LmuWindowsReadoutItemKey.ClassCarGap.Root.value
    private val fuelLapsKey = LmuWindowsReadoutItemKey.RemainingFuel.Laps.value
    private val energyLapsKey = LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value
    private val tyreLapsKey = LmuWindowsReadoutItemKey.PitTiming.TyreWear.value

    private val classCarGap = LmuWindowsClassCarGapData(gapAheadSeconds = 1.0, gapBehindSeconds = 2.0)

    private val state =
        LmuWindowsNarratorState(
            pitTimingVirtualEnergyTrackingState = tracking(currentValue = 0.8, consumption = 0.2),
            pitTimingTyreWearTrackingState = tracking(currentValue = 0.75, consumption = 0.25),
            fuelLapsTrackingState = tracking(currentValue = 50.0, consumption = 10.0),
        )

    private val allKeys =
        listOf(
            brakeWearKey,
            tyreWearKey,
            fuelKey,
            energyKey,
            brakeTemperatureKey,
            tyreTemperatureKey,
            bestLapKey,
            classCarGapKey,
            fuelLapsKey,
            energyLapsKey,
            tyreLapsKey,
        )

    private fun tracking(
        currentValue: Double,
        consumption: Double,
    ) = LmuWindowsPitTimingTrackingState(
        currentLap = 2,
        currentValue = currentValue,
        lastValidLapConsumption = consumption,
    )

    private fun <T : Any> received(
        value: T?,
        receivedAtMs: Long = 0L,
    ) = LmuWindowsReceivedValue(value, receivedAtMs)

    private fun brakeWear(vararg percents: Float) =
        LmuWindowsBrakeWearRemainingData(
            WheelIndex.entries.zip(percents.toList()).associate { (wheel, percent) ->
                wheel to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.03f), percent)
            },
        )

    private fun readings(vararg celsius: Float) =
        WheelIndex.entries.zip(celsius.toList()).associate { (wheel, value) -> wheel to CelsiusReading(value) }

    private fun snapshot(
        receivedAtMs: Long = 0L,
        pitTimingUpdatedAtMs: Long = 0L,
        bestLapTimeMs: Long? = 90_000L,
    ) = LmuWindowsManualReadoutSnapshot(
        virtualEnergy = received(LmuWindowsVirtualEnergyData(LmuWindowsVirtualEnergyRatio(0.5)), receivedAtMs),
        fuel = received(LmuWindowsRemainingFuelData(remainingPercent = 40, exactPercent = 40.5), receivedAtMs),
        classCarGap = received(classCarGap, receivedAtMs),
        tyreWear =
            received(
                LmuWindowsTyreWearData(
                    mapOf(
                        WheelIndex.FRONT_LEFT to LmuWindowsTyreWearRatio(0.9),
                        WheelIndex.FRONT_RIGHT to LmuWindowsTyreWearRatio(0.6),
                    ),
                ),
                receivedAtMs,
            ),
        brakeWear = received(brakeWear(80f, 70f), receivedAtMs),
        brakeTemperature =
            received(LmuWindowsBrakeTemperatureData(readings(300f, 400f, Float.NaN)), receivedAtMs),
        tyreTemperature =
            received(
                LmuWindowsTyreCarcassTemperatureData(readings(Float.POSITIVE_INFINITY, 90f, 95f)),
                receivedAtMs,
            ),
        bestLapTimeMs = received(bestLapTimeMs, receivedAtMs),
        pitTimingUpdatedAtMs = pitTimingUpdatedAtMs,
    )

    private fun values(
        snapshot: LmuWindowsManualReadoutSnapshot,
        observedAtMs: Long,
        keys: List<String> = allKeys,
        narratorState: LmuWindowsNarratorState = state,
    ) = composer.compose(ManualReadoutSet("set", keys), snapshot, narratorState, observedAtMs).map { it.value }

    @Test
    fun `鮮度の上限ちょうどまでは項目ごとの現在値を返す`() {
        assertEquals(
            listOf(
                LmuWindowsManualReadoutValue.Ratio(0.7),
                LmuWindowsManualReadoutValue.Ratio(0.6),
                LmuWindowsManualReadoutValue.Ratio(0.405),
                LmuWindowsManualReadoutValue.Ratio(0.5),
                LmuWindowsManualReadoutValue.Celsius(400f),
                LmuWindowsManualReadoutValue.Celsius(95f),
                LmuWindowsManualReadoutValue.LapTimeMs(90_000L),
                LmuWindowsManualReadoutValue.ClassCarGap(classCarGap),
                LmuWindowsManualReadoutValue.RemainingLaps(5),
                LmuWindowsManualReadoutValue.RemainingLaps(4),
                LmuWindowsManualReadoutValue.RemainingLaps(3),
            ),
            values(snapshot(), observedAtMs = 5_000L),
        )
    }

    @Test
    fun `鮮度の上限を超えると全項目がデータ無しになる`() {
        assertEquals(List(allKeys.size) { null }, values(snapshot(), observedAtMs = 5_001L))
    }

    @Test
    fun `未受信の項目はデータ無しになる`() {
        val empty =
            LmuWindowsManualReadoutSnapshot(
                virtualEnergy = received(null),
                fuel = received(null),
                classCarGap = received(null),
                tyreWear = received(null),
                brakeWear = received(null),
                brakeTemperature = received(null),
                tyreTemperature = received(null),
                bestLapTimeMs = received(null),
                pitTimingUpdatedAtMs = 0L,
            )

        assertEquals(List(allKeys.size) { null }, values(empty, observedAtMs = 0L))
    }

    @Test
    fun `ピット判定の更新が古いと燃料以外の残り周回数はデータ無しになる`() {
        assertEquals(
            listOf(LmuWindowsManualReadoutValue.RemainingLaps(5), null, null),
            values(
                snapshot(receivedAtMs = 10_000L, pitTimingUpdatedAtMs = 0L),
                observedAtMs = 10_000L,
                keys = listOf(fuelLapsKey, energyLapsKey, tyreLapsKey),
            ),
        )
    }

    @Test
    fun `消費量の蓄積前の残り周回数はデータ無しになる`() {
        assertEquals(
            listOf(null, null, null),
            values(
                snapshot(),
                observedAtMs = 0L,
                keys = listOf(fuelLapsKey, energyLapsKey, tyreLapsKey),
                narratorState = LmuWindowsNarratorState(),
            ),
        )
    }

    @Test
    fun `ゼロ以下の自己ベストはデータ無しになる`() {
        assertEquals(
            listOf(null),
            values(snapshot(bestLapTimeMs = 0L), observedAtMs = 0L, keys = listOf(bestLapKey)),
        )
    }

    @Test
    fun `車輪の値が無いブレーキとタイヤはデータ無しになる`() {
        val noWheels =
            snapshot().copy(
                brakeWear = received(brakeWear()),
                tyreWear = received(LmuWindowsTyreWearData(emptyMap())),
                brakeTemperature = received(LmuWindowsBrakeTemperatureData(emptyMap())),
                tyreTemperature = received(LmuWindowsTyreCarcassTemperatureData(readings(Float.NaN))),
            )

        assertEquals(
            listOf(null, null, null, null),
            values(
                noWheels,
                observedAtMs = 0L,
                keys = listOf(brakeWearKey, tyreWearKey, brakeTemperatureKey, tyreTemperatureKey),
            ),
        )
    }

    @Test
    fun `対応していない項目を除き保存順と保存文言を使い未保存の項目は既定文言を使う`() {
        val items =
            composer.compose(
                ManualReadoutSet(
                    id = "set",
                    itemKeys = listOf(energyKey, "unknown", fuelKey),
                    itemTexts = mapOf(energyKey to "保存した文言"),
                ),
                snapshot(),
                state,
                observedAtMs = 0L,
            )

        assertEquals(listOf(energyKey, fuelKey), items.map { it.key })
        assertEquals(
            listOf("保存した文言", lmuWindowsManualReadoutItemTextDefault(fuelKey)),
            items.map { it.textTemplate },
        )
    }

    @Test
    fun `受信値は鮮度の上限までは返し超えるとnullになる`() {
        val value = received(1, receivedAtMs = 1_000L)

        assertEquals(1, value.freshValue(observedAtMs = 6_000L))
        assertNull(value.freshValue(observedAtMs = 6_001L))
    }
}
