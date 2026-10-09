package kurou.kodriver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.acewindowsdata.aceWindowsDataModule
import kurou.kodriver.core.texttospeechdata.textToSpeechDataModule
import kurou.kodriver.data.desktopDataModule
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.feature.gt7ps5narrator.fakeGt7Ps5DataModule
import kurou.kodriver.feature.lmuwindowsnarrator.fakeLmuWindowsNarratorModule
import kurou.kodriver.feature.main.fakeMainModule
import kurou.kodriver.feature.otherconsoleipdetail.fakeOtherConsoleIpDetailModule
import kurou.kodriver.feature.otherlist.fakeOtherListModule
import kurou.kodriver.feature.otherreadoutstartsounddetail.fakeOtherReadoutStartSoundDetailModule
import kurou.kodriver.feature.otherthemedetail.fakeOtherThemeDetailModule
import kurou.kodriver.feature.othervoicedetail.fakeOtherVoiceDetailModule
import kurou.kodriver.feature.othervolumedetail.fakeOtherVolumeDetailModule
import kurou.kodriver.feature.readoutlist.fakeReadoutListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogListModule
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogRepository
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.NarratorOverlayScreen
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
            "一覧の上にある項目ほど優先度が高くなります。キューがOFFの項目は、読み上げ中の項目より優先度が高ければ割り込み、同じか低ければ読み上げを省略します。読み上げ中の項目がなければ、そのまま読み上げます。"

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
                    listOf(desktopDataModule, aceWindowsDataModule, textToSpeechDataModule) + featureModules +
                        listOf(
                            fakeGt7Ps5DataModule,
                            fakeLmuWindowsNarratorModule,
                            fakeReadoutListModule,
                            fakeTelemetryLogListModule,
                            fakeMainModule,
                            fakeOtherThemeDetailModule,
                            fakeOtherReadoutStartSoundDetailModule,
                            fakeOtherConsoleIpDetailModule,
                            fakeOtherVolumeDetailModule,
                            fakeOtherVoiceDetailModule,
                            fakeOtherListModule,
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
        scrollToItem("タイヤ温度")
        clickItemAndVerifyDescription(
            "タイヤ温度",
            "タイヤの温度状況を音声でお知らせします。判定にはカーカス温度を使用するため、ゲーム上に表示されるタイヤ温度とは若干の温度差が生じる場合があります。",
        )
        scrollToItem("ブレーキ温度")
        clickItemAndVerifyDescription(
            "ブレーキ温度",
            "ブレーキ温度が過熱した際に音声でお知らせします。" +
                "一度警告を読み上げた後は、4輪すべてが閾値より約100℃低い温度まで下がるまで再度読み上げません。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        scrollToItem("車両接近")
        clickItemAndVerifyDescription("車両接近", "周囲の車両が接近した際に音声でお知らせします。")
        scrollToItem("ピットタイミング")
        clickItemAndVerifyDescription(
            "ピットタイミング",
            "ピットインの最適なタイミングが近づいたときに音声でお知らせします。\n" +
                "毎周ベストラップの30秒前に、バーチャルエナジー残量・タイヤ摩耗の予想残り周回数を判定し、" +
                "いずれかが閾値以下であれば、より緊急性の高い（予想残り周回数が少ない）方を1回だけ読み上げます。\n" +
                "燃料残量はこの機能の判定対象に含みません。\n" +
                "読み上げる文言は、バーチャルエナジー・タイヤ摩耗それぞれ下の欄で設定できます。",
        )
        scrollToItem("バーチャルエナジー残量")
        clickItemAndVerifyDescription(
            "バーチャルエナジー残量",
            "バーチャルエナジー残量が設定した閾値以下になった場合に音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        scrollToItem("タイヤ摩耗")
        clickItemAndVerifyDescription(
            "タイヤ摩耗",
            "タイヤの残り（残存率）が設定した閾値以下になった場合に音声でお知らせします。いずれかのタイヤが条件を満たすと読み上げ、全タイヤが閾値を上回るまでは再度読み上げません。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        scrollToItem("車両故障")
        clickItemAndVerifyDescription(
            "車両故障",
            "車両の故障状況を音声でお知らせします。\n読み上げる文言は下の欄で設定できます。",
        )
        scrollToItem("自己ベストラップ")
        clickItemAndVerifyDescription(
            "自己ベストラップ",
            "自己ベストラップを更新したときに音声でお知らせします。\n読み上げる文言は下の欄で設定できます。",
        )
    }

    @Test
    fun `GT7選択時に読み上げ項目を順にタップする`() {
        setContent()

        selectSimulator("Gran Turismo 7（PS5）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("燃料残り周回数")
        clickItemAndVerifyDescription(
            "燃料残り周回数",
            "各ラップごとに燃料と走行可能な残り周回数を計算します。現在のベストラップの30秒前にあたるタイミングで判定し、" +
                "設定した周回数以下になると音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "燃料残量",
            "燃料残量が設定した閾値以下になった場合に、音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "タイヤ温度",
            "タイヤの温度状況を音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "自己ベストラップ",
            "自己ベストラップを更新したときに音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
    }

    @Test
    fun `ACE選択時に読み上げ項目を順にタップする`() {
        setContent()

        selectSimulator("Assetto Corsa EVO（Windows版）")
        clickReadoutPriorityHelp()

        waitUntilDisplayed("フラッグ")
        clickItemAndVerifyDescription(
            "フラッグ",
            "ホワイトフラッグ・グリーンフラッグ・レッドフラッグ・イエローフラッグなどのフラッグ状況を音声でお知らせします。",
        )
        clickItemAndVerifyDescription("車両接近", "周囲の車両が接近した際に音声でお知らせします。")
        scrollToItem("燃料残り周回数")
        clickItemAndVerifyDescription(
            "燃料残り周回数",
            "ACEが算出する残燃料で走行可能な周回数をもとに判定し、設定した周回数以下になると" +
                "1周減るごとに音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        scrollToItem("燃料残量")
        clickItemAndVerifyDescription(
            "燃料残量",
            "燃料残量が設定した閾値以下になった場合に、音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        scrollToItem("タイヤ温度")
        clickItemAndVerifyDescription(
            "タイヤ温度",
            "タイヤの温度状況を音声でお知らせします。判定にはカーカス温度を使用するため、ゲーム上に表示されるタイヤ温度とは若干の温度差が生じる場合があります。",
        )
        scrollToItem("自己ベストラップ")
        clickItemAndVerifyDescription(
            "自己ベストラップ",
            "自己ベストラップを更新したときに音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
    }

    @Test
    fun `LMU選択時に接続状況バナーが表示される`() {
        setContent()

        selectSimulator("Le Mans Ultimate（Windows版）")
        waitUntilDisplayed("シミュレーター接続待機中")
        // Desktop ではサーバーIP設定への導線がないため、バナー表示のみ確認する。
    }

    @Test
    fun `ACE選択時に接続状況バナーが表示される`() {
        setContent()

        selectSimulator("Assetto Corsa EVO（Windows版）")
        waitUntilDisplayed("シミュレーター接続待機中")
        // Desktop ではサーバーIP設定への導線がないため、バナー表示のみ確認する。
    }

    @Test
    fun `GT7選択時に接続状況バナーをタップして戻る`() {
        setContent()

        selectSimulator("Gran Turismo 7（PS5）")
        waitUntilDisplayed("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        clickItem("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        clickItem("ルール")
    }

    @Test
    fun `その他タブの項目を順にタップする`() {
        setContent()

        clickItem("その他")
        clickItem("ゲーム機・SimHubへ接続するIPアドレス")
        clickItem("音量")
        clickItem("読み上げ音声")
        waitUntilDisplayed("テスト音声")
        clickItem("テスト音声")
        rule
            .onNode(hasText("システム既定") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            .performClick()
        rule.waitForIdle()
        clickItem("読み上げ開始音")
        clickItem("キャンセル")
        // 「テレメトリ受信中は画面をスリープさせない」は Desktop では表示されないため、AppTest では対象外。
        scrollToItem("テーマ")
        clickItem("テーマ")
        clickItem("キャンセル")
        scrollToItem("オーバーレイを表示")
        clickItem("オーバーレイを表示")
        scrollToItem("文字サイズ")
        clickItem("文字サイズ")
        clickItem("キャンセル")
        scrollToItem("背景の透明度")
        clickItem("背景の透明度")
        scrollToItem("Windowsの日本語音声を設定")
        clickItem("Windowsの日本語音声を設定")
        scrollToItem("PC起動時に自動起動")
        clickItem("PC起動時に自動起動")
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

    @Test
    fun `NarratorOverlayScreenに最新の読み上げ内容が表示される`() {
        fakeTelemetryLogRepository.emit(
            listOf(
                telemetryLog(
                    id = 1,
                    createdAt = 100,
                    readoutItemKey = ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
                    telemetryJson = """{"flag":"yellow"}""",
                ),
            ),
        )

        rule.setContent {
            Box(modifier = Modifier.requiredSize(400.dp, 120.dp)) {
                NarratorOverlayScreen()
            }
        }

        waitUntilDisplayed("イエローフラッグ")
    }

    private fun selectSimulator(simulatorName: String) {
        rule.onNodeWithTag("primarySimulatorNavItem").performClick()
        rule.waitForIdle()
        clickLastItem(simulatorName)
    }

    private fun setContent() {
        rule.setContent {
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreen()
            }
        }
        waitUntilDisplayed("Windowsで日本語音声を利用できません")
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
        val node = rule.onAllNodes(hasText(text)).get(nodeIndex)
        node.performScrollTo()
        rule.waitForIdle()
        node.performClick()
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
    narratedText = "イエローフラッグ",
    narrationOutcome = NarrationOutcome.INTERRUPTED,
    telemetryJson = telemetryJson,
)
