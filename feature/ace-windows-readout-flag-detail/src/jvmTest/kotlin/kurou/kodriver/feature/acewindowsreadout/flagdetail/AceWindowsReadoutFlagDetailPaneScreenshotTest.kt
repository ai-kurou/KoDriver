package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
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
                flagTexts = FlagReadoutItem.entries.filter { it.defaultText != null }.associateWith { "編集済み" },
                isTextToSpeechAvailable = false,
            )
        }

    @Test
    fun `空白文言`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = FlagReadoutItem.entries.filter { it.defaultText != null }.associateWith { "" },
                isTextToSpeechAvailable = true,
            )
        }

    @Test
    fun `編集済み文言のリセットボタン表示`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = FlagReadoutItem.entries.filter { it.defaultText != null }.associateWith { "チェッカー、完走" },
                isTextToSpeechAvailable = true,
            )
        }

    @Test
    fun `Whiteの自由文言入力`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.WhiteFlag to "編集済みWhite"),
                scrollToText = "編集済みWhite",
            )
        }

    @Test
    fun `Greenの自由文言入力`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.GreenFlag to "編集済みGreen"),
                scrollToText = "編集済みGreen",
            )
        }

    @Test
    fun `Redの自由文言入力`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.RedFlag to "編集済みRed"),
                scrollToText = "編集済みRed",
            )
        }

    @Test
    fun `Blueの自由文言入力`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.BlueFlag to "編集済みBlue"),
                scrollToText = "編集済みBlue",
            )
        }

    @Test
    fun `Yellowの自由文言入力`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.YellowFlag to "編集済みYellow"),
                scrollToText = "編集済みYellow",
            )
        }

    @Test
    fun `Blackの自由文言入力`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.BlackFlag to "編集済みBlack"),
                scrollToText = "編集済みBlack",
            )
        }

    @Test
    fun `BlackWhiteの自由文言入力`() =
        composeScreenshotTest {
            captureAceWindowsReadoutFlagDetailPane(
                enabledStates = FlagReadoutItem.entries.associate { it.key to true },
                flagTexts = mapOf(FlagReadoutItem.BlackWhiteFlag to "編集済みBlackWhite"),
                scrollToText = "編集済みBlackWhite",
            )
        }

    private fun DesktopComposeUiTest.captureAceWindowsReadoutFlagDetailPane(
        enabledStates: Map<ReadoutItemKey, Boolean>,
        flagTexts: Map<FlagReadoutItem, String> = emptyMap(),
        isTextToSpeechAvailable: Boolean = true,
        scrollToText: String? = null,
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
        scrollToText?.let { onNodeWithText(it).performScrollTo() }
        onRoot().captureRoboImage()
    }
}
