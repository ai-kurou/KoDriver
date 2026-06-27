@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kurou.kodriver.domain.repository.MyBestLapPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5RemainingFuelLapsUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveMyBestLapVoiceTypeUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
class Gt7Ps5NarratorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private data class ReadoutSettings(
        val enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
        val remainingFuelLapsEnabled: Boolean = true,
        val fuelThreshold: Int = 3,
    )

    private fun buildViewModel(
        telemetryChannel: Channel<Gt7Ps5TelemetryData> = Channel(Channel.UNLIMITED),
        ttsEngine: TextToSpeechEngine,
        readoutSettings: ReadoutSettings = ReadoutSettings(),
        orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.MyBestLap),
        voiceType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
        simulator: Simulator? = Simulator.Gt7Ps5,
        currentTimeMs: () -> Long = { 0L },
    ): Gt7Ps5NarratorViewModel {
        val readoutRepo = FakeReadoutPreferencesRepo(readoutSettings.enabledOverrides, orderOverride)
        return Gt7Ps5NarratorViewModel(
            myBestLapUseCases = MyBestLapUseCases(
                observeGt7Ps5 = ObserveGt7Ps5UseCase(
                    FakeChannelGt7Ps5Repository(telemetryChannel.receiveAsFlow()),
                ),
                observeMyBestLapVoiceType = ObserveMyBestLapVoiceTypeUseCase(
                    FakeMyBestLapPreferencesRepo(voiceType),
                ),
            ),
            readoutListUseCases = ReadoutListUseCases(
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(
                    FakeSimulatorPreferencesRepo(simulator),
                ),
                observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepo),
                observeReadoutOrder = ObserveReadoutOrderUseCase(readoutRepo),
            ),
            remainingFuelLapsUseCases = RemainingFuelLapsUseCases(
                observeRemainingFuelLapsThreshold = ObserveGt7Ps5RemainingFuelLapsUseCase(
                    FakeRemainingFuelLapsPreferencesRepo(readoutSettings.fuelThreshold),
                ),
                observeRemainingFuelLapsEnabled = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(
                    FakeGt7Ps5RemainingFuelLapsEnabledRepo(readoutSettings.remainingFuelLapsEnabled),
                ),
            ),
            ttsEngine = ttsEngine,
            currentTimeMs = currentTimeMs,
        )
    }

    // --- シミュレータ選択 ---

    @Test
    fun `GT7非選択時はベストラップアナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, simulator = null)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `simulator が null のときはベストラップアナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, simulator = null)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    // --- 初期状態 ---

    @Test
    fun `起動直後の最初のemitではベストラップが設定済みでもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    // --- ベストラップ更新 ---

    @Test
    fun `ベストラップが更新されるとFormalボイスでアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, voiceType = MyBestLapVoiceType.FORMAL)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }

    @Test
    fun `ベストラップが更新されるとCasualボイスでアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts, voiceType = MyBestLapVoiceType.CASUAL)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapCasual), tts.spokenTexts)
    }

    @Test
    fun `ベストラップ未計測から初計測でもアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }

    @Test
    fun `同じベストラップ値が続いてもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ベストラップより遅いラップではアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 61_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ベストラップが無効のときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(enabledOverrides = mapOf(ReadoutItemKey.MyBestLap to false)),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ベストラップが正値から-1にリセットされてもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = -1))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ベストラップを複数回更新すると都度アナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 58_000))

        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal, SpeechEvent.MyBestLapFormal),
            tts.spokenTexts,
        )
    }

    @Test
    fun `セッションリセット後に同じベストラップが来てもアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))

        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }

    @Test
    fun `セッションリセット後により良いタイムが来たらアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(telemetryChannel = channel, ttsEngine = tts)

        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = -1))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal, SpeechEvent.MyBestLapFormal),
            tts.spokenTexts,
        )
    }

    // --- 燃料残り周回数 ---

    @Test
    fun `GT7非選択時は燃料アナウンスをしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            simulator = null,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 2),
        )

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 60f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 40f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `Slider3のとき残り3周から1周まで合計3回アナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 3),
        )

        // スタート: gasLevel=40L、1周10L消費
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 100f))
        // lap1完了 → 残り30/10=3.0周 → アナウンス(3)
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f))
        // lap2完了 → 残り20/10=2.0周 → アナウンス(2)
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 20f, gasCapacity = 100f))
        // lap3完了 → 残り10/10=1.0周 → アナウンス(1)
        channel.send(gt7Telemetry(lapCount = 3, gasLevel = 10f, gasCapacity = 100f))

        assertEquals(
            listOf<SpeechEvent>(
                SpeechEvent.RemainingFuelLapsWarning(3),
                SpeechEvent.RemainingFuelLapsWarning(2),
                SpeechEvent.RemainingFuelLapsWarning(1),
            ),
            tts.spokenTexts,
        )
    }

    @Test
    fun `最速ラップの30秒前に到達するまでは燃料残り周回数をアナウンスしない`() = runTest(testDispatcher) {
        var currentTimeMs = 0L
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 3),
            currentTimeMs = { currentTimeMs },
        )

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 100f, bestLapTimeMs = 90_000))
        currentTimeMs = 10_000L
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000))
        currentTimeMs = 69_999L
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)

        currentTimeMs = 70_000L
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f, bestLapTimeMs = 90_000))

        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.RemainingFuelLapsWarning(3)),
            tts.spokenTexts,
        )
    }

    @Test
    fun `Slider5のとき残り5周から1周まで合計5回アナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 5),
        )

        // スタート: gasLevel=60L、1周10L消費
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 60f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f)) // 残り5周
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 40f, gasCapacity = 100f)) // 残り4周
        channel.send(gt7Telemetry(lapCount = 3, gasLevel = 30f, gasCapacity = 100f)) // 残り3周
        channel.send(gt7Telemetry(lapCount = 4, gasLevel = 20f, gasCapacity = 100f)) // 残り2周
        channel.send(gt7Telemetry(lapCount = 5, gasLevel = 10f, gasCapacity = 100f)) // 残り1周

        assertEquals(
            listOf<SpeechEvent>(
                SpeechEvent.RemainingFuelLapsWarning(5),
                SpeechEvent.RemainingFuelLapsWarning(4),
                SpeechEvent.RemainingFuelLapsWarning(3),
                SpeechEvent.RemainingFuelLapsWarning(2),
                SpeechEvent.RemainingFuelLapsWarning(1),
            ),
            tts.spokenTexts,
        )
    }

    @Test
    fun `残り周回数がSliderより多いときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 2),
        )

        // 1周あたり10L消費、60Lスタートなら残り6周 → Slider2を超えるのでアナウンスしない
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 60f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 50f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `同じ残り周回数が連続してもアナウンスは1回だけ`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 3),
        )

        // スタート: gasLevel=70L、1周10L消費
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 70f, gasCapacity = 100f))
        // lap1完了 → 残り6.0周 → アナウンスなし
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 60f, gasCapacity = 100f))
        // lap2完了 → 残り5.0周 → アナウンスなし
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 50f, gasCapacity = 100f))
        // lap3完了 → 残り40/10=4.0周 → まだアナウンスなし
        channel.send(gt7Telemetry(lapCount = 3, gasLevel = 40f, gasCapacity = 100f))
        // lap4完了 → 残り30/10=3.0周 → アナウンス(3)
        channel.send(gt7Telemetry(lapCount = 4, gasLevel = 30f, gasCapacity = 100f))
        // lap5完了 → 残り30/10=3.0周（燃料消費が一時的に少なかった想定、floor=3.0）
        // ※実際は消費が変動するが、今回は同じfloor値が連続する状況をテスト
        channel.send(gt7Telemetry(lapCount = 5, gasLevel = 30f, gasCapacity = 100f))

        // lapCount=5では gasLevel が変わらず lapsCompleted=5, consumedFuel=70-30=40, avg=8
        // remainingLapsFloor = floor(30/8) = 3 → 同じfloor値なのでアナウンスしない
        assertEquals(
            listOf<SpeechEvent>(SpeechEvent.RemainingFuelLapsWarning(3)),
            tts.spokenTexts,
        )
    }

    @Test
    fun `燃料残り周回数アナウンスが無効のときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = false, fuelThreshold = 3),
        )

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 30f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 20f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 10f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `lapCountがリセットされると燃料追跡とアナウンス履歴がリセットされる`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 3),
        )

        // 1周目セッション: 残り3周でアナウンス済み
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 100f))
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f)) // 残り3周
        // リセット
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 100f))
        // リセット後1周完了 → 残り3周 → アナウンス履歴がリセットされているので再アナウンス
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f))

        assertEquals(
            listOf<SpeechEvent>(
                SpeechEvent.RemainingFuelLapsWarning(3),
                SpeechEvent.RemainingFuelLapsWarning(3),
            ),
            tts.spokenTexts,
        )
    }

    @Test
    fun `1周も完了していないときはアナウンスしない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 3),
        )

        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 30f, gasCapacity = 100f))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `ピットイン給油後も正しい平均消費量でアナウンスする`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = RecordingTextToSpeechEngine()
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            readoutSettings = ReadoutSettings(remainingFuelLapsEnabled = true, fuelThreshold = 3),
        )

        // スタート: 40L、1周10L消費
        channel.send(gt7Telemetry(lapCount = 0, gasLevel = 40f, gasCapacity = 100f))
        // lap1完了 → 30L、消費10L、平均10L/周、残り3.0周 → アナウンス(3)
        channel.send(gt7Telemetry(lapCount = 1, gasLevel = 30f, gasCapacity = 100f))
        // ピット給油 → 50L（+20L補給）: lapCount変化なし、同一周なのでscanに流れない

        // lap2完了 → 給油後1周目: gasLevel=50L → totalRefueled=20, consumed=40+20-50=10, avg=10/2=5L/周
        // 残り=floor(50/5)=10周 → Sliderより多いのでアナウンスしない
        channel.send(gt7Telemetry(lapCount = 2, gasLevel = 50f, gasCapacity = 100f))
        // lap3完了 → 40L、consumed=40+20-40=20, avg=20/3≈6.67, 残り=floor(40/6.67)=5周 → アナウンスなし
        channel.send(gt7Telemetry(lapCount = 3, gasLevel = 40f, gasCapacity = 100f))
        // lap4完了 → 30L、consumed=40+20-30=30, avg=30/4=7.5, 残り=floor(30/7.5)=4周 → アナウンスなし
        channel.send(gt7Telemetry(lapCount = 4, gasLevel = 30f, gasCapacity = 100f))
        // lap5完了 → 20L、consumed=40+20-20=40, avg=40/5=8, 残り=floor(20/8)=2周 → アナウンス(2)
        channel.send(gt7Telemetry(lapCount = 5, gasLevel = 20f, gasCapacity = 100f))
        // lap6完了 → 10L、consumed=40+20-10=50, avg=50/6≈8.33, 残り=floor(10/8.33)=1周 → アナウンス(1)
        channel.send(gt7Telemetry(lapCount = 6, gasLevel = 10f, gasCapacity = 100f))

        assertEquals(
            listOf<SpeechEvent>(
                SpeechEvent.RemainingFuelLapsWarning(3),
                SpeechEvent.RemainingFuelLapsWarning(2),
                SpeechEvent.RemainingFuelLapsWarning(1),
            ),
            tts.spokenTexts,
        )
    }

    // --- 優先度 ---

    @Test
    fun `優先度の高いアイテム読み上げ中にベストラップが来ても読み上げない`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(initialKey = ReadoutItemKey.Flag)
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.Flag, ReadoutItemKey.MyBestLap),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(emptyList<SpeechEvent>(), tts.spokenTexts)
    }

    @Test
    fun `優先度の低いアイテム読み上げ中にベストラップが来ると割り込む`() = runTest(testDispatcher) {
        val channel = Channel<Gt7Ps5TelemetryData>(Channel.UNLIMITED)
        val tts = PriorityAwareTextToSpeechEngine(initialKey = ReadoutItemKey.Flag)
        buildViewModel(
            telemetryChannel = channel,
            ttsEngine = tts,
            orderOverride = listOf(ReadoutItemKey.MyBestLap, ReadoutItemKey.Flag),
        )

        channel.send(gt7Telemetry(bestLapTimeMs = 60_000))
        channel.send(gt7Telemetry(bestLapTimeMs = 59_000))

        assertEquals(true, tts.stopCalled)
        assertEquals(listOf<SpeechEvent>(SpeechEvent.MyBestLapFormal), tts.spokenTexts)
    }
}

private fun gt7Telemetry(bestLapTimeMs: Int) = Gt7Ps5TelemetryData(
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

private class RecordingTextToSpeechEngine : TextToSpeechEngine {
    val spokenTexts = mutableListOf<SpeechEvent>()
    override val currentReadoutItemKey: ReadoutItemKey? = null
    override fun speak(event: SpeechEvent, queue: Boolean) { spokenTexts.add(event) }
    override fun stop() = Unit
    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}

private class PriorityAwareTextToSpeechEngine(
    initialKey: ReadoutItemKey? = null,
) : TextToSpeechEngine {
    val spokenTexts = mutableListOf<SpeechEvent>()
    var stopCalled = false
    override var currentReadoutItemKey: ReadoutItemKey? = initialKey
    override fun speak(event: SpeechEvent, queue: Boolean) { spokenTexts.add(event) }
    override fun stop() {
        stopCalled = true
        currentReadoutItemKey = null
    }
    override fun previewStartSound(type: ReadoutStartSoundType) = Unit
}

private class FakeChannelGt7Ps5Repository(
    private val stream: Flow<Gt7Ps5TelemetryData>,
) : Gt7Ps5Repository {
    override fun telemetryStream(): Flow<Gt7Ps5TelemetryData> = stream
    override suspend fun isConnected(): Boolean = true
}

private class FakeSimulatorPreferencesRepo(
    private val simulator: Simulator?,
) : SimulatorPreferencesRepository {
    override fun selectedSimulator(): Flow<Simulator?> = MutableStateFlow(simulator)
    override suspend fun saveSelectedSimulator(simulator: Simulator) = Unit
}

private class FakeReadoutPreferencesRepo(
    private val enabledOverrides: Map<ReadoutItemKey, Boolean> = emptyMap(),
    private val orderOverride: List<ReadoutItemKey> = listOf(ReadoutItemKey.MyBestLap),
) : ReadoutPreferencesRepository {
    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        MutableStateFlow(enabledOverrides)

    override suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean) = Unit
    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> = MutableStateFlow(orderOverride)
    override suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>) = Unit
}

private class FakeMyBestLapPreferencesRepo(
    initialType: MyBestLapVoiceType = MyBestLapVoiceType.FORMAL,
) : MyBestLapPreferencesRepository {
    private val type = MutableStateFlow(initialType)
    override fun observeVoiceType(): Flow<MyBestLapVoiceType> = type
    override suspend fun saveVoiceType(type: MyBestLapVoiceType) {
        this.type.update { type }
    }
}

private class FakeRemainingFuelLapsPreferencesRepo(
    private val threshold: Int = 3,
) : Gt7Ps5RemainingFuelLapsPreferencesRepository {
    override fun observeRemainingFuelLaps(): Flow<Int> = MutableStateFlow(threshold)
    override suspend fun saveRemainingFuelLaps(laps: Int) = Unit
}

private class FakeGt7Ps5RemainingFuelLapsEnabledRepo(
    private val enabled: Boolean = true,
) : Gt7Ps5RemainingFuelLapsEnabledRepository {
    override fun observeEnabled(): Flow<Boolean> = MutableStateFlow(enabled)
    override suspend fun saveEnabled(enabled: Boolean) = Unit
}
