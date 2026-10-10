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
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.Test

class LmuWindowsReadoutFlagDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            captureLmuWindowsReadoutFlagDetailPane(
                enabledStates =
                    mapOf(
                        LmuWindowsReadoutItemKey.Flag.BlueFlag to true,
                        LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to true,
                        LmuWindowsReadoutItemKey.Flag.FullCourseYellow to true,
                        LmuWindowsReadoutItemKey.Flag.RedFlag to true,
                    ),
            )
        }

    @Test
    fun `カスタム文言あり`() =
        composeScreenshotTest {
            captureLmuWindowsReadoutFlagDetailPane(
                enabledStates =
                    mapOf(
                        LmuWindowsReadoutItemKey.Flag.BlueFlag to true,
                        LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to true,
                        LmuWindowsReadoutItemKey.Flag.FullCourseYellow to true,
                        LmuWindowsReadoutItemKey.Flag.RedFlag to true,
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

    @Test
    fun `編集済み文言のリセットボタン表示`() =
        composeScreenshotTest {
            captureLmuWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = FlagReadoutItem.entries.associateWith { "編集済みの読み上げ文言" },
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
                            onFlagTextReset = {},
                            onFlagTextPreviewClicked = {},
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
