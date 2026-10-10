package kurou.kodriver.feature.othervoicepitchdetail

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
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

class OtherVoicePitchDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明を表示して戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            KoDriverTheme {
                OtherVoicePitchDetailPaneContent(
                    uiState = OtherVoicePitchDetailUiState(),
                    canNavigateBack = true,
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNodeWithText("1.0倍").assertIsDisplayed()
        rule.onNodeWithText("声の高さ").assertIsDisplayed()
        rule.onNodeWithText("読み上げ音声の高さを設定します。").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `戻れない場合は戻るボタンを表示しない`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoicePitchDetailPaneContent(
                    uiState = OtherVoicePitchDetailUiState(),
                    canNavigateBack = false,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("声の高さ").assertIsDisplayed()
        rule.onNodeWithText("読み上げ音声の高さを設定します。").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
    }

    @Test
    fun `渡したmodifierを詳細ペインに適用する`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoicePitchDetailPaneContent(
                    uiState = OtherVoicePitchDetailUiState(),
                    canNavigateBack = true,
                    onBack = {},
                    modifier = Modifier.testTag("voice-pitch-detail"),
                )
            }
        }

        rule.onNodeWithTag("voice-pitch-detail").assertIsDisplayed()
    }

    @Test
    fun `声の高さスライダー操作を完了すると変更後の声の高さを通知する`() {
        var changedPitch: Float? = null
        rule.setContent {
            KoDriverTheme {
                OtherVoicePitchDetailPaneContent(
                    uiState = OtherVoicePitchDetailUiState(),
                    onPitchChanged = { changedPitch = it },
                )
            }
        }

        rule
            .onNode(
                SemanticsMatcher("ProgressBarRangeInfoを持つスライダー") {
                    it.config.contains(SemanticsProperties.ProgressBarRangeInfo)
                },
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo(1.0f, 0.5f..2.0f, 14),
                ),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(2.0f) }

        assertEquals(2.0f, changedPitch)
        rule.onNodeWithText("2.0倍").assertIsDisplayed()
    }

    @Test
    fun `リセット操作はデフォルトの声の高さを通知する`() {
        var changedPitch: Float? = null
        rule.setContent {
            KoDriverTheme {
                OtherVoicePitchDetailPaneContent(
                    uiState = OtherVoicePitchDetailUiState(pitch = 1.5f),
                    onPitchChanged = { changedPitch = it },
                )
            }
        }

        rule.onNode(hasContentDescription("声の高さをデフォルトに戻す")).performClick()

        assertEquals(1.0f, changedPitch)
    }

    @Test
    fun `デフォルトの声の高さではリセット操作は無効になる`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoicePitchDetailPaneContent(uiState = OtherVoicePitchDetailUiState())
            }
        }

        rule.onNode(hasContentDescription("声の高さをデフォルトに戻す")).assertIsNotEnabled()
    }

    @Test
    fun `試聴ボタンを押すと試聴用の文言を通知する`() {
        var previewText: String? = null
        rule.setContent {
            KoDriverTheme {
                OtherVoicePitchDetailPaneContent(
                    uiState = OtherVoicePitchDetailUiState(),
                    onPreviewClicked = { previewText = it },
                )
            }
        }

        rule.onNodeWithText("試聴").performClick()

        assertEquals("これは声の高さの試聴です。", previewText)
    }

    @Test
    fun `試聴中は停止ボタンを表示する`() {
        rule.setContent {
            KoDriverTheme {
                OtherVoicePitchDetailPaneContent(
                    uiState = OtherVoicePitchDetailUiState(isPreviewing = true),
                )
            }
        }

        rule.onNodeWithText("試聴を停止").assertIsDisplayed()
        rule.onNodeWithText("試聴").assertDoesNotExist()
    }
}
