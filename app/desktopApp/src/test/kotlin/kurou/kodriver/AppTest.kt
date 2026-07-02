package kurou.kodriver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.gt7ps5data.gt7Ps5DataModule
import kurou.kodriver.core.lmuwindowsdata.lmuWindowsDataModule
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.feature.lmuwindowsnarrator.fakeLmuWindowsNarratorModule
import kurou.kodriver.feature.readoutlist.fakeReadoutListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogRepository
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.appModules
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class AppTest {

    companion object {
        private const val READOUT_PRIORITY_HELP_DESCRIPTION =
            "上位の項目は読み上げ中でも割り込みます。読み上げ中の同順位・下位の項目は無視されます"

        @BeforeClass @JvmStatic
        fun setUpKoin() {
            startKoin {
                modules(
                    listOf(
                        desktopDataModule,
                        lmuWindowsDataModule,
                        gt7Ps5DataModule,
                        fakeLmuWindowsNarratorModule,
                        fakeReadoutListModule,
                        fakeTelemetryLogListModule,
                    ) + appModules,
                )
            }
        }

        @AfterClass @JvmStatic
        fun tearDownKoin() {
            stopKoin()
        }
    }

    @get:Rule
    val rule = createComposeRule()

    @Before
    fun setUp() {
        fakeTelemetryLogRepository.clear()
    }

    @Test
    fun `LMU選択時に読み上げ項目を順にタップする`() {
        setContent()

        selectSimulator("Le Mans Ultimate（Windows版）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("フラッグ")
        clickItem("フラッグ")
        clickItem("車両接近")
        clickContentDescription("閾値の説明を表示")
        dismissBottomSheet()
        clickItem("車両故障")
        waitUntilDisplayed("オーバーヒート")
    }

    @Test
    fun `GT7選択時に読み上げ項目を順にタップする`() {
        setContent()

        selectSimulator("GranTurismo 7（PS5）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("燃料残り周回数")
        clickItem("燃料残り周回数")
        clickItem("自己ベストラップ")
    }

    @Test
    fun `LMU選択時に接続状況バナーが表示される`() {
        setContent()

        selectSimulator("Le Mans Ultimate（Windows版）")
        waitUntilDisplayed("シミュレータ接続待機中")
        // Desktop ではサーバーIP設定への導線がないため、バナー表示のみ確認する。
    }

    @Test
    fun `GT7選択時に接続状況バナーをタップして戻る`() {
        setContent()

        selectSimulator("GranTurismo 7（PS5）")
        waitUntilDisplayed("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        clickItem("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        clickItem("読み上げ")
    }

    @Test
    fun `その他タブの項目を順にタップする`() {
        setContent()

        clickItem("その他")
        clickItem("ゲーム機・SimHubへ接続するIPアドレス")
        clickItem("音量")
        clickItem("読み上げ開始音")
        // 「画面をスリープさせない」は Desktop では表示されないため、AppTest では対象外。
        clickItem("終了確認を表示")
        clickItem("キャンセル")
        clickItem("ライセンス")
    }

    @Test
    fun `ログタブを表示する`() {
        setContent()

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
        setContent()

        clickItem("ログ")

        waitUntilDisplayed("new_flag")
        waitUntilDisplayed("""{"flag":"green"}""")
        waitUntilDisplayed("lmu_windows / 200")
        waitUntilDisplayed("old_flag")
    }

    private fun selectSimulator(simulatorName: String) {
        rule.onNode(hasContentDescription("シミュレータを選択")).performClick()
        rule.waitForIdle()
        clickLastItem(simulatorName)
    }

    private fun setContent() {
        rule.setContent {
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreen()
            }
        }
    }

    private fun waitUntilDisplayed(text: String) {
        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun clickItem(text: String) {
        rule.onNodeWithText(text).performClick()
        rule.waitForIdle()
    }

    private fun clickContentDescription(contentDescription: String) {
        rule.onNode(hasContentDescription(contentDescription)).performClick()
        rule.waitForIdle()
    }

    private fun dismissBottomSheet() {
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            .get(0)
            .performSemanticsAction(SemanticsActions.Dismiss)
        rule.waitForIdle()
    }

    private fun clickReadoutPriorityHelp() {
        rule.onNode(hasContentDescription(READOUT_PRIORITY_HELP_DESCRIPTION)).performClick()
        rule.waitForIdle()
        rule.onAllNodes(isRoot()).get(0).performTouchInput { click(Offset(10f, 10f)) }
        rule.waitForIdle()
    }

    private fun clickLastItem(text: String) {
        val nodeIndex = rule.onAllNodes(hasText(text)).fetchSemanticsNodes().lastIndex
        rule.onAllNodes(hasText(text)).get(nodeIndex).performClick()
        rule.waitForIdle()
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
