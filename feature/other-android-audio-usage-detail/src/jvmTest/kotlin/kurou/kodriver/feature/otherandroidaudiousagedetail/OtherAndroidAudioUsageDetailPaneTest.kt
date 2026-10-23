package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
        rule.onNodeWithText("読み上げ音声を、メディアや通話などどの用途の音声として扱うか設定します。右の再生ボタンで、その用途で試聴できます。").assertIsDisplayed()
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
    fun `選択カードの読み上げ対象に装飾用の頭文字を含めない`() {
        rule.setContent {
            KoDriverTheme {
                OtherAndroidAudioUsageDetailPaneContent(uiState = OtherAndroidAudioUsageDetailUiState())
            }
        }

        rule
            .onNodeWithText("メディア")
            .assertTextEquals("メディア", "音楽や動画などのメディア音声として扱います。")
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

    @Test
    fun `試聴ボタンは選択と独立し再生中の説明を切り替える`() {
        var state by mutableStateOf(OtherAndroidAudioUsageDetailUiState())
        val previews = mutableListOf<Pair<AndroidAudioUsage, String>>()
        rule.setContent {
            KoDriverTheme {
                OtherAndroidAudioUsageDetailPaneContent(
                    uiState = state,
                    onPreviewClicked = { usage, text ->
                        previews.add(usage to text)
                        state = state.copy(previewingUsage = usage)
                    },
                )
            }
        }
        rule.onNodeWithContentDescription("アラームを試聴").performScrollTo().performClick()
        rule.onNodeWithContentDescription("アラームの試聴を停止").assertIsDisplayed()
        rule.onNodeWithText("アラーム").assertIsNotSelected()
        assertEquals(AndroidAudioUsage.ALARM, previews.single().first)
        assertEquals("これは音声の用途の試聴です。", previews.single().second)
    }

    @Test
    fun `音量ボタンを表示してそれぞれの操作を通知する`() {
        var volumeCount = 0
        var soundCount = 0
        rule.setContent {
            KoDriverTheme {
                OtherAndroidAudioUsageDetailPaneContent(
                    uiState = OtherAndroidAudioUsageDetailUiState(),
                    onOpenVolumePanel = { volumeCount++ },
                    onOpenSoundSettings = { soundCount++ },
                )
            }
        }
        rule.onNodeWithText("音量を調整する").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("音量パネルを開く").performScrollTo().performClick()
        rule.onNodeWithText("サウンド設定を開く").performScrollTo().performClick()
        assertEquals(1, volumeCount)
        assertEquals(1, soundCount)
    }

    @Test
    fun `音量アクションがnullなら該当ボタンのみ非表示`() {
        rule.setContent {
            KoDriverTheme {
                OtherAndroidAudioUsageDetailPaneContent(
                    uiState = OtherAndroidAudioUsageDetailUiState(),
                    onOpenSoundSettings = {},
                )
            }
        }
        rule.onNodeWithText("音量を調整する").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("音量パネルを開く").assertDoesNotExist()
        rule.onNodeWithText("サウンド設定を開く").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `両方の音量アクションがnullならセクションを表示しない`() {
        rule.setContent {
            KoDriverTheme { OtherAndroidAudioUsageDetailPaneContent(uiState = OtherAndroidAudioUsageDetailUiState()) }
        }
        rule.onNodeWithText("音量を調整する").assertDoesNotExist()
    }

    @Test
    fun `音量ヒントは選択用途に対応するストリーム名を表示する`() {
        rule.setContent {
            KoDriverTheme {
                OtherAndroidAudioUsageDetailPaneContent(
                    uiState = OtherAndroidAudioUsageDetailUiState(usage = AndroidAudioUsage.ASSISTANT),
                    onOpenVolumePanel = {},
                )
            }
        }
        rule.onNodeWithText("いま選んでいる「アシスタント」の音声は、「メディア」の音量で鳴ります。").performScrollTo().assertIsDisplayed()
    }
}
