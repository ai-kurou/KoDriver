@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.model.TelemetryLogDetail
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.LmuWindowsNarratorReadoutSettings
import kurou.kodriver.domain.usecase.LmuWindowsNarratorState
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class LmuWindowsNarratorEventProcessorTest {

    @Test
    fun `読み上げたイベントを直前と現在の接近データとともに保存する`() = runTest {
        val telemetryLogRepository = EventProcessorTelemetryLogRepository()
        val ttsEngine = EventProcessorRecordingTts()
        val processor = createProcessor(telemetryLogRepository, ttsEngine)

        processor.processVehicleApproach(
            vehicleApproach = leftVehicleApproach(distance = 4.0),
            events = emptyList(),
            readoutOrder = emptyList(),
            queueEnabledStates = emptyMap(),
            observedAtMs = 100L,
            logContext = logContext(),
        )
        processor.processVehicleApproach(
            vehicleApproach = leftVehicleApproach(distance = 3.0),
            events = listOf(SpeechEvent.CarLeft),
            readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
            queueEnabledStates = emptyMap(),
            observedAtMs = 200L,
            logContext = logContext(),
        )

        val log = telemetryLogRepository.logs.single()
        assertEquals(200L, log.createdAt)
        assertEquals(Simulator.LmuWindows.id, log.simulatorId)
        assertEquals(ReadoutItemKey.LmuWindows.VehicleApproach.Root.value, log.readoutItemKey)
        assertContains(log.telemetryJson, """"previousVehicleApproach":{"sideBySideLeftVehicleIds":[1]""")
        assertContains(log.telemetryJson, """"lateralDistanceLeftMeters":4.0""")
        assertContains(log.telemetryJson, """"vehicleApproach":{"sideBySideLeftVehicleIds":[1]""")
        assertContains(log.telemetryJson, """"lateralDistanceLeftMeters":3.0""")
        assertContains(log.telemetryJson, """"observedAtMs":200""")
        assertEquals(listOf(SpokenEvent(SpeechEvent.CarLeft, queue = false)), ttsEngine.spokenEvents)
        assertEquals(1, ttsEngine.currentReadoutItemKeyReadCount)
    }

    @Test
    fun `優先度の高い項目を再生中なら読み上げも保存もしない`() = runTest {
        val currentKey = ReadoutItemKey.LmuWindows.Flag.Root
        val newEvent = SpeechEvent.CarLeft
        val telemetryLogRepository = EventProcessorTelemetryLogRepository()
        val ttsEngine = EventProcessorRecordingTts(currentKey = currentKey)
        val processor = createProcessor(telemetryLogRepository, ttsEngine)

        processor.processVehicleApproach(
            vehicleApproach = leftVehicleApproach(),
            events = listOf(newEvent),
            readoutOrder = listOf(
                currentKey,
                newEvent.readoutItemKey,
            ),
            queueEnabledStates = emptyMap(),
            observedAtMs = 0L,
            logContext = logContext(),
        )

        assertEquals(0, ttsEngine.stopCount)
        assertEquals(emptyList<SpokenEvent>(), ttsEngine.spokenEvents)
        assertEquals(emptyList<TelemetryLog>(), telemetryLogRepository.logs)
        assertEquals(1, ttsEngine.currentReadoutItemKeyReadCount)
    }

    @Test
    fun `優先度で本来無視される項目でもキュー設定が有効ならキュー再生する`() = runTest {
        val currentKey = ReadoutItemKey.LmuWindows.Flag.Root
        val newEvent = SpeechEvent.CarLeft
        val telemetryLogRepository = EventProcessorTelemetryLogRepository()
        val ttsEngine = EventProcessorRecordingTts()
        val processor = createProcessor(telemetryLogRepository, ttsEngine)

        processor.processVehicleApproach(
            vehicleApproach = leftVehicleApproach(),
            events = listOf(newEvent),
            readoutOrder = listOf(currentKey, newEvent.readoutItemKey),
            queueEnabledStates = mapOf(newEvent.readoutItemKey to true),
            observedAtMs = 0L,
            logContext = logContext(),
        )

        assertEquals(0, ttsEngine.stopCount)
        assertEquals(listOf(SpokenEvent(newEvent, queue = true)), ttsEngine.spokenEvents)
        assertEquals(1, telemetryLogRepository.logs.size)
    }

    @Test
    fun `優先度の低い項目を再生中なら停止して読み上げる`() = runTest {
        val currentKey = ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root
        val newEvent = SpeechEvent.CarLeft
        val telemetryLogRepository = EventProcessorTelemetryLogRepository()
        val ttsEngine = EventProcessorRecordingTts(currentKey = currentKey)
        val processor = createProcessor(telemetryLogRepository, ttsEngine)

        processor.processVehicleApproach(
            vehicleApproach = leftVehicleApproach(),
            events = listOf(newEvent),
            readoutOrder = listOf(newEvent.readoutItemKey, currentKey),
            queueEnabledStates = emptyMap(),
            observedAtMs = 0L,
            logContext = logContext(),
        )

        assertEquals(1, ttsEngine.stopCount)
        assertEquals(listOf(SpokenEvent(newEvent, queue = false)), ttsEngine.spokenEvents)
        assertEquals(1, ttsEngine.currentReadoutItemKeyReadCount)
        assertEquals(1, telemetryLogRepository.logs.size)
    }

    @Test
    fun `ログ保存に失敗しても次の読み上げは継続する`() = runTest {
        val telemetryLogRepository = EventProcessorTelemetryLogRepository(failureCount = 1)
        val ttsEngine = EventProcessorRecordingTts()
        val processor = createProcessor(telemetryLogRepository, ttsEngine)

        processor.processVehicleApproach(
            vehicleApproach = leftVehicleApproach(distance = 4.0),
            events = listOf(SpeechEvent.CarLeft),
            readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
            queueEnabledStates = emptyMap(),
            observedAtMs = 100L,
            logContext = logContext(),
        )
        processor.processVehicleApproach(
            vehicleApproach = leftVehicleApproach(distance = 3.0),
            events = listOf(SpeechEvent.CarLeft),
            readoutOrder = listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root),
            queueEnabledStates = emptyMap(),
            observedAtMs = 200L,
            logContext = logContext(),
        )

        assertEquals(
            listOf(
                SpokenEvent(SpeechEvent.CarLeft, queue = false),
                SpokenEvent(SpeechEvent.CarLeft, queue = false),
            ),
            ttsEngine.spokenEvents,
        )
        assertEquals(2, telemetryLogRepository.saveCount)
        assertEquals(2, ttsEngine.currentReadoutItemKeyReadCount)
    }

    private fun createProcessor(
        telemetryLogRepository: TelemetryLogRepository,
        ttsEngine: TextToSpeechEngine,
    ) = LmuWindowsNarratorEventProcessor(
        ttsEngine = ttsEngine,
        saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
    )
}

