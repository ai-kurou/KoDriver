package kurou.kodriver.feature.gt7ps5narrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5NarratorEventProcessorTest {
    private val telemetryLogRepository: TelemetryLogRepository = mockk()

    private val ttsEngine: TextToSpeechEngine = mockk()

    @Test
    fun `直前のテレメトリがないイベントはnullとして保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
            every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
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
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `読み上げたイベントを直前と現在のテレメトリとともに保存する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
            every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
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
                        """"gasLevel":20.0,"gasCapacity":100.0,"carCategory":"",""" +
                        """"tyreTemperature":{"frontLeftCelsius":0.0,"frontRightCelsius":0.0,""" +
                        """"rearLeftCelsius":0.0,"rearRightCelsius":0.0}}""",
                ),
            )
            assertEquals(
                true,
                telemetryJsons.single().contains(
                    """"telemetry":{"lapCount":0,"lapsInRace":5,"bestLapTimeMs":59000,""" +
                        """"gasLevel":20.0,"gasCapacity":100.0,"carCategory":"",""" +
                        """"tyreTemperature":{"frontLeftCelsius":0.0,"frontRightCelsius":0.0,""" +
                        """"rearLeftCelsius":0.0,"rearRightCelsius":0.0}},"settings":""",
                ),
            )
            assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":200"""))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    200L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `テレメトリログの保存に失敗しても例外を投げない`() =
        runTest {
            val observedAtMs = 0L
            val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
            val narratedText = "自己ベストラップ更新"
            val telemetryJsonSlot = slot<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    observedAtMs,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    narratedText,
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsonSlot),
                )
            } throws RuntimeException("db error")

            createProcessor().process(
                sourceKey = sourceKey,
                telemetry = telemetry(),
                events = listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
                readoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = observedAtMs,
            )

            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    observedAtMs,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    narratedText,
                    NarrationOutcome.SPOKEN,
                    telemetryJsonSlot.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `テレメトリがNaNでも保存に失敗しない`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
            every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    capture(telemetryJsons),
                )
            } just Runs

            createProcessor().process(
                sourceKey = sourceKey,
                telemetry = telemetry(gasLevel = Float.NaN),
                events = listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
                readoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 0L,
            )

            assertEquals(true, telemetryJsons.single().contains("\"gasLevel\":NaN"))
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    sourceKey,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `機能ごとに直前のテレメトリを保持する`() =
        runTest {
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns null
            val processor = createProcessor()
            val myBestLapKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
            val fuelKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
            every { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    myBestLapKey,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
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
                    Simulator.Gt7Ps5,
                    myBestLapKey,
                    "自己ベストラップ更新",
                    NarrationOutcome.SPOKEN,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の高い項目を再生中なら読み上げずSKIPPEDとして保存する`() =
        runTest {
            val json = slot<String>()
            val currentKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
            val newEvent = SpeechEvent.RemainingFuelLapsWarning(2)
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    newEvent.readoutItemKey,
                    newEvent.narratedText,
                    NarrationOutcome.SKIPPED,
                    capture(json),
                )
            } just Runs
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
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    newEvent.readoutItemKey,
                    newEvent.narratedText,
                    NarrationOutcome.SKIPPED,
                    json.captured,
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度で本来無視される項目でもキュー設定が有効ならキュー再生する`() =
        runTest {
            val currentKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
            val newEvent = SpeechEvent.RemainingFuelLapsWarning(2)
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.speak(newEvent.copy(resolvedText = newEvent.narratedText), queue = true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                    "燃料は残り約2周",
                    NarrationOutcome.QUEUED,
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
            verify(exactly = 1) { ttsEngine.speak(newEvent.copy(resolvedText = newEvent.narratedText), queue = true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                    "燃料は残り約2周",
                    NarrationOutcome.QUEUED,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `優先度の低い項目を再生中なら停止して読み上げる`() =
        runTest {
            val currentKey = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
            val newEvent = SpeechEvent.Gt7Ps5MyBestLapFormal
            val telemetryJsons = mutableListOf<String>()
            every { ttsEngine.currentReadoutItemKey } returns currentKey
            every { ttsEngine.stop() } just Runs
            every { ttsEngine.speak(newEvent, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    "自己ベストラップ更新",
                    NarrationOutcome.INTERRUPTED,
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
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    "自己ベストラップ更新",
                    NarrationOutcome.INTERRUPTED,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `ログ保存に失敗しても次の読み上げは継続する`() =
        runTest {
            val spokenEvents = mutableListOf<SpeechEvent>()
            var saveCount = 0
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(capture(spokenEvents), queue = false) } just Runs
            val sourceKey = ReadoutItemKey.Gt7Ps5.MyBestLap.Root
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 100L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = sourceKey,
                    narratedText = "自己ベストラップ更新",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(slot<String>()),
                )
            } answers {
                saveCount += 1
                error("failed")
            }
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = sourceKey,
                    narratedText = "自己ベストラップ更新",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(slot<String>()),
                )
            } answers { saveCount += 1 }
            val processor = createProcessor()

            processor.process(
                sourceKey = sourceKey,
                telemetry = telemetry(bestLapTimeMs = 60_000),
                events = listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
                readoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 100L,
            )
            processor.process(
                sourceKey = sourceKey,
                telemetry = telemetry(bestLapTimeMs = 59_000),
                events = listOf(SpeechEvent.Gt7Ps5MyBestLapFormal),
                readoutOrder = listOf(sourceKey),
                queueEnabledStates = emptyMap(),
                observedAtMs = 200L,
            )

            assertEquals(
                listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapFormal, SpeechEvent.Gt7Ps5MyBestLapFormal),
                spokenEvents,
            )
            assertEquals(2, saveCount)
            verify(exactly = 2) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 2) { ttsEngine.speak(SpeechEvent.Gt7Ps5MyBestLapFormal, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 100L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = sourceKey,
                    narratedText = "自己ベストラップ更新",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(slot<String>()),
                )
            }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    createdAt = 200L,
                    simulator = Simulator.Gt7Ps5,
                    readoutItemKey = sourceKey,
                    narratedText = "自己ベストラップ更新",
                    narrationOutcome = NarrationOutcome.SPOKEN,
                    telemetryJson = capture(slot<String>()),
                )
            }
            confirmVerified(telemetryLogRepository, ttsEngine)
        }

    @Test
    fun `燃料残り周回数は解決文言をイベントとログに保持してキューへ渡す`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
            val event = SpeechEvent.RemainingFuelLapsWarning(2)
            val resolved = event.copy(resolvedText = "あと2周")
            every { ttsEngine.speak(resolved, true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "あと2周",
                    NarrationOutcome.QUEUED,
                    capture(jsons),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) {
                    resolvedEvents += it
                    "あと2周"
                }

            processor.process(
                sourceKey = key,
                telemetry = telemetry(),
                events = listOf(event),
                readoutOrder = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 10L,
            )

            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(resolved, true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "あと2周",
                    NarrationOutcome.QUEUED,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `文言がnullの燃料残り周回数は読み上げず空文言とSKIPPEDを保存する`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
            val event = SpeechEvent.RemainingFuelLapsWarning(0)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(jsons),
                )
            } just Runs
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) { null }

            processor.process(key, telemetry(), listOf(event), listOf(key), emptyMap(), 10L)

            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `燃料なし文言も解決して通常再生とログに渡す`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root
            val event = SpeechEvent.RemainingFuelLapsWarning(0)
            val resolved = event.copy(resolvedText = "燃料切れです")
            every { ttsEngine.currentReadoutItemKey } returns null
            every { ttsEngine.speak(resolved, false) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "燃料切れです",
                    NarrationOutcome.SPOKEN,
                    capture(jsons),
                )
            } just Runs
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) {
                    assertEquals(event, it)
                    "燃料切れです"
                }

            processor.process(key, telemetry(), listOf(event), listOf(key), emptyMap(), 10L)

            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(resolved, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "燃料切れです",
                    NarrationOutcome.SPOKEN,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `燃料残量は解決文言をイベントとログに保持してキューへ渡す`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = ReadoutItemKey.Gt7Ps5.RemainingFuel.Root
            val event = SpeechEvent.Gt7Ps5RemainingFuelWarning(30)
            val resolved = event.copy(resolvedText = "残り30%")
            every { ttsEngine.speak(resolved, true) } just Runs
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "残り30%",
                    NarrationOutcome.QUEUED,
                    capture(jsons),
                )
            } just Runs
            val resolvedEvents = mutableListOf<SpeechEvent>()
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) {
                    resolvedEvents += it
                    "残り30%"
                }

            processor.process(
                sourceKey = key,
                telemetry = telemetry(),
                events = listOf(event),
                readoutOrder = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, key),
                queueEnabledStates = mapOf(key to true),
                observedAtMs = 10L,
            )

            assertEquals(listOf<SpeechEvent>(event), resolvedEvents)
            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.speak(resolved, true) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "残り30%",
                    NarrationOutcome.QUEUED,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    @Test
    fun `文言がnullの燃料残量は読み上げず空文言とSKIPPEDを保存する`() =
        runTest {
            val jsons = mutableListOf<String>()
            val key = ReadoutItemKey.Gt7Ps5.RemainingFuel.Root
            val event = SpeechEvent.Gt7Ps5RemainingFuelWarning(30)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    capture(jsons),
                )
            } just Runs
            val processor =
                Gt7Ps5NarratorEventProcessor(ttsEngine, SaveTelemetryLogUseCase(telemetryLogRepository)) { null }

            processor.process(key, telemetry(), listOf(event), listOf(key), emptyMap(), 10L)

            verify(exactly = 0) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 0) { ttsEngine.speak(event, false) }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    10L,
                    Simulator.Gt7Ps5,
                    key,
                    "",
                    NarrationOutcome.SKIPPED,
                    jsons.single(),
                )
            }
            confirmVerified(ttsEngine, telemetryLogRepository)
        }

    private fun createProcessor() =
        Gt7Ps5NarratorEventProcessor(
            ttsEngine = ttsEngine,
            saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
            readoutText = {
                check(it is SpeechEvent.RemainingFuelLapsWarning) { "燃料残り周回数以外は文言を解決しない" }
                it.narratedText
            },
        )

    private fun telemetry(
        bestLapTimeMs: Int = 60_000,
        gasLevel: Float = 20f,
    ) = Gt7Ps5TelemetryData(
        lapCount = 0,
        lapsInRace = 5,
        bestLapTimeMs = bestLapTimeMs,
        gasLevel = Gt7Ps5FuelUnit(gasLevel),
        gasCapacity = Gt7Ps5FuelUnit(100f),
    )
}
