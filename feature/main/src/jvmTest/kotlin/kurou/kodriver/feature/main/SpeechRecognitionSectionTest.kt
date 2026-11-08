package kurou.kodriver.feature.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechRecognitionChimeUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kurou.kodriver.domain.usecase.SetSpeechRecognitionModeUseCase
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionSectionTest {
    /** Compose のルールが後始末で `Dispatchers.Main` を使うため、それより外側で差し替える。 */
    @get:Rule(order = 0)
    val mainDispatcherRule =
        object : ExternalResource() {
            override fun before() {
                Dispatchers.setMain(UnconfinedTestDispatcher())
            }

            override fun after() {
                Dispatchers.resetMain()
            }
        }

    @get:Rule(order = 1)
    val rule = createComposeRule()

    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val itemRequestRepository = FakeManualReadoutItemRequestRepository()
    private val audioInputDeviceRepository = FakeAudioInputDeviceRepository()
    private val events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = 16)
    private lateinit var viewModel: SpeechRecognitionViewModel

    private class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle get() = registry

        fun moveTo(state: Lifecycle.State) {
            registry.currentState = state
        }

        fun handle(event: Lifecycle.Event) {
            registry.handleLifecycleEvent(event)
        }
    }

    private fun createViewModel(
        granted: Boolean,
        deviceSelector: AudioInputDeviceSelector? = null,
    ) {
        every { permissionRepository.isGranted() } returns granted
        every { speechRepository.observeRecognition() } returns events
        viewModel =
            SpeechRecognitionViewModel(
                observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
                checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
                requestManualReadoutItem = RequestManualReadoutItemUseCase(itemRequestRepository),
                setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(FakeSpeechRecognitionModeRepository()),
                observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(FakeSpeechPlaybackStateRepository()),
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(FakeSelectedSimulatorRepository()),
                playChime = PlaySpeechRecognitionChimeUseCase(FakeSpeechRecognitionChimePlayer()),
                audioInputDeviceSelector = deviceSelector,
            )
    }

    @Composable
    private fun Section(requestPermission: () -> Unit = {}) {
        rememberSpeechRecognitionSection(viewModel, requestPermission)(Modifier)
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) viewModel.viewModelScope.cancel()
    }

    @Test
    fun `権限が許可済みでスイッチを操作すると聞き取りを開始して状態を表示する`() {
        createViewModel(granted = true)
        var requestCount = 0
        rule.setContent {
            KoDriverTheme {
                Section(requestPermission = { requestCount++ })
            }
        }

        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()
        rule.runOnIdle { events.tryEmit(SpeechRecognitionEvent.Listening) }

        rule.onNodeWithText("「ドライバー」の呼びかけを待っています(マイクを使用中)").assertExists()
        assertEquals(0, requestCount)
    }

    @Test
    fun `権限が未許可でスイッチを操作すると権限の要求を呼ぶ`() {
        createViewModel(granted = false)
        var requestCount = 0
        rule.setContent {
            KoDriverTheme {
                Section(requestPermission = { requestCount++ })
            }
        }

        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()

        rule.runOnIdle { assertEquals(1, requestCount) }
        assertFalse(viewModel.uiState.value.enabled)
    }

    @Test
    fun `アプリが見えなくなると聞き取りを止める`() {
        createViewModel(granted = true)
        val owner = TestLifecycleOwner()
        owner.moveTo(Lifecycle.State.RESUMED)
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                KoDriverTheme {
                    Section()
                }
            }
        }
        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()
        rule.runOnIdle { assertTrue(viewModel.uiState.value.enabled) }

        rule.runOnUiThread { owner.handle(Lifecycle.Event.ON_STOP) }

        rule.runOnIdle { assertFalse(viewModel.uiState.value.enabled) }
    }

    @Test
    fun `前面に戻ると見えなくなる前に聞き取り中だった場合は再開する`() {
        createViewModel(granted = true)
        val owner = TestLifecycleOwner()
        owner.moveTo(Lifecycle.State.RESUMED)
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                KoDriverTheme { Section() }
            }
        }
        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()
        rule.runOnUiThread { owner.handle(Lifecycle.Event.ON_STOP) }
        rule.runOnIdle { assertFalse(viewModel.uiState.value.enabled) }

        rule.runOnUiThread { owner.handle(Lifecycle.Event.ON_START) }

        rule.runOnIdle { assertTrue(viewModel.uiState.value.enabled) }
    }

    @Test
    fun `入力デバイスを選べる場合はカードを出すときに一覧を取得して選択中のデバイスを表示する`() {
        audioInputDeviceRepository.devices = listOf(AudioInputDevice("Mic A"), AudioInputDevice("Mic B"))
        createViewModel(granted = true, deviceSelector = audioInputDeviceSelector(audioInputDeviceRepository))
        rule.setContent { KoDriverTheme { Section() } }

        rule.onNodeWithTag("speech_recognition_device").assertTextContains("Mic A")
        rule.onNodeWithTag("speech_recognition_device").performClick()
        rule.onNodeWithTag("speech_recognition_device_Mic B").performClick()

        rule.onNodeWithTag("speech_recognition_device").assertTextContains("Mic B")
        rule.runOnIdle { assertEquals(2, audioInputDeviceRepository.getCount) }
    }

    @Test
    fun `入力デバイスを選べない場合はデバイスの選択を表示しない`() {
        createViewModel(granted = true)
        rule.setContent { KoDriverTheme { Section() } }

        rule.onNodeWithText("音声認識").assertExists()
        rule.onNodeWithTag("speech_recognition_device").assertDoesNotExist()
    }

    @Test
    fun `デスクトップでは音声認識のカードを表示する`() {
        createViewModel(granted = true)
        rule.setContent {
            KoinApplication(
                koinConfiguration {
                    modules(
                        module {
                            viewModel { viewModel }
                        },
                    )
                },
            ) {
                KoDriverTheme { rememberSpeechRecognitionSection()(Modifier) }
            }
        }

        rule.onNodeWithText("音声認識").assertExists()
        assertTrue(isSpeechRecognitionSupportedOnPlatform())
    }
}
