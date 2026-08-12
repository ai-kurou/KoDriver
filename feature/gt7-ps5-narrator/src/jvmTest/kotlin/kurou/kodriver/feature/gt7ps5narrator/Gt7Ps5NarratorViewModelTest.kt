@file:Suppress("FunctionNaming", "TooManyFunctions")

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.TelemetryLogRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5TyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveTelemetryLogUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5NarratorViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var telemetryRepository: Gt7Ps5Repository

    @MockK
    private lateinit var myBestLapPreferencesRepository: Gt7Ps5MyBestLapPreferencesRepository

    @MockK
    private lateinit var remainingFuelLapsPreferencesRepository: Gt7Ps5RemainingFuelLapsPreferencesRepository

    @MockK
    private lateinit var remainingFuelPreferencesRepository: Gt7Ps5RemainingFuelPreferencesRepository

    @MockK
    private lateinit var tyreTemperaturePreferencesRepository: Gt7Ps5TyreTemperaturePreferencesRepository

    @MockK
    private lateinit var simulatorPreferencesRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var readoutPreferencesRepository: ReadoutPreferencesRepository

    @MockK
    private lateinit var telemetryLogRepository: TelemetryLogRepository

    @MockK
    private lateinit var queuePreferencesRepository: QueuePreferencesRepository

    @MockK
    private lateinit var ttsEngine: TextToSpeechEngine

    @MockK
    private lateinit var priorityAwareTts: PriorityAwareTts

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        telemetryChannel: Channel<Gt7Ps5TelemetryData>,
        ttsEngine: TextToSpeechEngine,
        currentTimeMs: () -> Long = { 0L },
    ): Gt7Ps5NarratorViewModel {
        every { telemetryRepository.telemetryStream() } returns telemetryChannel.receiveAsFlow()
        return Gt7Ps5NarratorViewModel(
            myBestLapUseCases =
                MyBestLapUseCases(
                    observeGt7Ps5 = ObserveGt7Ps5UseCase(telemetryRepository),
                    observeMyBestLapVoiceType = ObserveGt7Ps5MyBestLapVoiceTypeUseCase(myBestLapPreferencesRepository),
                ),
            readoutListUseCases =
                ReadoutListUseCases(
                    observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorPreferencesRepository),
                    observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutPreferencesRepository),
                    observeReadoutOrder = ObserveReadoutOrderUseCase(readoutPreferencesRepository),
                    observeQueueEnabledStates = ObserveQueueEnabledStatesUseCase(queuePreferencesRepository),
                ),
            remainingFuelLapsUseCases =
                RemainingFuelLapsUseCases(
                    observeRemainingFuelLapsThreshold =
                        ObserveGt7Ps5RemainingFuelLapsUseCase(remainingFuelLapsPreferencesRepository),
                ),
            remainingFuelUseCases =
                RemainingFuelUseCases(
                    observeRemainingFuelThresholdPercentage =
                        ObserveGt7Ps5RemainingFuelThresholdPercentageUseCase(remainingFuelPreferencesRepository),
                ),
            tyreTemperatureUseCases =
                TyreTemperatureUseCases(
                    observeHighThresholdCelsius =
                        ObserveGt7Ps5TyreTemperatureHighThresholdUseCase(tyreTemperaturePreferencesRepository),
                ),
            eventProcessor =
                Gt7Ps5NarratorEventProcessor(
                    ttsEngine = ttsEngine,
                    saveTelemetryLog = SaveTelemetryLogUseCase(telemetryLogRepository),
                ),
            currentTimeMs = currentTimeMs,
        )
    }

    @Test
    fun `GT7非選択時は読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(null)
            every {
                readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(emptyMap())
            every {
                readoutPreferencesRepository.observeReadoutOrder(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(emptyList())
            every { myBestLapPreferencesRepository.observeVoiceType() } returns
                MutableStateFlow(MyBestLapVoiceType.FORMAL)
            every { remainingFuelLapsPreferencesRepository.observeRemainingFuelLaps() } returns MutableStateFlow(3)
            every { remainingFuelPreferencesRepository.observeThresholdPercentage() } returns MutableStateFlow(30)
            every { tyreTemperaturePreferencesRepository.observeHighThresholdCelsius() } returns MutableStateFlow(95)
            every { queuePreferencesRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))
            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            confirmVerified(simulatorPreferencesRepository)
        }

    @Test
    fun `起動直後の最初のemitではベストラップが設定済みでもアナウンスしない`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults()
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `自己ベストラップの声種別設定を反映して読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(voiceType = MyBestLapVoiceType.CASUAL)
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapCasual), spokenTexts)
        }

    @Test
    fun `読み上げが発生したら現在と直前のテレメトリを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    capture(telemetryJsons),
                )
            } just Runs
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine, currentTimeMs = { 123_456L })

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().startsWith("{\"state\":{\"raw\":"))
            assertEquals(
                true,
                telemetryJsons.single().contains(
                    """"previousTelemetry":{"lapCount":0,"lapsInRace":0,"bestLapTimeMs":60000,""" +
                        """"gasLevel":0.0,"gasCapacity":100.0,"carCategory":"",""" +
                        """"tyreTemperature":{"frontLeftCelsius":0.0,"frontRightCelsius":0.0,""" +
                        """"rearLeftCelsius":0.0,"rearRightCelsius":0.0}}""",
                ),
            )
            assertEquals(
                true,
                telemetryJsons.single().contains(
                    """"telemetry":{"lapCount":0,"lapsInRace":0,"bestLapTimeMs":59000,""" +
                        """"gasLevel":0.0,"gasCapacity":100.0,"carCategory":"",""" +
                        """"tyreTemperature":{"frontLeftCelsius":0.0,"frontRightCelsius":0.0,""" +
                        """"rearLeftCelsius":0.0,"rearRightCelsius":0.0}}""",
                ),
            )
            assertEquals(true, telemetryJsons.single().contains(""""settings":{"raw":"""))
            assertEquals(true, telemetryJsons.single().contains(""""observedAtMs":123456"""))
            assertEquals(true, telemetryJsons.single().contains(""""finalState":{"raw":"""))
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `自己ベストラップが無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(enabledOverrides = mapOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false))
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `燃料残り周回数の閾値設定を反映して読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(fuelThreshold = 3)
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 100f))
            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f))
            channel.send(gt7Telemetry(lapCount = 2, gasLevel = 20f, gasCapacity = 100f))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.RemainingFuelLapsWarning(2)), spokenTexts)
        }

    @Test
    fun `給油やラップ数戻り後は燃料残り周回数を再度読み上げる`() =
        runTest(testDispatcher) {
            suspend fun runRemainingFuelLapsSequence(
                spokenTexts: MutableList<SpeechEvent>,
                telemetry: List<Gt7Ps5TelemetryData>,
            ) {
                val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
                val ttsEngine = mockTts(spokenTexts)
                val currentTimeMsQueue = mutableListOf(0L, 100_000L, 160_000L, 200_000L, 300_000L, 360_000L)
                var currentTimeMsCallCount = 0
                stubReadoutDefaults(fuelThreshold = 3)
                createViewModel(
                    telemetryChannel = channel,
                    ttsEngine = ttsEngine,
                    currentTimeMs = {
                        // myBestLapJob/remainingFuelLapsJob/remainingFuelJob/tyreTemperatureJobがテレメトリ1回につき
                        // それぞれ1回ずつ呼ぶため、4回ごとに1つのタイムスタンプへ進める。
                        currentTimeMsQueue[currentTimeMsCallCount / 4].also { currentTimeMsCallCount++ }
                    },
                )
                runCurrent()

                telemetry.forEach { channel.send(it) }
                runCurrent()
            }

            val refuelSpokenTexts = mutableListOf<SpeechEvent>()
            runRemainingFuelLapsSequence(
                spokenTexts = refuelSpokenTexts,
                telemetry =
                    listOf(
                        gt7Telemetry(lapCount = 1, gasLevel = 100f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 2, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 2, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 3, gasLevel = 80f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 4, gasLevel = 20f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 4, gasLevel = 20f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                    ),
            )
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.RemainingFuelLapsWarning(0),
                    SpeechEvent.RemainingFuelLapsWarning(0),
                ),
                refuelSpokenTexts,
            )

            val lapResetSpokenTexts = mutableListOf<SpeechEvent>()
            runRemainingFuelLapsSequence(
                spokenTexts = lapResetSpokenTexts,
                telemetry =
                    listOf(
                        gt7Telemetry(lapCount = 1, gasLevel = 100f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 2, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 2, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 1, gasLevel = 100f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 2, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                        gt7Telemetry(lapCount = 2, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000),
                    ),
            )
            assertEquals(
                listOf<SpeechEvent>(
                    SpeechEvent.RemainingFuelLapsWarning(0),
                    SpeechEvent.RemainingFuelLapsWarning(0),
                ),
                lapResetSpokenTexts,
            )
        }

    @Test
    fun `燃料残り周回数が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                enabledOverrides = mapOf(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to false),
                fuelThreshold = 3,
            )
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(lapCount = 0, gasLevel = 30f, gasCapacity = 100f))
            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 20f, gasCapacity = 100f))
            channel.send(gt7Telemetry(lapCount = 2, gasLevel = 10f, gasCapacity = 100f))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `燃料残量の閾値設定を反映して読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(remainingFuelThresholdPercentage = 30)
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 31f, gasCapacity = 100f))
            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f))
            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 20f, gasCapacity = 100f))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5RemainingFuelWarning), spokenTexts)
        }

    @Test
    fun `燃料残量が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                enabledOverrides = mapOf(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to false),
                remainingFuelThresholdPercentage = 30,
            )
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 20f, gasCapacity = 100f))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `燃料残量の読み上げが発生したら現在と直前のテレメトリを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(remainingFuelThresholdPercentage = 30)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
                    capture(telemetryJsons),
                )
            } just Runs
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine, currentTimeMs = { 123_456L })

            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 31f, gasCapacity = 100f))
            channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5RemainingFuelWarning), spokenTexts)
            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().contains("remainingFuelWarned=false"))
            assertEquals(true, telemetryJsons.single().contains("remainingFuelWarned=true"))
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
                    telemetryJsons.single(),
                )
            }
        }

    @Test
    fun `タイヤ温度の高温閾値設定を反映して読み上げる`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(tyreTemperatureHighThresholdCelsius = 95)
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(tyreTemperature = Gt7Ps5TyreTemperatureData(90f, 90f, 90f, 90f)))
            channel.send(gt7Telemetry(tyreTemperature = Gt7Ps5TyreTemperatureData(95f, 90f, 90f, 90f)))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5TyreOverheat), spokenTexts)
        }

    @Test
    fun `タイヤ温度が無効のときは読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(
                enabledOverrides = mapOf(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root to false),
                tyreTemperatureHighThresholdCelsius = 95,
            )
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(tyreTemperature = Gt7Ps5TyreTemperatureData(90f, 90f, 90f, 90f)))
            channel.send(gt7Telemetry(tyreTemperature = Gt7Ps5TyreTemperatureData(95f, 90f, 90f, 90f)))

            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `タイヤ温度の読み上げが発生したら現在と直前のテレメトリを保存する`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val telemetryJsons = mutableListOf<String>()
            val ttsEngine = mockTts(spokenTexts)
            stubReadoutDefaults(tyreTemperatureHighThresholdCelsius = 95)
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.TyreTemperature.Root,
                    capture(telemetryJsons),
                )
            } just Runs
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine, currentTimeMs = { 123_456L })

            channel.send(gt7Telemetry(tyreTemperature = Gt7Ps5TyreTemperatureData(90f, 90f, 90f, 90f)))
            channel.send(gt7Telemetry(tyreTemperature = Gt7Ps5TyreTemperatureData(95f, 90f, 90f, 90f)))

            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5TyreOverheat), spokenTexts)
            assertEquals(1, telemetryJsons.size)
            assertEquals(true, telemetryJsons.single().contains("tyreOverheating=false"))
            assertEquals(true, telemetryJsons.single().contains("tyreOverheating=true"))
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    123_456L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.TyreTemperature.Root,
                    telemetryJsons.single(),
                )
            }
        }

    @Test
    fun `優先度の高いアイテム読み上げ中にベストラップが来ても読み上げない`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
                )
            stubReadoutDefaults(
                orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root, ReadoutItemKey.Gt7Ps5.MyBestLap.Root),
            )
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            assertEquals(false, ttsEngine.stopCalled)
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
            verify(exactly = 0) { ttsEngine.stop() }
            verify(exactly = 1) { ttsEngine.currentReadoutItemKey }
            verify(exactly = 1) { ttsEngine.stopCalled }
            confirmVerified(ttsEngine)
        }

    @Test
    fun `優先度制御で読み上げなかったイベントは保存しない`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
                )
            stubReadoutDefaults(
                orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root, ReadoutItemKey.Gt7Ps5.MyBestLap.Root),
            )
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            confirmVerified(telemetryLogRepository)
        }

    @Test
    fun `優先度の低いアイテム読み上げ中にベストラップが来ると割り込む`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
                )
            stubReadoutDefaults(
                orderOverride = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, ReadoutItemKey.LmuWindows.Flag.Root),
            )
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            assertEquals(true, ttsEngine.stopCalled)
            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapFormal), spokenTexts)
        }

    @Test
    fun `再生中の項目が優先度リストにないときは新しい読み上げで割り込む`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
                )
            stubReadoutDefaults(orderOverride = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root))
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            assertEquals(true, ttsEngine.stopCalled)
            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapFormal), spokenTexts)
        }

    @Test
    fun `キュー設定が有効なら優先度で本来無視される項目もキュー再生する`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
                )
            stubReadoutDefaults(
                orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root, ReadoutItemKey.Gt7Ps5.MyBestLap.Root),
                queueEnabledOverrides = mapOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true),
            )
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            assertEquals(false, ttsEngine.stopCalled)
            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapFormal), spokenTexts)
        }

    @Test
    fun `新しい項目が優先度リストにないときは再生中の読み上げを優先する`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine =
                mockPriorityAwareTts(
                    spokenTexts = spokenTexts,
                    initialKey = ReadoutItemKey.LmuWindows.Flag.Root,
                )
            stubReadoutDefaults(orderOverride = listOf(ReadoutItemKey.LmuWindows.Flag.Root))
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            assertEquals(false, ttsEngine.stopCalled)
            assertEquals(emptyList<SpeechEvent>(), spokenTexts)
        }

    @Test
    fun `設定読み込み前にテレメトリが届いても例外にならずデフォルト値で判定する`() =
        runTest(testDispatcher) {
            val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
            val spokenTexts = mutableListOf<SpeechEvent>()
            val ttsEngine = mockTts(spokenTexts)
            every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(Simulator.Gt7Ps5)
            // enabledStatesはまだ何も流れてこない（DataStoreの初回読み込み中を模す）状態にする。
            every {
                readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            } returns MutableSharedFlow()
            every {
                readoutPreferencesRepository.observeReadoutOrder(Simulator.Gt7Ps5.id)
            } returns MutableStateFlow(listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root))
            every { myBestLapPreferencesRepository.observeVoiceType() } returns
                MutableStateFlow(MyBestLapVoiceType.FORMAL)
            every { remainingFuelLapsPreferencesRepository.observeRemainingFuelLaps() } returns MutableStateFlow(3)
            every { remainingFuelPreferencesRepository.observeThresholdPercentage() } returns MutableStateFlow(0)
            every { tyreTemperaturePreferencesRepository.observeHighThresholdCelsius() } returns MutableStateFlow(95)
            every { queuePreferencesRepository.observeQueueEnabledStates() } returns MutableStateFlow(emptyMap())
            val telemetryJsons = mutableListOf<String>()
            coEvery {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    capture(telemetryJsons),
                )
            } just Runs
            createViewModel(telemetryChannel = channel, ttsEngine = ttsEngine)

            channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
            channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

            // Gt7Ps5.MyBestLap.RootのREADOUT_ENABLED_STATE_DEFAULTはtrueのため、未読み込みでも読み上げられる。
            assertEquals(listOf<SpeechEvent>(SpeechEvent.Gt7Ps5MyBestLapFormal), spokenTexts)
            verify(exactly = 1) { simulatorPreferencesRepository.selectedSimulator() }
            verify(exactly = 1) {
                readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            }
            verify(exactly = 1) {
                readoutPreferencesRepository.observeReadoutOrder(Simulator.Gt7Ps5.id)
            }
            verify(exactly = 1) { myBestLapPreferencesRepository.observeVoiceType() }
            verify(exactly = 1) { remainingFuelLapsPreferencesRepository.observeRemainingFuelLaps() }
            verify(exactly = 1) { remainingFuelPreferencesRepository.observeThresholdPercentage() }
            verify(exactly = 1) { tyreTemperaturePreferencesRepository.observeHighThresholdCelsius() }
            verify(exactly = 1) { queuePreferencesRepository.observeQueueEnabledStates() }
            coVerify(exactly = 1) {
                telemetryLogRepository.saveTelemetryLog(
                    0L,
                    Simulator.Gt7Ps5,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    telemetryJsons.single(),
                )
            }
            confirmVerified(
                simulatorPreferencesRepository,
                readoutPreferencesRepository,
                myBestLapPreferencesRepository,
                remainingFuelLapsPreferencesRepository,
                remainingFuelPreferencesRepository,
                tyreTemperaturePreferencesRepository,
                queuePreferencesRepository,
                telemetryLogRepository,
            )
        }

    /**
     * simulator/enabledStates/readoutOrder/voiceType/fuelThresholdの標準スタブをまとめて設定する。
     * ViewModelがコンストラクタ内で即座にFlowを購読・combineするため、必ず [createViewModel] の前に呼ぶこと。
     */
    @Suppress("LongParameterList")
    private fun stubReadoutDefaults(
        simulator: Simulator? = Simulator.Gt7Ps5,
        enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root),
        voiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        fuelThreshold: Int = 3,
        remainingFuelThresholdPercentage: Int = 0,
        tyreTemperatureHighThresholdCelsius: Int = 95,
        queueEnabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    ) {
        every { simulatorPreferencesRepository.selectedSimulator() } returns MutableStateFlow(simulator)
        every {
            readoutPreferencesRepository.observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
        } returns MutableStateFlow(enabledOverrides)
        every {
            readoutPreferencesRepository.observeReadoutOrder(Simulator.Gt7Ps5.id)
        } returns MutableStateFlow(orderOverride)
        every { myBestLapPreferencesRepository.observeVoiceType() } returns MutableStateFlow(voiceType)
        every {
            remainingFuelLapsPreferencesRepository.observeRemainingFuelLaps()
        } returns MutableStateFlow(fuelThreshold)
        every {
            remainingFuelPreferencesRepository.observeThresholdPercentage()
        } returns MutableStateFlow(remainingFuelThresholdPercentage)
        every {
            tyreTemperaturePreferencesRepository.observeHighThresholdCelsius()
        } returns MutableStateFlow(tyreTemperatureHighThresholdCelsius)
        every {
            queuePreferencesRepository.observeQueueEnabledStates()
        } returns MutableStateFlow(queueEnabledOverrides)
        val telemetryJsons = mutableListOf<String>()
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                any(),
                Simulator.Gt7Ps5,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                capture(telemetryJsons),
            )
        } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                any(),
                Simulator.Gt7Ps5,
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                capture(telemetryJsons),
            )
        } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                any(),
                Simulator.Gt7Ps5,
                ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
                capture(telemetryJsons),
            )
        } just Runs
        coEvery {
            telemetryLogRepository.saveTelemetryLog(
                any(),
                Simulator.Gt7Ps5,
                ReadoutItemKey.Gt7Ps5.TyreTemperature.Root,
                capture(telemetryJsons),
            )
        } just Runs
    }

    private fun mockTts(spokenTexts: MutableList<SpeechEvent>): TextToSpeechEngine {
        every { ttsEngine.currentReadoutItemKey } returns null
        every { ttsEngine.speak(capture(spokenTexts), capture(mutableListOf<Boolean>())) } just Runs
        every { ttsEngine.stop() } just Runs
        return ttsEngine
    }

    private fun mockPriorityAwareTts(
        spokenTexts: MutableList<SpeechEvent>,
        initialKey: ReadoutItemKey?,
    ): PriorityAwareTts {
        var currentKey = initialKey
        var stopCalled = false
        every { priorityAwareTts.currentReadoutItemKey } answers { currentKey }
        every { priorityAwareTts.speak(capture(spokenTexts), capture(mutableListOf<Boolean>())) } just Runs
        every { priorityAwareTts.stop() } answers {
            stopCalled = true
            currentKey = null
        }
        every { priorityAwareTts.stopCalled } answers { stopCalled }
        return priorityAwareTts
    }

    private interface PriorityAwareTts : TextToSpeechEngine {
        val stopCalled: Boolean
    }
}

private fun gt7Telemetry(bestLapTimeMs: Int) =
    Gt7Ps5TelemetryData(
        lapCount = 0,
        lapsInRace = 0,
        bestLapTimeMs = bestLapTimeMs,
        gasLevel = 0f,
        gasCapacity = 100f,
    )

private fun gt7Telemetry(
    lapCount: Int,
    gasLevel: Float,
    gasCapacity: Float,
    bestLapTimeMs: Int = 30_000,
) = Gt7Ps5TelemetryData(
    lapCount = lapCount,
    lapsInRace = 5,
    bestLapTimeMs = bestLapTimeMs,
    gasLevel = gasLevel,
    gasCapacity = gasCapacity,
)

private fun gt7Telemetry(tyreTemperature: Gt7Ps5TyreTemperatureData) =
    Gt7Ps5TelemetryData(
        lapCount = 0,
        lapsInRace = 0,
        bestLapTimeMs = 30_000,
        gasLevel = 0f,
        gasCapacity = 100f,
        tyreTemperature = tyreTemperature,
    )
