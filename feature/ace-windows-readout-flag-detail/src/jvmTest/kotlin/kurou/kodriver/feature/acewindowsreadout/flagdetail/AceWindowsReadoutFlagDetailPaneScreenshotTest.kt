package kurou.kodriver.feature.acewindowsreadout.flagdetail

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

class AceWindowsReadoutFlagDetailPaneScreenshotTest {
    @Test
    fun `デフォルト`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
            )
        }

    @Test
    fun `全カード無効`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to false },
            )
        }

    @Test
    fun `TTS利用不可`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.CheckeredFlag to "編集済み"),
                isTextToSpeechAvailable = false,
            )
        }

    @Test
    fun `空白文言`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.CheckeredFlag to ""),
                isTextToSpeechAvailable = true,
            )
        }

    @Test
    fun `編集済み文言のリセットボタン表示`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.CheckeredFlag to "チェッカー、完走"),
                isTextToSpeechAvailable = true,
            )
        }

    private fun DesktopComposeUiTest.captureAceWindowsReadoutFlagDetailPane(
        enabledStates: Map<ReadoutItemKey, Boolean>,
        flagTexts: Map<FlagReadoutItem, String> = emptyMap(),
        isTextToSpeechAvailable: Boolean = true,
    ) {
        setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(1560.dp, 1080.dp)) {
                        AceWindowsReadoutFlagDetailPaneContent(
                            uiState =
                                AceWindowsReadoutFlagDetailUiState(
                                    enabledStates = enabledStates,
                                    flagTexts = flagTexts,
                                    isTextToSpeechAvailable = isTextToSpeechAvailable,
                                ),
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
