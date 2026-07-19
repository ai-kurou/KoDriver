package kurou.kodriver.feature.gt7ps5narrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5NarratorEventProcessorTest {

    private val telemetryLogRepository: TelemetryLogRepository = mockk()
    private val ttsEngine: TextToSpeechEngine = mockk()

    @Test
    fun `直前のテレメトリがないイベントはnullとして保存する`() = runTest {
        val logs = mutableListOf<TelemetryLog>()
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(any(), any()) } just Runs
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } answers {
            logs += TelemetryLog(0, firstArg(), secondArg(), thirdArg(), arg(3))
        }
        val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root

        createProcessor().process(
            sourceKey = sourceKey,
            telemetry = telemetry(),
            events = listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
            readoutOrder = listOf(sourceKey),
            observedAtMs = 0L,
        )

        assertEquals(true, logs.single().telemetryJson.startsWith("{\"state\":{\"raw\":"))
        assertEquals(true, logs.single().telemetryJson.contains("\"previousTelemetry\":null"))
    }

    @Test
    fun `読み上げたイベントを直前と現在のテレメトリとともに保存する`() = runTest {
        val logs = mutableListOf<TelemetryLog>()
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(any(), any()) } just Runs
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } answers {
            logs += TelemetryLog(0, firstArg(), secondArg(), thirdArg(), arg(3))
        }
        val processor = createProcessor()
        val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root

        processor.process(sourceKey, telemetry(bestLapTimeMs = 60_000), emptyList(), emptyList(), 100L)
        processor.process(
            sourceKey,
            telemetry(bestLapTimeMs = 59_000),
            listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
            listOf(sourceKey),
            200L,
        )

        assertEquals(1, logs.size)
        assertEquals(200L, logs.single().createdAt)
        assertEquals(Simulator.Gt7Ps5.id, logs.single().simulatorId)
        assertEquals(sourceKey.value, logs.single().readoutItemKey)
        assertEquals(
            true,
            logs.single().telemetryJson.contains(
                """"previousTelemetry":{"lapCount":0,"lapsInRace":5,"bestLapTimeMs":60000,""" +
                    """"gasLevel":20.0,"gasCapacity":100.0}""",
            ),
        )
        assertEquals(
            true,
            logs.single().telemetryJson.contains(
                """"telemetry":{"lapCount":0,"lapsInRace":5,"bestLapTimeMs":59000,""" +
                    """"gasLevel":20.0,"gasCapacity":100.0}""",
            ),
        )
        assertEquals(true, logs.single().telemetryJson.contains(""""observedAtMs":200"""))
    }

    @Test
    fun `機能ごとに直前のテレメトリを保持する`() = runTest {
        val logs = mutableListOf<TelemetryLog>()
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(any(), any()) } just Runs
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } answers {
            logs += TelemetryLog(0, firstArg(), secondArg(), thirdArg(), arg(3))
        }
        val processor = createProcessor()
        val myBestLapKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        val fuelKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root

        processor.process(myBestLapKey, telemetry(bestLapTimeMs = 60_000), emptyList(), emptyList(), 0L)
        processor.process(fuelKey, telemetry(bestLapTimeMs = 50_000), emptyList(), emptyList(), 0L)
        processor.process(
            myBestLapKey,
            telemetry(bestLapTimeMs = 59_000),
            listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
            emptyList(),
            0L,
        )

        assertEquals(true, logs.single().telemetryJson.contains("\"bestLapTimeMs\":60000"))
    }

    @Test
    fun `優先度の高い項目を再生中なら読み上げも保存もしない`() = runTest {
        val currentKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        val newEvent = SpeechEvent.RemainingFuelLapsWarning(2)
        every { ttsEngine.currentReadoutItemKey } returns currentKey
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } just Runs
        val processor = createProcessor()

        processor.process(
            sourceKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            telemetry = telemetry(),
            events = listOf(newEvent),
            readoutOrder = listOf(currentKey, newEvent.readoutItemKey),
            observedAtMs = 0L,
        )

        verify(exactly = 0) { ttsEngine.stop() }
        verify(exactly = 0) { ttsEngine.speak(any(), any()) }
        coVerify(exactly = 0) { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) }
    }

    @Test
    fun `優先度の低い項目を再生中なら停止して読み上げる`() = runTest {
        val currentKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
        val newEvent = SpeechEvent.Gt7Ps5MyBestLapFormal
        every { ttsEngine.currentReadoutItemKey } returns currentKey
        every { ttsEngine.stop() } just Runs
        every { ttsEngine.speak(newEvent, any()) } just Runs
        coEvery { telemetryLogRepository.saveTelemetryLog(any(), any(), any(), any()) } just Runs
        val processor = createProcessor()

        processor.process(
            sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
            telemetry = telemetry(),
            events = listOf(newEvent),
            readoutOrder = listOf(newEvent.readoutItemKey, currentKey),
            observedAtMs = 0L,
        )

        verify(exactly = 1) { ttsEngine.stop() }
        verify(exactly = 1) { ttsEngine.speak(newEvent, any()) }
    }

    private fun createProcessor() = Gt7Ps5NarratorEventProcessor(
        ttsEngine = ttsEngine,
        saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
    )

    private fun telemetry(bestLapTimeMs: Int = 60_000) = Gt7Ps5TelemetryData(
        lapCount = 0,
        lapsInRace = 5,
        bestLapTimeMs = bestLapTimeMs,
        gasLevel = 20f,
        gasCapacity = 100f,
    )
}
