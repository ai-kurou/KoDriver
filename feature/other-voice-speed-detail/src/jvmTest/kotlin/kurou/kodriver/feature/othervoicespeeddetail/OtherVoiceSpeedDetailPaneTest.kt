package kurou.kodriver.feature.othervoicespeeddetail

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherVoiceSpeedDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明を表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPaneContent(
                    uiState = OtherVoiceSpeedDetailUiState(),
                    canNavigateBack = true,
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNodeWithText("1.0倍").assertIsDisplayed()
        rule.onNodeWithText("読み上げ速度").assertIsDisplayed()
        rule.onNodeWithText("読み上げの速さを設定します。").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `戻れない場合は戻るボタンを表示しない`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPaneContent(
                    uiState = OtherVoiceSpeedDetailUiState(),
                    canNavigateBack = false,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("読み上げ速度").assertIsDisplayed()
        rule.onNodeWithText("読み上げの速さを設定します。").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @Test
    fun `渡したmodifierを詳細ペインに適用する`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPaneContent(
                    uiState = OtherVoiceSpeedDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                    modifier = Modifier.testTag("voice-speed-detail"),
                )
            }
        }

        rule.onNodeWithTag("voice-speed-detail").assertIsDisplayed()
    }

    @Test
    fun `速度スライダー操作を完了すると変更後の速度を通知する`() {
        var changedSpeed: Float? = null
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPaneContent(
                    uiState = OtherVoiceSpeedDetailUiState(),
                    onSpeedChanged = { changedSpeed = it },
                )
            }
        }

        rule
            .onNode(
                SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
                    it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
                },
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(2.0f) }

        assertEquals(2.0f, changedSpeed)
        rule.onNodeWithText("2.0倍").assertIsDisplayed()
    }

    @Test
    fun `リセット操作はデフォルト速度を通知する`() {
        var changedSpeed: Float? = null
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPaneContent(
                    uiState = OtherVoiceSpeedDetailUiState(speed = 1.5f),
                    onSpeedChanged = { changedSpeed = it },
                )
            }
        }

        rule.onNode(hasContentDescription("読み上げ速度をデフォルトに戻す")).performClick()

        assertEquals(1.0f, changedSpeed)
    }

    @Test
    fun `デフォルト速度ではリセット操作は無効になる`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPaneContent(uiState = OtherVoiceSpeedDetailUiState())
            }
        }

        rule.onNode(hasContentDescription("読み上げ速度をデフォルトに戻す")).assertIsNotEnabled()
    }

    @Test
    fun `試聴ボタンを押すと試聴用の文言を通知する`() {
        var previewText: String? = null
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPaneContent(
                    uiState = OtherVoiceSpeedDetailUiState(),
                    onPreviewClicked = { previewText = it },
                )
            }
        }

        rule.onNodeWithText("試聴").performClick()

        assertEquals("これは読み上げ速度の試聴です。", previewText)
    }

    @Test
    fun `試聴中は停止ボタンを表示する`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoiceSpeedDetailPaneContent(
                    uiState = OtherVoiceSpeedDetailUiState(isPreviewing = true),
                )
            }
        }

        rule.onNodeWithText("試聴を停止").assertIsDisplayed()
        rule.onNodeWithText("試聴").assertDoesNotExist()
    }
}
