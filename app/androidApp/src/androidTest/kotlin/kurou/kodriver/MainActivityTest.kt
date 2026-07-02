package kurou.kodriver

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogRepository
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        loadFakeTelemetryLogListModuleIfNeeded()
        fakeTelemetryLogRepository.clear()
    }

    @After
    fun tearDown() {
        scenario?.close()
    }

    @Test
    fun `LMU選択時に読み上げ項目を順にタップする`() {
        launchActivity()
        selectSimulator("Le Mans Ultimate（Windows版）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("フラッグ")
        clickItemAndNavigateBack("フラッグ")
        clickItem("車両接近")
        clickContentDescription("閾値の説明を表示")
        navigateBack()
        clickItemAndNavigateBack("車両故障")
    }

    @Test
    fun `GT7選択時に読み上げ項目を順にタップする`() {
        launchActivity()
        selectSimulator("GranTurismo 7（PS5）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("燃料残り周回数")
        clickItemAndNavigateBack("燃料残り周回数")
        clickItemAndNavigateBack("自己ベストラップ")
    }

    @Test
    fun `LMU選択時に接続状況バナーをタップして戻る`() {
        launchActivity()
        selectSimulator("Le Mans Ultimate（Windows版）")
        waitUntilDisplayed("Windows版KoDriverへ接続するIPアドレスが未設定です")
        clickItem("Windows版KoDriverへ接続するIPアドレスが未設定です")
        waitUntilDisplayed("Windows版KoDriverが動作しているPCのIPアドレスを入力してください。")
        navigateBack()
    }

    @Test
    fun `GT7選択時に接続状況バナーをタップして戻る`() {
        launchActivity()
        selectSimulator("GranTurismo 7（PS5）")
        waitUntilDisplayed("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        clickItem("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        waitUntilDisplayed("ゲーム機またはSimHubを実行しているPCのIPアドレスを入力してください。")
        navigateBack()
    }

    @Test
    fun `その他タブの項目を順にタップする`() {
        launchActivity()
        clickItem("その他")
        clickItemAndNavigateBack("Windows版KoDriverへ接続するIPアドレス")
        clickItemAndNavigateBack("ゲーム機・SimHubへ接続するIPアドレス")
        clickItemAndNavigateBack("音量")
        clickItem("画面をスリープさせない")
        clickItem("読み上げ開始音")
        clickItem("キャンセル")
        clickItem("終了確認を表示")
        clickItemAndNavigateBack("ライセンス")
    }

    @Test
    fun `ログタブにログがない場合は空状態を表示する`() {
        launchActivity()

        clickItem("ログ")
        waitUntilDisplayed("ログはまだありません")
        waitUntilDisplayed("テレメトリを受信すると、ここに新しい順で表示されます。")
    }

    @Test
    fun `ログタブにログがある場合は一覧を表示する`() {
        fakeTelemetryLogRepository.emit(
            listOf(
                telemetryLog(
                    id = 1,
                    createdAt = 100,
                    readoutItemKey = "old_flag",
                    telemetryJson = """{"flag":"yellow"}""",
                ),
                telemetryLog(
                    id = 2,
                    createdAt = 200,
                    readoutItemKey = "new_flag",
                    telemetryJson = """{"flag":"green"}""",
                ),
            ),
        )
        launchActivity()

        clickItem("ログ")

        waitUntilDisplayed("new_flag")
        waitUntilDisplayed("""{"flag":"green"}""")
        waitUntilDisplayed("lmu_windows / 200")
        waitUntilDisplayed("old_flag")
        clickItem("new_flag")
        waitUntilDisplayed("選択したログ")
        waitUntilDisplayed("一つ前のログ")
        waitUntilDisplayed("""{"flag":"yellow"}""")
    }

    private fun launchActivity() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeTestRule.waitForIdle()
    }

    private fun selectSimulator(simulatorName: String) {
        composeTestRule.onNode(hasContentDescription("シミュレータを選択")).performClick()
        composeTestRule.waitForIdle()
        clickLastItem(simulatorName)
    }

    private fun waitUntilDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickItemAndNavigateBack(text: String) {
        scrollToItem(text)
        clickItem(text)
        navigateBack()
    }

    private fun scrollToItem(text: String) {
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText(text))
        composeTestRule.waitForIdle()
    }

    private fun navigateBack() {
        if (composeTestRule.onAllNodes(hasContentDescription("戻る")).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNode(hasContentDescription("戻る")).performClick()
        } else {
            scenario?.onActivity { activity ->
                activity.onBackPressedDispatcher.onBackPressed()
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun clickItem(text: String) {
        composeTestRule.onNodeWithText(text).performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickLastItem(text: String) {
        val nodeIndex = composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().lastIndex
        composeTestRule.onAllNodes(hasText(text)).get(nodeIndex).performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickContentDescription(contentDescription: String) {
        composeTestRule.onNode(hasContentDescription(contentDescription)).performClick()
        composeTestRule.waitForIdle()
    }

    private fun clickReadoutPriorityHelp() {
        clickContentDescription(READOUT_PRIORITY_HELP_DESCRIPTION)
        // 実機では外側タップでボトムシートが閉じないことがあるため、dismissアクションを直接実行する。
        composeTestRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            .get(0)
            .performSemanticsAction(SemanticsActions.Dismiss)
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(READOUT_PRIORITY_HELP_DESCRIPTION)).fetchSemanticsNodes().isEmpty()
        }
    }

    private companion object {
        const val READOUT_PRIORITY_HELP_DESCRIPTION =
            "上位の項目は読み上げ中でも割り込みます。読み上げ中の同順位・下位の項目は無視されます"
        var isFakeTelemetryLogListModuleLoaded = false

        fun loadFakeTelemetryLogListModuleIfNeeded() {
            if (!isFakeTelemetryLogListModuleLoaded) {
                loadKoinModules(fakeTelemetryLogListModule)
                isFakeTelemetryLogListModuleLoaded = true
            }
        }
    }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
    readoutItemKey: String,
    telemetryJson: String,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulatorId = "lmu_windows",
    readoutItemKey = readoutItemKey,
    telemetryJson = telemetryJson,
)
