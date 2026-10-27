package kurou.kodriver.presentation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListResetUiState
import kurou.kodriver.feature.otherlist.OtherListUiState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentAutoReadoutSettingsResetTest {
    @get:Rule
    val rule = createComposeRule()

    private val compactWindowSizeClass = WindowSizeClass.compute(400f, 800f)

    private val singlePaneDirective =
        PaneScaffoldDirective(
            maxHorizontalPartitions = 1,
            horizontalPartitionSpacerSize = 0.dp,
            maxVerticalPartitions = 1,
            verticalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = 360.dp,
            excludedBounds = emptyList(),
        )

    @Test
    fun `自動設定初期化ダイアログの確認とキャンセルを配線する`() {
        var confirmed = 0
        var dismissed = 0
        rule.setContent {
            AppTheme {
                OtherContent(
                    uiState =
                        OtherListUiState(
                            autoReadoutSettingsReset = OtherListResetUiState(showResetConfirmDialog = true),
                        ),
                    onItemSelected = {},
                    onClearSelectedItem = {},
                    onAutoReadoutSettingsResetConfirm = { confirmed++ },
                    onAutoReadoutSettingsResetDismiss = { dismissed++ },
                )
            }
        }
        rule
            .onNodeWithText(
                "自動タブのすべてのシミュレーター（LMU・ACE・GT7）の設定を初期化し、デフォルトに戻します。" +
                    "読み上げの有効/無効、並び順、閾値、読み上げ文言などが対象です。この操作は元に戻せません。",
            ).assertExists()
        rule.onNodeWithText("キャンセル").performClick()
        assertEquals(1, dismissed)
        rule.onNodeWithText("初期化", substring = false).performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun `初期化結果の成功と失敗をスナックバーで通知して消費する`() {
        var succeeded by mutableStateOf<Boolean?>(true)
        var consumed = 0
        rule.setContent {
            AppTheme {
                OtherContent(
                    uiState =
                        OtherListUiState(autoReadoutSettingsReset = OtherListResetUiState(resetSucceeded = succeeded)),
                    onItemSelected = {},
                    onClearSelectedItem = {},
                    onAutoReadoutSettingsResetResultConsumed = { consumed++ },
                )
            }
        }
        rule.onNodeWithText("自動タブの設定を初期化しました").assertExists()
        rule.waitUntil(timeoutMillis = 5_000L) { consumed == 1 }
        rule.runOnIdle { succeeded = false }
        rule.onNodeWithText("自動タブの設定の初期化に失敗しました").assertExists()
        rule.waitUntil(timeoutMillis = 5_000L) { consumed == 2 }
    }

    @Test
    fun `初期化結果の表示中にタブを離れると結果を消費する`() {
        var visible by mutableStateOf(true)
        var consumed = 0
        rule.setContent {
            AppTheme {
                if (visible) {
                    OtherContent(
                        uiState =
                            OtherListUiState(autoReadoutSettingsReset = OtherListResetUiState(resetSucceeded = true)),
                        onItemSelected = {},
                        onClearSelectedItem = {},
                        onAutoReadoutSettingsResetResultConsumed = { consumed++ },
                    )
                }
            }
        }
        rule.onNodeWithText("自動タブの設定を初期化しました").assertExists()
        rule.runOnIdle { visible = false }
        rule.waitUntil(timeoutMillis = 5_000L) { consumed >= 1 }
        assertTrue(consumed >= 1)
    }

    @Test
    fun `自動タブの設定を初期化をタップしても選択や詳細遷移を行わない`() {
        var selectedItem by mutableStateOf<OtherListItemType?>(null)
        var backEnabled = false
        var detailDisplayed = false
        var resetClicked = false
        rule.setContent {
            OtherContent(
                uiState =
                    OtherListUiState(
                        items = listOf(OtherListItemType.ResetAutoReadoutSettings),
                        selectedItem = selectedItem,
                    ),
                onAutoReadoutSettingsResetClick = { resetClicked = true },
                onItemSelected = { selectedItem = it },
                onClearSelectedItem = {},
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled, _, _ -> backEnabled = enabled },
                detailContent = { _, _, _, _, _ -> detailDisplayed = true },
            )
        }

        rule.onNodeWithText("自動タブの設定を初期化").assertExists().performTouchInput { click() }
        rule.waitForIdle()

        assertTrue(resetClicked)
        assertEquals(null, selectedItem)
        assertFalse(backEnabled)
        assertFalse(detailDisplayed)
    }
}
