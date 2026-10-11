package kurou.kodriver.feature.telemetryloglist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog

private val previewAceWindowsTelemetryLogs: List<TelemetryLog> =
    listOf(
        TelemetryLog(
            id = 3,
            createdAt = 1_840_000,
            simulator = Simulator.AceWindows,
            readoutItemKey = AceWindowsReadoutItemKey.RemainingFuel.Root,
            narratedText = "残り燃料警告",
            narrationOutcome = NarrationOutcome.QUEUED,
            telemetryJson = """{"remainingFuelLiters":8.2}""",
        ),
    )

private val previewLmuWindowsTelemetryLogs: List<TelemetryLog> =
    listOf(
        TelemetryLog(
            id = 2,
            createdAt = 1_820_000,
            simulator = Simulator.LmuWindows,
            readoutItemKey = LmuWindowsReadoutItemKey.Flag.Root,
            narratedText = "イエローフラッグ",
            narrationOutcome = NarrationOutcome.INTERRUPTED,
            telemetryJson = """{"flag":"green","sector1":"clear","sector2":"clear","sector3":"clear"}""",
        ),
        TelemetryLog(
            id = 4,
            createdAt = 1_810_000,
            simulator = Simulator.LmuWindows,
            readoutItemKey = LmuWindowsReadoutItemKey.VehicleApproach.Root,
            narratedText = "カーレフト",
            narrationOutcome = NarrationOutcome.SKIPPED,
            telemetryJson = """{"left":true,"right":false}""",
        ),
    )

private val previewGt7Ps5TelemetryLogs: List<TelemetryLog> =
    listOf(
        TelemetryLog(
            id = 1,
            createdAt = 1_800_000,
            simulator = Simulator.Gt7Ps5,
            readoutItemKey = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
            narratedText = "燃料は残り約3周",
            narrationOutcome = NarrationOutcome.SPOKEN,
            telemetryJson = """{"remainingFuelLaps":3.6,"fuelPercent":18.2}""",
        ),
    )

internal val previewTelemetryLogListUiState =
    TelemetryLogListUiState(
        logs =
            (previewAceWindowsTelemetryLogs + previewLmuWindowsTelemetryLogs + previewGt7Ps5TelemetryLogs)
                .sortedByDescending { it.createdAt },
    )
