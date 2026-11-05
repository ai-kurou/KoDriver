package kurou.kodriver.feature.main

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
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
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.usecase.GetAudioInputDevicesUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneInputStatusUseCase
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MicrophoneInputSectionTest {
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

    private val microphoneRepository: MicrophoneRepository = mockk()
    private val deviceRepository: AudioInputDeviceRepository = mockk()
    private val events = MutableSharedFlow<MicrophoneAudioEvent>(extraBufferCapacity = 16)
    private val viewModel: MicrophoneInputViewModel

    init {
        viewModel =
            MicrophoneInputViewModel(
                observeMicrophoneInputStatus = ObserveMicrophoneInputStatusUseCase(microphoneRepository),
                getAudioInputDevices = GetAudioInputDevicesUseCase(deviceRepository),
            )
    }

    private fun stubMicA() {
        val micA = AudioInputDevice("Mic A")
        coEvery { deviceRepository.getDevices() } returns listOf(micA)
        every { microphoneRepository.observeAudio(micA) } returns events
    }

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

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `スイッチを操作すると取得を開始してデバイス名と状態を表示する`() {
        stubMicA()
        rule.setContent {
            KoDriverTheme { rememberMicrophoneInputSection(viewModel)(Modifier) }
        }

        rule.onNodeWithContentDescription("マイク入力の確認を切り替え").performClick()

        rule.onNodeWithText("準備中(マイクを使用中)").assertExists()
        rule.onNodeWithText("入力デバイス: Mic A").assertExists()
    }

    @Test
    fun `アプリが見えなくなると取得を止め前面に戻ると再開する`() {
        stubMicA()
        val owner = TestLifecycleOwner()
        owner.moveTo(Lifecycle.State.RESUMED)
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                KoDriverTheme { rememberMicrophoneInputSection(viewModel)(Modifier) }
            }
        }
        rule.onNodeWithContentDescription("マイク入力の確認を切り替え").performClick()
        rule.runOnIdle { assertTrue(viewModel.uiState.value.enabled) }

        rule.runOnIdle { owner.handle(Lifecycle.Event.ON_STOP) }
        rule.runOnIdle { assertEquals(false, viewModel.uiState.value.enabled) }

        rule.runOnIdle { owner.handle(Lifecycle.Event.ON_START) }
        rule.runOnIdle { assertTrue(viewModel.uiState.value.enabled) }
    }
}