private data class SpokenEvent(val event: SpeechEvent, val queue: Boolean)

private class EventProcessorRecordingTts(
    private var currentKey: ReadoutItemKey? = null,
) : TextToSpeechEngine {
    val spokenEvents = mutableListOf<SpokenEvent>()
    var currentReadoutItemKeyReadCount = 0
    var stopCount = 0

    override val currentReadoutItemKey: ReadoutItemKey?
        get() {
            currentReadoutItemKeyReadCount += 1
            return currentKey
        }

    override fun speak(event: SpeechEvent, queue: Boolean) {
        spokenEvents += SpokenEvent(event, queue)
    }

    override fun stop() {
        stopCount += 1
        currentKey = null
    }

    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}

private class EventProcessorTelemetryLogRepository(
    private var failureCount: Int = 0,
) : TelemetryLogRepository {
    val logs = mutableListOf<TelemetryLog>()
    var saveCount = 0

    override fun observeTelemetryLogs(): Flow<List<TelemetryLog>> = emptyFlow()

    override fun observeTelemetryLogDetail(id: Long): Flow<TelemetryLogDetail?> = emptyFlow()

    override suspend fun saveTelemetryLog(
        createdAt: Long,
        simulatorId: String,
        readoutItemKey: String,
        telemetryJson: String,
    ) {
        saveCount += 1
        if (failureCount > 0) {
            failureCount -= 1
            error("failed")
        }
        logs += TelemetryLog(
            id = 0,
            createdAt = createdAt,
            simulatorId = simulatorId,
            readoutItemKey = readoutItemKey,
            telemetryJson = telemetryJson,
        )
    }

    override suspend fun deleteAllTelemetryLogs() = Unit
}

private fun logContext() = LmuWindowsTelemetryLogContext(
    state = LmuWindowsNarratorState(),
    settings = LmuWindowsNarratorReadoutSettings(
        enabledStates = mapOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root to true),
        myBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        redFlagVoiceType = RedFlagVoiceType.SESSION_STOP,
        currentLap = 1,
        skipFirstLap = false,
        vehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
        vehicleApproachSustainedApproachDurationSeconds = 7,
        vehicleApproachSustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
        tyreTemperatureHighThresholdCelsius = 95,
        tyreTemperatureLowWarningPhases = emptySet(),
        remainingVirtualEnergyLapsThreshold = 3,
        remainingVirtualEnergyLapsEnabled = false,
    ),
    finalState = LmuWindowsNarratorState(),
)

private fun leftVehicleApproach(distance: Double = 3.0) = LmuWindowsVehicleApproachData(
    sideBySideLeftVehicleIds = setOf(1),
    sideBySideRightVehicleIds = emptySet(),
    lateralDistanceLeftMeters = distance,
    lateralDistanceRightMeters = Double.MAX_VALUE,
)
