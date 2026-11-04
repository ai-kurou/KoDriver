package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
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

    private fun createViewModel(granted: Boolean) {
        every { permissionRepository.isGranted() } returns granted
        every { speechRepository.observeRecognition() } returns events
        viewModel =
            SpeechRecognitionViewModel(
                observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
                checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            )
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
                SpeechRecognitionSection(viewModel = viewModel, requestPermission = { requestCount++ })
            }
        }

        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()
        rule.runOnIdle { events.tryEmit(SpeechRecognitionEvent.Listening) }

        rule.onNodeWithText("聞き取り中").assertExists()
        assertEquals(0, requestCount)
    }

    @Test
    fun `権限が未許可でスイッチを操作すると権限の要求を呼ぶ`() {
        createViewModel(granted = false)
        var requestCount = 0
        rule.setContent {
            KoDriverTheme {
                SpeechRecognitionSection(viewModel = viewModel, requestPermission = { requestCount++ })
            }
        }

        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()

        rule.runOnIdle { assertEquals(1, requestCount) }
        assertFalse(viewModel.uiState.value.enabled)
    }

    @Test
    fun `画面から離れると聞き取りを止める`() {
        createViewModel(granted = true)
        val visible = mutableStateOf(true)
        rule.setContent {
            KoDriverTheme {
                if (visible.value) {
                    SpeechRecognitionSection(viewModel = viewModel, requestPermission = {})
                }
            }
        }
        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()
        rule.runOnIdle { assertTrue(viewModel.uiState.value.enabled) }

        visible.value = false

        rule.runOnIdle { assertFalse(viewModel.uiState.value.enabled) }
    }

    @Test
    fun `アプリが見えなくなると聞き取りを止める`() {
        createViewModel(granted = true)
        val owner = TestLifecycleOwner()
        owner.moveTo(Lifecycle.State.RESUMED)
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                KoDriverTheme {
                    SpeechRecognitionSection(viewModel = viewModel, requestPermission = {})
                }
            }
        }
        rule.onNodeWithContentDescription("音声認識の聞き取りを切り替え").performClick()
        rule.runOnIdle { assertTrue(viewModel.uiState.value.enabled) }

        rule.runOnUiThread { owner.handle(Lifecycle.Event.ON_STOP) }

        rule.runOnIdle { assertFalse(viewModel.uiState.value.enabled) }
    }

    @Test
    fun `音声認識を提供しないプラットフォームでは何も表示しない`() {
        rule.setContent {
            KoDriverTheme { ManualReadoutSpeechRecognitionSection() }
        }

        rule.onNodeWithText("音声認識(テスト)").assertDoesNotExist()
        assertFalse(isSpeechRecognitionSupportedOnPlatform())
    }
}
