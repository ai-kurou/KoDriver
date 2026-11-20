package kurou.kodriver.feature.main

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kurou.kodriver.domain.model.AudioInputDevice
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionDeviceControllerTest {
    private val scope = TestScope(UnconfinedTestDispatcher())
    private val deviceRepository =
        FakeAudioInputDeviceRepository(listOf(AudioInputDevice("Mic A"), AudioInputDevice("Mic B")))
    private val selectedRepository = FakeSelectedAudioInputDevicePreferencesRepository()
    private val uiState = MutableStateFlow(SpeechRecognitionUiState())
    private val cancelLaunch: () -> Unit = mockk(relaxed = true)
    private val restart: () -> Unit = mockk(relaxed = true)
    private val controller =
        SpeechRecognitionDeviceController(
            scope = scope,
            selector = audioInputDeviceSelector(deviceRepository, selectedRepository),
            uiState = uiState,
            cancelLaunch = cancelLaunch,
            restart = restart,
        )

    @Test
    fun `一覧を取得すると一覧と選択中のデバイスを反映し聞き取りはやり直さない`() {
        controller.refresh()

        assertEquals(listOf("Mic A", "Mic B"), uiState.value.devices)
        assertEquals("Mic A", uiState.value.deviceName)
        verify(exactly = 0) { restart() }
        confirmVerified(restart)
    }

    @Test
    fun `聞き取り中に別のデバイスを選ぶと起動時の自動開始を取り消し保存してからやり直す`() {
        controller.refresh()
        uiState.value = uiState.value.copy(enabled = true)

        controller.select("Mic B")

        assertEquals("Mic B", uiState.value.deviceName)
        assertEquals("Mic B", selectedRepository.saved.value)
        verify(exactly = 1) { cancelLaunch() }
        verify(exactly = 1) { restart() }
        confirmVerified(cancelLaunch, restart)
    }

    @Test
    fun `聞き取りを始める前に一覧と選択を取り直して表示へ反映する`() {
        runBlocking { controller.sync() }

        assertEquals(listOf("Mic A", "Mic B"), uiState.value.devices)
        assertEquals("Mic A", uiState.value.deviceName)
    }
}
