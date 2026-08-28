package kurou.kodriver

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.feature.otherlist.fakeOtherListModule
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
        loadFakeOtherListModuleIfNeeded()
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
        clickItemAndVerifyDescription(
            "フラッグ",
            "ブルーフラッグ・イエローフラッグ・レッドフラッグ・フルコースイエローなどのフラッグ状況を音声でお知らせします。",
        )
        clickItemAndVerifyDescription(
            "タイヤ温度",
            "タイヤの温度状況を音声でお知らせします。判定にはカーカス温度を使用するため、ゲーム上に表示されるタイヤ温度とは若干の温度差が生じる場合があります。",
        )
        clickItemAndVerifyDescription("車両接近", "周囲の車両が接近した際に音声でお知らせします。")
        clickItemAndVerifyDescription(
            "ピットタイミング",
            "ピットインの最適なタイミングが近づいたときに音声でお知らせします。\n" +
                "毎周ベストラップの30秒前に、燃料残量・タイヤ摩耗の予想残り周回数を判定し、" +
                "いずれかが閾値以下であれば、より緊急性の高い（予想残り周回数が少ない）方を1回だけ読み上げます。",
        )
        clickItemAndVerifyDescription(
            "バーチャルエナジー残量",
            "バーチャルエナジー残量が設定した閾値以下になった場合に音声でお知らせします。",
        )
        clickItemAndVerifyDescription(
            "タイヤ摩耗",
            "タイヤの摩耗率が設定した閾値以上になった場合に音声でお知らせします。いずれかのタイヤが条件を満たすと読み上げ、全タイヤが閾値未満に戻るまでは再度読み上げません。",
        )
        clickItemAndVerifyDescription("車両故障", "車両の故障状況を音声でお知らせします。")
        clickItemAndVerifyDescription("自己ベストラップ", "自己ベストラップを更新したときに音声でお知らせします。")
    }

    @Test
    fun `GT7選択時に読み上げ項目を順にタップする`() {
        launchActivity()
        selectSimulator("Gran Turismo 7（PS5）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("燃料残り周回数")
        clickItemAndVerifyDescription(
            "燃料残り周回数",
            "各ラップごとに燃料と走行可能な残り周回数を計算します。現在の最速ラップの30秒前にあたるタイミングで判定し、" +
                "設定した周回数以下になると音声でお知らせします。",
        )
        clickItemAndVerifyDescription("燃料残量", "残り燃料が設定した閾値を下回った場合に、音声でお知らせします。")
        clickItemAndVerifyDescription("自己ベストラップ", "自己ベストラップを更新したときに音声でお知らせします。")
    }

    @Test
    fun `ACE選択時に読み上げ項目を順にタップする`() {
        launchActivity()
        selectSimulator("Assetto Corsa EVO（Windows版）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("フラッグ")
        clickItemAndVerifyDescription(
            "フラッグ",
            "ホワイトフラッグ・グリーンフラッグ・レッドフラッグ・イエローフラッグなどのフラッグ状況を音声でお知らせします。",
        )
        clickItemAndVerifyDescription("車両接近", "周囲の車両が接近した際に音声でお知らせします。")
        clickItemAndVerifyDescription(
            "タイヤ温度",
            "タイヤの温度状況を音声でお知らせします。判定にはカーカス温度を使用するため、ゲーム上に表示されるタイヤ温度とは若干の温度差が生じる場合があります。",
        )
        clickItemAndVerifyDescription(
            "燃料残量",
            "残り燃料が設定した閾値を下回った場合に、音声でお知らせします。",
        )
        clickItemAndVerifyDescription("自己ベストラップ", "自己ベストラップを更新した場合に、音声でお知らせします。")
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
    fun `ACE選択時に接続状況バナーをタップして戻る`() {
        launchActivity()
        selectSimulator("Assetto Corsa EVO（Windows版）")
        waitUntilDisplayed("Windows版KoDriverへ接続するIPアドレスが未設定です")
        clickItem("Windows版KoDriverへ接続するIPアドレスが未設定です")
        waitUntilDisplayed("Windows版KoDriverが動作しているPCのIPアドレスを入力してください。")
        navigateBack()
    }

    @Test
    fun `GT7選択時に接続状況バナーをタップして戻る`() {
        launchActivity()
        selectSimulator("Gran Turismo 7（PS5）")
        waitUntilDisplayed("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        clickItem("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        waitUntilDisplayed("を実行しているPCのIPアドレスを入力してください。", substring = true)
        navigateBack()
    }

    @Test
    fun `その他タブの項目を順にタップする`() {
        launchActivity()
        clickItem("その他")
        clickItemAndNavigateBack("Windows版KoDriverへ接続するIPアドレス")
        clickItemAndNavigateBack("ゲーム機・SimHubへ接続するIPアドレス")
        clickItemAndNavigateBack("音量")
        clickScrollableItem("読み上げ開始音")
        clickItem("キャンセル")
        clickScrollableItem("テレメトリ受信中は画面をスリープさせない")
        clickScrollableItem("テーマ")
        clickItem("キャンセル")
        clickScrollableItem("ダイナミックカラー")
        clickScrollableItem("ハプティックフィードバック")
        clickItemAndNavigateBack("フィードバックを送信")
        clickItemAndNavigateBack("ライセンス")
    }

    @Test
    fun `アプリバージョンを5回連続タップするとデバッグ状態画面へ遷移する`() {
        launchActivity()
        clickItem("その他")
        scrollToItem("Android版KoDriverバージョン")
        repeat(5) { clickItem("Android版KoDriverバージョン") }
        waitUntilDisplayed("デバッグ状態")
        navigateBack()
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
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
                    telemetryJson = """{"flag":"yellow"}""",
                ),
                telemetryLog(
                    id = 2,
                    createdAt = 200,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.Root,
                    telemetryJson = """{"flag":"green"}""",
                ),
            ),
        )
        launchActivity()

        clickItem("ログ")

        waitUntilDisplayed("フラッグ")
        waitUntilDisplayed("09:00:00.200 / レース +00:00:00.100")
        waitUntilDisplayed("イエローフラッグ")
        clickItem("フラッグ")
        waitUntilDisplayed("選択したログ")
        waitUntilDisplayed("一つ前のログ")
        waitUntilDisplayed("""{"flag":"yellow"}""")
    }

    private fun launchActivity() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeTestRule.waitForIdle()
    }

    private fun selectSimulator(simulatorName: String) {
        composeTestRule.onNodeWithTag("primarySimulatorNavItem").performClick()
        composeTestRule.waitForIdle()
        clickLastItem(simulatorName)
    }

    private fun waitUntilDisplayed(
        text: String,
        substring: Boolean = false,
    ) {
        // CI実機エミュレータではボトムシートの閉じるアニメーション等が遅く、
        // 5秒では不足してタイムアウトすることがあるため、実機テストのみ余裕を持たせる。
        composeTestRule.waitUntil(timeoutMillis = 8_000L) {
            composeTestRule.onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickItemAndNavigateBack(text: String) {
        scrollToItem(text)
        clickItem(text)
        navigateBack()
    }

    private fun clickItemAndVerifyDescription(
        itemText: String,
        descriptionText: String,
    ) {
        scrollToItem(itemText)
        clickReadoutItem(itemText)
        waitUntilDisplayed(descriptionText)
        navigateBack()
    }

    private fun clickScrollableItem(text: String) {
        scrollToItem(text)
        clickItem(text)
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

    private fun clickReadoutItem(text: String) {
        composeTestRule.onNode(hasContentDescription(text)).performClick()
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

    private fun dismissBottomSheet() {
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule
                .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            .get(0)
            .performSemanticsAction(SemanticsActions.Dismiss)
        composeTestRule.waitForIdle()
    }

    private fun clickReadoutPriorityHelp() {
        clickContentDescription(READOUT_PRIORITY_HELP_DESCRIPTION)
        // 実機では外側タップでボトムシートが閉じないことがあるため、dismissアクションを直接実行する。
        dismissBottomSheet()
        composeTestRule.waitUntil(timeoutMillis = 5_000L) {
            composeTestRule.onAllNodes(hasText(READOUT_PRIORITY_HELP_DESCRIPTION)).fetchSemanticsNodes().isEmpty()
        }
    }

    private companion object {
        const val READOUT_PRIORITY_HELP_DESCRIPTION =
            "上位の項目は読み上げ中でも割り込みます。読み上げ中の同順位・下位の項目は無視されます"
        var isFakeTelemetryLogListModuleLoaded = false
        var isFakeOtherListModuleLoaded = false

        fun loadFakeTelemetryLogListModuleIfNeeded() {
            if (!isFakeTelemetryLogListModuleLoaded) {
                loadKoinModules(fakeTelemetryLogListModule)
                isFakeTelemetryLogListModuleLoaded = true
            }
        }

        fun loadFakeOtherListModuleIfNeeded() {
            if (!isFakeOtherListModuleLoaded) {
                loadKoinModules(fakeOtherListModule)
                isFakeOtherListModuleLoaded = true
            }
        }
    }
}

private fun telemetryLog(
    id: Long,
    createdAt: Long,
    readoutItemKey: ReadoutItemKey,
    telemetryJson: String,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulator = Simulator.LmuWindows,
    readoutItemKey = readoutItemKey,
    telemetryJson = telemetryJson,
)
