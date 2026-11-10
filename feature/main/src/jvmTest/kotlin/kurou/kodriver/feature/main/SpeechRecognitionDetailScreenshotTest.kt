package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
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
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
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
import org.junit.Test

/** 詳細の外枠（ボトムシート・ポップオーバー）と、チップをタップして詳細が開くまでの合成を撮る。中身単体は [SpeechRecognitionChipScreenshotTest]。 */
@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionDetailScreenshotTest {
    private val listeningState =
        SpeechRecognitionUiState(
            enabled = true,
            status = SpeechRecognitionStatus.Listening,
            level = 0.6f,
            recognized = listOf(listOf("燃料", "然料"), listOf("ブレーキ")),
            devices = listOf("内蔵マイク", "USBマイク"),
            deviceName = "内蔵マイク",
        )

    @Test
    fun `ボトムシート 聞き取り中`() =
        composeScreenshotTest {
            captureDetail(SpeechRecognitionDetailPresentation.BottomSheet, listeningState)
        }

    @Test
    fun `ボトムシート 停止中`() =
        composeScreenshotTest {
            captureDetail(SpeechRecognitionDetailPresentation.BottomSheet, SpeechRecognitionUiState())
        }

    @Test
    fun `ポップオーバー 聞き取り中`() =
        composeScreenshotTest {
            captureDetail(SpeechRecognitionDetailPresentation.Popover, listeningState)
        }

    @Test
    fun `ポップオーバー 停止中`() =
        composeScreenshotTest {
            captureDetail(SpeechRecognitionDetailPresentation.Popover, SpeechRecognitionUiState())
        }

    @Test
    fun `チップ付き詳細 タップ前`() =
        withMainDispatcher {
            composeScreenshotTest {
                withChipWithDetail(SpeechRecognitionDetailPresentation.Popover) {}
            }
        }

    @Test
    fun `チップ付き詳細 ポップオーバーを開いた状態`() =
        withMainDispatcher {
            composeScreenshotTest {
                withChipWithDetail(SpeechRecognitionDetailPresentation.Popover) {
                    onNodeWithContentDescription("停止中").performClick()
                    waitForIdle()
                }
            }
        }

    @Test
    fun `チップ付き詳細 ボトムシートを開いた状態`() =
        withMainDispatcher {
            composeScreenshotTest {
                withChipWithDetail(SpeechRecognitionDetailPresentation.BottomSheet) {
                    onNodeWithContentDescription("停止中").performClick()
                    waitForIdle()
                }
            }
        }

    /** ViewModel の `viewModelScope` と Compose のテストが `Dispatchers.Main` を使うため、テスト全体を包んで差し替える。 */
    private fun withMainDispatcher(block: () -> Unit) {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    /** 右下にチップを置いた画面を想定し、[SpeechRecognitionDetail] を直接表示する。ポップオーバーはこのチップ位置の上に出る。 */
    private fun DesktopComposeUiTest.captureDetail(
        presentation: SpeechRecognitionDetailPresentation,
        uiState: SpeechRecognitionUiState,
    ) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(DETAIL_HOST_WIDTH, DETAIL_HOST_HEIGHT)) {
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(CHIP_MARGIN)) {
                            SpeechRecognitionChip(uiState = uiState)
                            SpeechRecognitionDetail(
                                presentation = presentation,
                                uiState = uiState,
                                onEnabledChange = {},
                                onDeviceMenuOpen = {},
                                onDeviceSelected = {},
                                onDismissRequest = {},
                            )
                        }
                    }
                }
            }
        }
        captureScene()
    }

    /** ViewModel を実際に組み立てた [rememberSpeechRecognitionChip] の合成を、右下に置いて [interact] の操作後に撮る。 */
    private fun DesktopComposeUiTest.withChipWithDetail(
        presentation: SpeechRecognitionDetailPresentation,
        interact: DesktopComposeUiTest.() -> Unit,
    ) {
        val viewModel = createViewModel()
        try {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(DETAIL_HOST_WIDTH, DETAIL_HOST_HEIGHT)) {
                            rememberSpeechRecognitionChip(viewModel, requestPermission = {})(
                                Modifier.align(Alignment.BottomEnd).padding(CHIP_MARGIN),
                                presentation,
                            )
                        }
                    }
                }
            }
            interact()
            captureScene()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    /**
     * Popup・ModalBottomSheet は本体とは別のルートに描かれ、そのルートはチップを含む画面全体を映す。
     * 詳細が開いていればそのルート、開いていなければ本体のルートを撮る。
     */
    private fun DesktopComposeUiTest.captureScene() {
        val roots = onAllNodes(isRoot())
        roots[roots.fetchSemanticsNodes().size - 1].captureRoboImage()
    }

    private fun createViewModel(): SpeechRecognitionViewModel {
        val speechRepository: SpeechRecognitionRepository = mockk()
        val permissionRepository: RecordAudioPermissionRepository = mockk()
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns MutableSharedFlow<SpeechRecognitionEvent>()
        return SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            requestManualReadoutItem = RequestManualReadoutItemUseCase(FakeManualReadoutItemRequestRepository()),
            setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(FakeSpeechRecognitionModeRepository()),
            observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(FakeSpeechPlaybackStateRepository()),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(FakeSelectedSimulatorRepository()),
            playChime = PlaySpeechRecognitionChimeUseCase(FakeSpeechRecognitionChimePlayer()),
            audioInputDeviceSelector = null,
        )
    }

    private companion object {
        val DETAIL_HOST_WIDTH = 480.dp
        val DETAIL_HOST_HEIGHT = 640.dp
        val CHIP_MARGIN = 26.dp
    }
}
