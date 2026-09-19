package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentTest {
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
    fun `詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        var backEnabled = false
        var githubRepositoryOpened = false
        var releasePageOpened = false
        var themeDialogOpened = false
        var overlayTextSizeDialogOpened = false
        var keepScreenOn = true
        var dynamicColorEnabled = false
        var capturedOnBack: (() -> Unit)? = null
        var selectedItem by mutableStateOf<OtherListItemType?>(null)

        rule.setContent {
            OtherContent(
                uiState =
                    OtherListUiState(
                        selectedItem = selectedItem,
                        keepScreenOn = keepScreenOn,
                        dynamicColorEnabled = dynamicColorEnabled,
                        items = OtherListUiState().items + OtherListItemType.DynamicColor,
                        appVersionLabel = "Windows版KoDriverバージョン",
                        appVersion = "1.2.3",
                    ),
                onItemSelected = { selectedItem = it },
                onOpenGitHubRepository = { githubRepositoryOpened = true },
                onOpenReleasePage = { releasePageOpened = true },
                onOpenThemeDialog = { themeDialogOpened = true },
                onOpenOverlayTextSizeDialog = { overlayTextSizeDialogOpened = true },
                onKeepScreenOnChange = { keepScreenOn = it },
                onDynamicColorEnabledChange = { dynamicColorEnabled = it },
                onAppVersionTapped = { selectedItem = OtherListItemType.DebugState },
                onClearSelectedItem = { selectedItem = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled: Boolean, _, onBack: () -> Unit ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
                detailContent = { item: OtherListItemType, _: Boolean, _: () -> Unit, _: Long?, _: Long ->
                    Text("Detail: ${item.id}")
                },
            )
        }

        assertFalse(backEnabled)

        // 一覧は項目数が表示領域を超えるため、クリック前に必ず対象までスクロールする。
        fun clickListItem(itemText: String) {
            rule.onNode(hasScrollAction()).performScrollToNode(hasText(itemText))
            rule.onNode(hasText(itemText)).performClick()
            rule.waitForIdle()
        }

        fun navigateToDetailAndBack(
            itemText: String,
            expectedDetailText: String,
        ) {
            clickListItem(itemText)

            rule.onNodeWithText(expectedDetailText).assertExists()
            assertTrue(backEnabled)

            rule.runOnIdle { capturedOnBack?.invoke() }
            rule.waitUntil { !backEnabled }
        }

        // ConsoleIp（Desktop では ServerIp・KeepScreenOn が除外されるため最初のアイテム）
        navigateToDetailAndBack("ゲーム機・SimHubへ接続するIPアドレス", "Detail: console_ip")

        // Volume
        navigateToDetailAndBack("音量", "Detail: volume")

        // OverlayBackgroundOpacity
        navigateToDetailAndBack("背景の透明度", "Detail: overlay_background_opacity")

        // ReadoutStartSound（ダイアログを開く）
        clickListItem("読み上げ開始音")

        assertFalse(backEnabled)

        // DynamicColor（Switchで直接切り替える）
        clickListItem("ダイナミックカラー")

        assertTrue(dynamicColorEnabled)
        assertFalse(backEnabled)

        // Theme（ダイアログを開く）
        clickListItem("テーマ")

        assertTrue(themeDialogOpened)
        assertFalse(backEnabled)

        // OverlayTextSize（ダイアログを開く）
        clickListItem("文字サイズ")

        assertTrue(overlayTextSizeDialogOpened)
        assertFalse(backEnabled)

        // GitHubRepository
        clickListItem("GitHubレポジトリ")

        assertTrue(githubRepositoryOpened)
        assertFalse(backEnabled)

        // ReleasePage
        clickListItem("リリースページ")

        assertTrue(releasePageOpened)
        assertFalse(backEnabled)

        // License（詳細あり）
        clickListItem("ライセンス")

        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)

        // アプリバージョンを5回連続タップ（onAppVersionTapped経由でDebugStateの詳細ペインへ遷移）
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Windows版KoDriverバージョン"))
        repeat(5) {
            rule.onNode(hasText("Windows版KoDriverバージョン")).performClick()
            rule.waitForIdle()
        }

        rule.onNodeWithText("Detail: debug_state").assertExists()
        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)
    }

    @Test
    fun `detailPane表示中にテーブルトップ姿勢になると選択解除コールバックを呼ぶ`() {
        var selectedItem by mutableStateOf<OtherListItemType?>(OtherListItemType.Volume)
        var windowPosture by mutableStateOf(Posture())
        var clearSelectedItemCallCount = 0

        rule.setContent {
            OtherContent(
                uiState = OtherListUiState(selectedItem = selectedItem),
                onItemSelected = { selectedItem = it },
                onClearSelectedItem = {
                    clearSelectedItemCallCount++
                    selectedItem = null
                },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                windowPosture = windowPosture,
                detailContent = { item: OtherListItemType, _: Boolean, _: () -> Unit, _: Long?, _: Long ->
                    Text("Detail: ${item.id}")
                },
            )
        }

        rule.runOnIdle { windowPosture = Posture(isTabletop = true) }

        rule.waitUntil { clearSelectedItemCallCount == 1 }
        assertEquals(null, selectedItem)
    }

    @Test
    fun `detailPane表示中に平らでない縦ヒンジの姿勢になると選択解除コールバックを呼ぶ`() {
        var selectedItem by mutableStateOf<OtherListItemType?>(OtherListItemType.Volume)
        var windowPosture by mutableStateOf(Posture())
        var clearSelectedItemCallCount = 0

        rule.setContent {
            OtherContent(
                uiState = OtherListUiState(selectedItem = selectedItem),
                onItemSelected = { selectedItem = it },
                onClearSelectedItem = {
                    clearSelectedItemCallCount++
                    selectedItem = null
                },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                windowPosture = windowPosture,
                detailContent = { item: OtherListItemType, _: Boolean, _: () -> Unit, _: Long?, _: Long ->
                    Text("Detail: ${item.id}")
                },
            )
        }

        rule.runOnIdle {
            windowPosture =
                Posture(
                    hingeList =
                        listOf(
                            HingeInfo(
                                bounds = Rect(left = 400f, top = 0f, right = 420f, bottom = 800f),
                                isFlat = false,
                                isVertical = true,
                                isSeparating = true,
                                isOccluding = false,
                            ),
                        ),
                )
        }

        rule.waitUntil { clearSelectedItemCallCount == 1 }
        assertEquals(null, selectedItem)
    }
}
