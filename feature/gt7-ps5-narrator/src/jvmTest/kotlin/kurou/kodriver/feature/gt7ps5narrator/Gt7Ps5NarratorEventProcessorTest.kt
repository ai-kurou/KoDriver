package kurou.kodriver.feature.gt7ps5narrator

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5NarratorEventProcessorTest {

    @MockK
    private lateinit var telemetryLogRepository: TelemetryLogRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `直前のテレメトリがないイベントはnullとして保存する`() = runTest {
        val telemetryJsons = mutableListOf<String>()
        every { ttsEngine.currentReadoutItemKey } returns null
        val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.Gt7Ps5.id,
                sourceKey.value,
                capture(telemetryJsons),
            )
        } just Runs

        createProcessor().process(
            sourceKey = sourceKey,
            telemetry = telemetry(),
            events = listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
            readoutOrder = listOf(sourceKey),
            queueEnabledStates = emptyMap(),
            observedAtMs = 0L,
        )

        assertEquals(true, telemetryJsons.single().startsWith("{\"state\":{\"raw\":"))
        assertEquals(true, telemetryJsons.single().contains("\"previousTelemetry\":null"))
        verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) }
        coVerify(exactly = 1) {
            telemetryLogRepository.saveTelemetryLog(0L, Simulator.Gt7Ps5.id, sourceKey.value, telemetryJsons.single())
        }
        confirmVerified(telemetryLogRepository, ttsEngine)
    }

    @Test
    fun `読み上げたイベントを直前と現在のテレメトリとともに保存する`() = runTest {
        val telemetryJsons = mutableListOf<String>()
        every { ttsEngine.currentReadoutItemKey } returns null
        val processor = createProcessor()
        val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                200L,
                Simulator.Gt7Ps5.id,
                sourceKey.value,
                capture(telemetryJsons),
            )
        } just Runs

        processor.process(sourceKey, telemetry(bestLapTimeMs = 60_000), emptyList(), emptyList(), emptyMap(), 100L)
        processor.process(
            sourceKey,
            telemetry(bestLapTimeMs = 59_000),
            listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
            listOf(sourceKey),
            emptyMap(),
            200L,
        )

        assertEquals(1, telemetryJsons.size)
        assertEquals(
            true,
            telemetryJsons.single().contains(
                """"previousTelemetry":{"lapCount":0,"lapsInRace":5,"bestLapTimeMs":60000,""" +
                    """"gasLevel":20.0,"gasCapacity":100.0}""",
            ),
        )
        assertEquals(
            true,
            telemetryJsons.single().contains(
                """"telemetry":{"lapCount":0,"lapsInRace":5,"bestLapTimeMs":59000,""" +
                    """"gasLevel":20.0,"gasCapacity":100.0}""",
            ),
        )
        assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":200"""))
        verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) }
        coVerify(exactly = 1) {
            telemetryLogRepository.saveTelemetryLog(200L, Simulator.Gt7Ps5.id, sourceKey.value, telemetryJsons.single())
        }
        confirmVerified(telemetryLogRepository, ttsEngine)
    }

    @Test
    fun `機能ごとに直前のテレメトリを保持する`() = runTest {
        val telemetryJsons = mutableListOf<String>()
        every { ttsEngine.currentReadoutItemKey } returns null
        val processor = createProcessor()
        val myBestLapKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        val fuelKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
        every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.Gt7Ps5.id,
                myBestLapKey.value,
                capture(telemetryJsons),
            )
        } just Runs

        processor.process(myBestLapKey, telemetry(bestLapTimeMs = 60_000), emptyList(), emptyList(), emptyMap(), 0L)
        processor.process(fuelKey, telemetry(bestLapTimeMs = 50_000), emptyList(), emptyList(), emptyMap(), 0L)
        processor.process(
            myBestLapKey,
            telemetry(bestLapTimeMs = 59_000),
            listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
            emptyList(),
            emptyMap(),
            0L,
        )

        assertEquals(true, telemetryJsons.single().contains("\"bestLapTimeMs\":60000"))
        verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
        verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) }
        coVerify(exactly = 1) {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.Gt7Ps5.id,
                myBestLapKey.value,
                telemetryJsons.single(),
            )
        }
        confirmVerified(telemetryLogRepository, ttsEngine)
    }

    @Test
    fun `優先度の高い項目を再生中なら読み上げも保存もしない`() = runTest {
        val currentKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        val newEvent = SpeechEvent.RemainingFuelLapsWarning(2)
        every { ttsEngine.currentReadoutItemKey } returns currentKey
        val processor = createProcessor()

        processor.process(
            sourceKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            telemetry = telemetry(),
            events = listOf(newEvent),
            readoutOrder = listOf(currentKey, newEvent.readoutItemKey),
            queueEnabledStates = emptyMap(),
            observedAtMs = 0L,
        )

        verify(exactly = 0) { ttsEngine.stop() }
        verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
        confirmVerified(telemetryLogRepository, ttsEngine)
    }

    @Test
    fun `優先度で本来無視される項目でもキュー設定が有効ならキュー再生する`() = runTest {
        val currentKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
        val newEvent = SpeechEvent.RemainingFuelLapsWarning(2)
        val telemetryJsons = mutableListOf<String>()
        every { ttsEngine.speak(newEvent, queue = true) } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.Gt7Ps5.id,
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root.value,
                capture(telemetryJsons),
            )
        } just Runs
        val processor = createProcessor()

        processor.process(
            sourceKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            telemetry = telemetry(),
            events = listOf(newEvent),
            readoutOrder = listOf(currentKey, newEvent.readoutItemKey),
            queueEnabledStates = mapOf(newEvent.readoutItemKey to true),
            observedAtMs = 0L,
        )

        verify(exactly = 0) { ttsEngine.stop() }
        verify(exactly = 1) { ttsEngine.speak(newEvent, queue = true) }
        coVerify(exactly = 1) {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.Gt7Ps5.id,
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root.value,
                telemetryJsons.single(),
            )
        }
        confirmVerified(telemetryLogRepository, ttsEngine)
    }

    @Test
    fun `優先度の低い項目を再生中なら停止して読み上げる`() = runTest {
        val currentKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
        val newEvent = SpeechEvent.Gt7Ps5MyBestLapFormal
        val telemetryJsons = mutableListOf<String>()
        every { ttsEngine.currentReadoutItemKey } returns currentKey
        every { ttsEngine.stop() } just Runs
        every { ttsEngine.speak(newEvent, false) } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.Gt7Ps5.id,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root.value,
                capture(telemetryJsons),
            )
        } just Runs
        val processor = createProcessor()

        processor.process(
            sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
            telemetry = telemetry(),
            events = listOf(newEvent),
            readoutOrder = listOf(newEvent.readoutItemKey, currentKey),
            queueEnabledStates = emptyMap(),
            observedAtMs = 0L,
        )

        verify(exactly = 1) { ttsEngine.stop() }
        verify(exactly = 1) { ttsEngine.speak(newEvent, false) }
        verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
        coVerify(exactly = 1) {
            telemetryLogRepository.saveTelemetryLog(
                0L,
                Simulator.Gt7Ps5.id,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root.value,
                telemetryJsons.single(),
            )
        }
        confirmVerified(telemetryLogRepository, ttsEngine)
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
