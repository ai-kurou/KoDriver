package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.Test

class LmuWindowsReadoutFlagDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            captureLmuWindowsReadoutFlagDetailPane(
                enabledStates =
                    mapOf(
                        ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                        ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
                        ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
                        ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
                    ),
            )
        }

    @Test
    fun `カスタム文言あり`() =
        composeScreenshotTest {
            captureLmuWindowsReadoutFlagDetailPane(
                enabledStates =
                    mapOf(
                        ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                        ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
                        ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
                        ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
                    ),
                // カスタム文言の入力欄を持つ全項目に文言を入力した状態にする。
                flagTexts =
                    FlagReadoutItem.entries.associateWith { item ->
                        when (item) {
                            FlagReadoutItem.BlueFlag -> "ブルー、後続に譲ってください"
                            FlagReadoutItem.SectorYellowFlag -> "イエロー、前方注意"
                            FlagReadoutItem.FullCourseYellow -> "フルコースイエロー、減速"
                            FlagReadoutItem.RedFlag -> "レッドフラッグ、走行を中止してください"
                        }
                    },
                isTextToSpeechAvailable = true,
            )
        }

    private fun DesktopComposeUiTest.captureLmuWindowsReadoutFlagDetailPane(
        enabledStates: Map<ReadoutItemKey, Boolean>,
        flagTexts: Map<FlagReadoutItem, String> = emptyMap(),
        isTextToSpeechAvailable: Boolean = false,
    ) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                        LmuWindowsReadoutFlagDetailPaneContent(
                            uiState =
                                LmuWindowsReadoutFlagDetailUiState(
                                    enabledStates = enabledStates,
                                    flagTexts = flagTexts,
                                    isTextToSpeechAvailable = isTextToSpeechAvailable,
                                ),
                            onFlagEnabledChanged = { _, _ -> },
                            onFlagTextChanged = { _, _ -> },
                            onFlagTextPreviewClicked = { _, _ -> },
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
