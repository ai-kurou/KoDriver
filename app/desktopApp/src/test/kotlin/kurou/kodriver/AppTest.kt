package kurou.kodriver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.acewindowsdata.aceWindowsDataModule
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.feature.gt7ps5narrator.fakeGt7Ps5DataModule
import kurou.kodriver.feature.lmuwindowsnarrator.fakeLmuWindowsNarratorModule
import kurou.kodriver.feature.main.fakeMainModule
import kurou.kodriver.feature.otherconsoleipdetail.fakeOtherConsoleIpDetailModule
import kurou.kodriver.feature.otherreadoutstartsounddetail.fakeOtherReadoutStartSoundDetailModule
import kurou.kodriver.feature.otherthemedetail.fakeOtherThemeDetailModule
import kurou.kodriver.feature.readoutlist.fakeReadoutListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogRepository
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.featureModules
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.BeforeTest

class AppTest {
    companion object {
        private const val READOUT_PRIORITY_HELP_DESCRIPTION =
            "上位の項目は読み上げ中でも割り込みます。読み上げ中の同順位・下位の項目は無視されます"

        @BeforeClass
        @JvmStatic
        fun setUpKoin() {
            startKoin {
                // Koinは同一型のsingleが複数登録された場合、後から登録した方で上書きする。
                // featureModules（lmuWindowsNarratorModule/gt7Ps5NarratorModuleのincludes(platformSoundModule)
                // で本物のSoundPlayerを再バインドする）をFake群より後ろに置くと、Fakeが上書きされてしまう。
                // 必ずfeatureModulesを先に、Fake群を最後に登録すること。
                // :core:lmu-windows-data の lmuWindowsDataModule は含めない。LMU/GT7の各Repositoryは
                // fakeLmuWindowsNarratorModule / fakeGt7Ps5DataModule が最後に上書きするため実質未使用になる。
                modules(
                    listOf(desktopDataModule, aceWindowsDataModule) + featureModules +
                        listOf(
                            fakeGt7Ps5DataModule,
                            fakeLmuWindowsNarratorModule,
                            fakeReadoutListModule,
                            fakeTelemetryLogListModule,
                            fakeMainModule,
                            fakeOtherThemeDetailModule,
                            fakeOtherReadoutStartSoundDetailModule,
                            fakeOtherConsoleIpDetailModule,
                        ),
                )
            }
        }

        @AfterClass
        @JvmStatic
        fun tearDownKoin() {
            stopKoin()
        }
    }

    @get:Rule
    val rule = createComposeRule()

    @BeforeTest
    fun setUp() {
        fakeTelemetryLogRepository.clear()
    }

    @Test
    fun `LMU選択時に読み上げ項目を順にタップする`() {
        setContent()

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
        scrollToItem("ピットタイミング")
        clickItemAndVerifyDescription(
            "ピットタイミング",
            "ピットインの最適なタイミングが近づいたときに音声でお知らせします。\n" +
                "毎周ベストラップの30秒前に、燃料残量・タイヤ摩耗の予想残り周回数を判定し、" +
                "いずれかが閾値以下であれば、より緊急性の高い（予想残り周回数が少ない）方を1回だけ読み上げます。",
        )
        scrollToItem("バーチャルエナジー残量")
        clickItemAndVerifyDescription(
            "バーチャルエナジー残量",
            "バーチャルエナジー残量が設定した閾値以下になった場合に音声でお知らせします。",
        )
        scrollToItem("タイヤ摩耗")
        clickItemAndVerifyDescription(
            "タイヤ摩耗",
            "タイヤの摩耗率が設定した閾値以上になった場合に音声でお知らせします。いずれかのタイヤが条件を満たすと読み上げ、全タイヤが閾値未満に戻るまでは再度読み上げません。",
        )
        scrollToItem("車両故障")
        clickItemAndVerifyDescription("車両故障", "車両の故障状況を音声でお知らせします。")
        scrollToItem("自己ベストラップ")
        clickItemAndVerifyDescription("自己ベストラップ", "自己ベストラップを更新したときに音声でお知らせします。")
    }

