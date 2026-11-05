package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.serialization.encodeToString
import kurou.kodriver.core.narrator.TelemetryLogJson
import kurou.kodriver.core.narrator.TelemetryLogJsonCurrentField
import kurou.kodriver.core.narrator.TelemetryLogJsonPreviousField
import kurou.kodriver.core.narrator.buildTelemetryLogJson
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreDetachedData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleDamageData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.lmuwindows.usecase.TyreTemperatureReadoutInput

internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsVehicleApproachData?,
    current: LmuWindowsVehicleApproachData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousVehicleApproach",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "vehicleApproach",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsTelemetryData?,
    current: LmuWindowsTelemetryData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous = TelemetryLogJsonPreviousField(name = "previousTelemetry", json = previous?.toJson()),
        current = TelemetryLogJsonCurrentField(name = "telemetry", json = current.toJson()),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

private fun LmuWindowsTelemetryData.toJson(): String =
    "{" +
        """"currentLapTimeMs":${timing.currentLapTimeMs},""" +
        """"lastLapTimeMs":${timing.lastLapTimeMs},""" +
        """"bestLapTimeMs":${timing.bestLapTimeMs},""" +
        """"currentLap":${timing.currentLap},""" +
        """"maxLaps":${timing.maxLaps}""" +
        "}"

internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsVehicleDamageData?,
    current: LmuWindowsVehicleDamageData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousVehicleDamage",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current = TelemetryLogJsonCurrentField(name = "vehicleDamage", json = TelemetryLogJson.encodeToString(current)),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsTyreDetachedData?,
    current: LmuWindowsTyreDetachedData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousTyreDetached",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current = TelemetryLogJsonCurrentField(name = "tyreDetached", json = TelemetryLogJson.encodeToString(current)),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * タイヤ摩耗判定入力（[LmuWindowsTyreWearData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase.determineTyreWear]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsTyreWearData?,
    current: LmuWindowsTyreWearData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousTyreWear",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current = TelemetryLogJsonCurrentField(name = "tyreWear", json = TelemetryLogJson.encodeToString(current)),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * ブレーキ温度判定入力（[LmuWindowsBrakeTemperatureData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsBrakeReadoutUseCase.determineBrakeTemperatureOverheat]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsBrakeTemperatureData?,
    current: LmuWindowsBrakeTemperatureData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousBrakeTemperature",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "brakeTemperature",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * ブレーキ残量判定入力（[LmuWindowsBrakeWearRemainingData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsBrakeReadoutUseCase.determineBrakeWearLow]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 */
internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsBrakeWearRemainingData?,
    current: LmuWindowsBrakeWearRemainingData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousBrakeWear",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "brakeWear",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * バーチャルエネルギー残量判定入力（[LmuWindowsVirtualEnergyData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVirtualEnergyReadoutUseCase.determineRemainingVirtualEnergy]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsVirtualEnergyData?,
    current: LmuWindowsVirtualEnergyData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousRemainingVirtualEnergy",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "remainingVirtualEnergy",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsRemainingFuelData?,
    current: LmuWindowsRemainingFuelData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousRemainingFuel",
                json = previous?.let { TelemetryLogJson.encodeToString(it) },
            ),
        current =
            TelemetryLogJsonCurrentField(
                name = "remainingFuel",
                json = TelemetryLogJson.encodeToString(current),
            ),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

@Suppress("LongParameterList")
internal fun buildPitTimingTelemetryLogJson(
    state: LmuWindowsNarratorState,
    telemetry: LmuWindowsTelemetryData,
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    tyreWear: LmuWindowsTyreWearData?,
    fuel: LmuWindowsRemainingFuelData?,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"telemetry":${telemetry.toJson()},""" +
        """"virtualEnergy":${TelemetryLogJson.encodeToString(virtualEnergy)},""" +
        """"tyreWear":${TelemetryLogJson.encodeToString(tyreWear)},""" +
        """"fuel":${TelemetryLogJson.encodeToString(fuel)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"

/**
 * フラッグ判定入力（[LmuWindowsRaceFlagsData]）は判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsRaceFlagReadoutUseCase.determineRaceFlags]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    previous: LmuWindowsRaceFlagsData?,
    current: LmuWindowsRaceFlagsData,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    finalState: LmuWindowsNarratorState,
): String =
    buildTelemetryLogJson(
        stateJson = TelemetryLogJson.encodeToString(state),
        previous =
            TelemetryLogJsonPreviousField(
                name = "previousRaceFlags",
                json =
                    previous?.let {
                        TelemetryLogJson.encodeToString(it)
                    },
            ),
        current = TelemetryLogJsonCurrentField(name = "raceFlags", json = TelemetryLogJson.encodeToString(current)),
        settingsJson = TelemetryLogJson.encodeToString(settings),
        observedAtMs = observedAtMs,
        finalStateJson = TelemetryLogJson.encodeToString(finalState),
    )

/**
 * タイヤ温度読み上げの入力は [TyreTemperatureReadoutInput] で判定ロジック（
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase.determineTyreTemperatureOverheat] /
 * [kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase.determineTyreTemperatureLow]）と
 * 共有しているため、フィールドを手動で選ばず [TelemetryLogJson] でシリアライズしてそのまま記録する。
 * これにより判定に使う入力が増えても記録側の更新漏れが構造的に起こらない。
 */
internal fun buildTelemetryLogJson(
    state: LmuWindowsNarratorState,
    input: TyreTemperatureReadoutInput,
    settings: LmuWindowsNarratorReadoutSettings,
    observedAtMs: Long,
    overheatState: LmuWindowsNarratorState,
    finalState: LmuWindowsNarratorState,
): String =
    "{" +
        """"state":${TelemetryLogJson.encodeToString(state)},""" +
        """"input":${TelemetryLogJson.encodeToString(input)},""" +
        """"settings":${TelemetryLogJson.encodeToString(settings)},""" +
        """"observedAtMs":$observedAtMs,""" +
        """"overheatState":${TelemetryLogJson.encodeToString(overheatState)},""" +
        """"finalState":${TelemetryLogJson.encodeToString(finalState)}""" +
        "}"
