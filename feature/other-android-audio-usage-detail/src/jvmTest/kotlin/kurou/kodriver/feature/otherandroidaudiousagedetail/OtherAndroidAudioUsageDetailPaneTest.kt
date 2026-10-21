package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.AndroidAudioUsage
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherAndroidAudioUsageDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `タイトルと説明を表示し戻る操作を通知する`() {
        var backCount = 0
        rule.setContent {
            KoDriverTheme {
                OtherAndroidAudioUsageDetailPaneContent(
                    uiState = OtherAndroidAudioUsageDetailUiState(),
                    canNavigateBack = true,
                    onBack = { backCount++ },
                )
            }
        }

        rule.onNodeWithText("音声の用途").assertIsDisplayed()
        rule.onNodeWithText("読み上げ音声を、メディアや通話などどの用途の音声として扱うか設定します。").assertIsDisplayed()
        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, backCount)
    }

    @Test
    fun `全用途の名前と説明を表示して選択を通知する`() {
        var state by mutableStateOf(OtherAndroidAudioUsageDetailUiState())
        val selected = mutableListOf<AndroidAudioUsage>()
        rule.setContent {
            KoDriverTheme {
                OtherAndroidAudioUsageDetailPaneContent(
                    uiState = state,
                    onUsageSelected = {
                        selected.add(it)
                        state = state.copy(usage = it)
                    },
                )
            }
        }
        val labels = listOf("メディア", "通話", "着信音", "通知", "アラーム", "アシスタント")
        val descriptions =
            listOf(
                "音楽や動画などのメディア音声として扱います。",
                "通話中の音声として扱います。",
                "電話の着信を知らせる音声として扱います。",
                "通知を知らせる音声として扱います。",
                "アラームの音声として扱います。",
                "音声アシスタントの応答として扱います。",
            )
        rule.onNodeWithText("メディア").assertIsSelected()
        rule.onNodeWithText("通話").assertIsNotSelected()
        AndroidAudioUsage.entries.forEachIndexed { index, _ ->
            rule.onNodeWithText(descriptions[index]).performScrollTo().assertIsDisplayed()
            rule
                .onNodeWithText(labels[index])
                .performScrollTo()
                .performClick()
                .assertIsSelected()
        }
        assertEquals(AndroidAudioUsage.entries.toList(), selected.toList())
    }

    @Test
    fun `戻れないときは戻るボタンを表示せずmodifierを適用する`() {
        rule.setContent {
            KoDriverTheme {
                OtherAndroidAudioUsageDetailPaneContent(
                    uiState = OtherAndroidAudioUsageDetailUiState(),
                    canNavigateBack = false,
                    modifier = Modifier.testTag("audio-usage-content"),
                )
            }
        }
        rule.onNode(hasContentDescription("戻る")).assertDoesNotExist()
        rule.onNodeWithTag("audio-usage-content").assertIsDisplayed()
    }
}