    @Test
    fun `GT7選択時に読み上げ項目を順にタップする`() {
        setContent()

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
        setContent()

        selectSimulator("Assetto Corsa EVO（Windows版）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("燃料残量")
        clickItemAndVerifyDescription(
            "燃料残量",
            "残り燃料が設定した閾値を下回った場合に、音声でお知らせします。",
        )
        clickItemAndVerifyDescription(
            "フラッグ",
            "ホワイトフラッグ・グリーンフラッグ・レッドフラッグ・イエローフラッグなどのフラッグ状況を音声でお知らせします。",
        )
    }

    @Test
    fun `LMU選択時に接続状況バナーが表示される`() {
        setContent()

        selectSimulator("Le Mans Ultimate（Windows版）")
        waitUntilDisplayed("シミュレータ接続待機中")
        // Desktop ではサーバーIP設定への導線がないため、バナー表示のみ確認する。
    }

    @Test
    fun `ACE選択時に接続状況バナーが表示される`() {
        setContent()

        selectSimulator("Assetto Corsa EVO（Windows版）")
        waitUntilDisplayed("シミュレータ接続待機中")
        // Desktop ではサーバーIP設定への導線がないため、バナー表示のみ確認する。
    }

    @Test
    fun `GT7選択時に接続状況バナーをタップして戻る`() {
        setContent()

        selectSimulator("Gran Turismo 7（PS5）")
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
        clickItem("キャンセル")
        // 「画面をスリープさせない」は Desktop では表示されないため、AppTest では対象外。
        clickItem("テーマ")
        clickItem("キャンセル")
        scrollToItem("フィードバックを送信")
        clickItem("フィードバックを送信")
        scrollToItem("ライセンス")
        clickItem("ライセンス")
    }

    @Test
    fun `アプリバージョンを5回連続タップするとデバッグ状態画面へ遷移する`() {
        setContent()

        clickItem("その他")
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Windows版KoDriverバージョン"))
        repeat(5) { clickItem("Windows版KoDriverバージョン") }
        waitUntilDisplayed("デバッグ状態")
    }

    @Test
    fun `ログタブにログがない場合は空状態を表示する`() {
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
        setContent()

        clickItem("ログ")

        waitUntilDisplayed("フラッグ")
        waitUntilDisplayed("09:00:00.200 / レース +00:00:00.100")
        waitUntilDisplayed("イエローフラッグ")
        clickItem("フラッグ")
        waitUntilDisplayed("選択したログ")
        waitUntilDisplayed("一つ前のログ")
        waitUntilDisplayed("""{"flag":"yellow"}""")
    }

    @Test
    fun `ログタブを再タップするとログ一覧に戻る`() {
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
        setContent()

        clickItem("ログ")
        clickItem("フラッグ")
        waitUntilDisplayed("選択したログ")

        clickItem("ログ")

        waitUntilNotDisplayed("選択したログ")
        waitUntilDisplayed("フラッグ")
    }

    @Test
    fun `選択済みのログを再タップするとログ一覧に戻る`() {
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
        setContent()

        clickItem("ログ")
        clickItem("フラッグ")
        waitUntilDisplayed("選択したログ")

        clickItem("フラッグ")

        waitUntilNotDisplayed("選択したログ")
        waitUntilDisplayed("フラッグ")
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

    private fun waitUntilNotDisplayed(text: String) {
        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun clickItem(text: String) {
        rule.onNodeWithText(text).performClick()
        rule.waitForIdle()
    }

    private fun clickItemAndVerifyDescription(
        itemText: String,
        descriptionText: String,
    ) {
        clickItem(itemText)
        waitUntilDisplayed(descriptionText)
    }

    private fun scrollToItem(text: String) {
        rule.onAllNodes(hasScrollAction()).get(0).performScrollToNode(hasText(text))
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
    readoutItemKey: ReadoutItemKey,
    telemetryJson: String,
) = TelemetryLog(
    id = id,
    createdAt = createdAt,
    simulator = Simulator.LmuWindows,
    readoutItemKey = readoutItemKey,
    telemetryJson = telemetryJson,
)
