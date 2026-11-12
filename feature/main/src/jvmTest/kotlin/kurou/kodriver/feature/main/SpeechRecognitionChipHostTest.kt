package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
class SpeechRecognitionChipHostTest {
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

    /** チップとカードを並べて出す。チップのタップで ON/OFF が切り替わり、カードに状態が出る。 */
    @Composable
    private fun Chip(requestPermission: () -> Unit = {}) {
        val slots = rememberSpeechRecognition(viewModel, requestPermission)
        Column {
            slots.chip(Modifier)
            slots.card(Modifier)
        }
    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) viewModel.viewModelScope.cancel()
    }

    @Test
    fun `アプリが見えなくなると聞き取りを止める`() {
        createViewModel(granted = true)
        val owner = TestLifecycleOwner()
        owner.moveTo(Lifecycle.State.RESUMED)
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                KoDriverTheme {
                    Chip()
                }
            }
        }
        rule.onNodeWithContentDescription("停止中").performClick()
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
                KoDriverTheme { Chip() }
            }
        }
        rule.onNodeWithContentDescription("停止中").performClick()
        rule.runOnUiThread { owner.handle(Lifecycle.Event.ON_STOP) }
        rule.runOnIdle { assertFalse(viewModel.uiState.value.enabled) }

        rule.runOnUiThread { owner.handle(Lifecycle.Event.ON_START) }

        rule.runOnIdle { assertTrue(viewModel.uiState.value.enabled) }
    }

    @Test
    fun `デスクトップでは音声認識のチップを表示する`() {
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
                KoDriverTheme { rememberSpeechRecognition().chip(Modifier) }
            }
        }

        rule.onNodeWithContentDescription("停止中").assertExists()
        assertTrue(isSpeechRecognitionSupportedOnPlatform())
    }

    @Test
    fun `チップは聞き取りの状態に合わせて説明を切り替える`() {
        createViewModel(granted = true)
        rule.setContent {
            KoDriverTheme {
                Chip()
            }
        }
        rule.onAllNodesWithContentDescription("停止中").assertCountEquals(1)

        rule.onNodeWithContentDescription("停止中").performClick()
        rule.runOnIdle { events.tryEmit(SpeechRecognitionEvent.Listening) }

        rule.onAllNodesWithContentDescription("「コドライバー」の呼びかけを待っています(マイクを使用中)").assertCountEquals(1)
    }

    @Test
    fun `チップをタップすると聞き取りを開始し、もう一度タップすると止める`() {
        createViewModel(granted = true)
        rule.setContent { KoDriverTheme { Chip() } }

        rule.onNodeWithContentDescription("停止中").performClick()
        rule.runOnIdle { assertTrue(viewModel.uiState.value.enabled) }
        rule.runOnIdle { events.tryEmit(SpeechRecognitionEvent.Listening) }
        rule.onAllNodesWithContentDescription("「コドライバー」の呼びかけを待っています(マイクを使用中)")[0].performClick()

        rule.runOnIdle { assertFalse(viewModel.uiState.value.enabled) }
    }

    @Test
    fun `権限が未許可のままチップをタップすると権限の要求を呼ぶ`() {
        createViewModel(granted = false)
        var requestCount = 0
        rule.setContent {
            KoDriverTheme {
                Chip(requestPermission = { requestCount++ })
            }
        }

        rule.onNodeWithContentDescription("停止中").performClick()

        assertEquals(1, requestCount)
    }

    @Test
    fun `カードは表示した時点で入力デバイスの一覧を取得し選んだデバイスを反映する`() {
        audioInputDeviceRepository.devices = listOf(AudioInputDevice("Mic A"), AudioInputDevice("Mic B"))
        createViewModel(granted = true, deviceSelector = audioInputDeviceSelector(audioInputDeviceRepository))
        rule.setContent { KoDriverTheme { Chip() } }

        rule.onNodeWithTag("speech_recognition_device").assertTextContains("Mic A")
        rule.onNodeWithTag("speech_recognition_device").performClick()
        rule.onNodeWithTag("speech_recognition_device_Mic B").performClick()

        rule.onNodeWithTag("speech_recognition_device").assertTextContains("Mic B")
    }

    @Test
    fun `音声認識を提供しない組は何も表示しない`() {
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionSlots.None.chip(Modifier)
                SpeechRecognitionSlots.None.card(Modifier)
            }
        }

        rule.onNodeWithContentDescription("停止中").assertDoesNotExist()
    }
}
