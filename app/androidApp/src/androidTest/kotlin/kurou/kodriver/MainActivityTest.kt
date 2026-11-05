package kurou.kodriver

import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.feature.otherlist.fakeOtherListModule
import kurou.kodriver.feature.otherlist.fakeTextToSpeechRepository
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
        loadFakeOtherListModuleIfNeeded()
        fakeTextToSpeechRepository.unavailableReason = TextToSpeechUnavailableReason.WindowsSpeechUnavailable
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
        clickItemAndVerifyDescription(
            "ブレーキ温度",
            "ブレーキ温度が過熱した際に音声でお知らせします。" +
                "一度警告を読み上げた後は、4輪すべてが閾値より約100℃低い温度まで下がるまで再度読み上げません。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "ブレーキ摩耗",
            "ブレーキの残量が設定した閾値以下になった際に音声でお知らせします。" +
                "一度警告を読み上げた後は、ブレーキを交換して全輪の残量が閾値を超えるまで再度読み上げません。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription("車両接近", "周囲の車両が接近した際に音声でお知らせします。")
        clickItemAndVerifyDescription(
            "ピットタイミング",
            "ピットインの最適なタイミングが近づいたときに音声でお知らせします。\n" +
                "毎周ベストラップの30秒前に、バーチャルエネルギー残量・タイヤ摩耗・燃料残量の予想残り周回数を判定し、" +
                "いずれかが閾値以下であれば、より緊急性の高い（予想残り周回数が少ない）方を1回だけ読み上げます。\n" +
                "車両クラスに応じてバーチャルエネルギーまたは燃料を判定します。\n" +
                "読み上げる文言は、バーチャルエネルギー・タイヤ摩耗・燃料それぞれ下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "バーチャルエネルギー残量",
            "バーチャルエネルギー残量が設定した閾値以下になった場合に音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "タイヤ摩耗",
            "タイヤの残り（残存率）が設定した閾値以下になった場合に音声でお知らせします。いずれかのタイヤが条件を満たすと読み上げ、全タイヤが閾値を上回るまでは再度読み上げません。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "車両故障",
            "車両の故障状況を音声でお知らせします。\n読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "自己ベストラップ",
            "自己ベストラップを更新したときに音声でお知らせします。\n読み上げる文言は下の欄で設定できます。",
        )
    }

    @Test
    fun `GT7選択時に読み上げ項目を順にタップする`() {
        launchActivity()
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
            "燃料残り周回数",
            "ACEが算出する残燃料で走行可能な周回数をもとに判定し、設定した周回数以下になると" +
                "1周減るごとに音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "燃料残量",
            "燃料残量が設定した閾値以下になった場合に、音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
        clickItemAndVerifyDescription(
            "タイヤ温度",
            "タイヤの温度状況を音声でお知らせします。判定にはカーカス温度を使用するため、ゲーム上に表示されるタイヤ温度とは若干の温度差が生じる場合があります。",
        )
        clickItemAndVerifyDescription(
            "自己ベストラップ",
            "自己ベストラップを更新したときに音声でお知らせします。\n" +
                "読み上げる文言は下の欄で設定できます。",
        )
    }

    @Test
    fun `LMU選択時に接続状況バナーをタップして戻る`() {
        launchActivity()
        selectSimulator("Le Mans Ultimate（Windows版）")
        waitUntilDisplayed("Windows版KoDriverへ接続するIPアドレスが未設定です")
        composeTestRule.onNodeWithText("Windows版KoDriverへ接続するIPアドレスが未設定です").assertHasClickAction()
        clickItem("Windows版KoDriverへ接続するIPアドレスが未設定です")
        waitUntilDisplayed("が動作しているPCのIPアドレスを入力してください。", substring = true)
        navigateBack()
    }

    @Test
    fun `ACE選択時に接続状況バナーをタップして戻る`() {
        launchActivity()
        selectSimulator("Assetto Corsa EVO（Windows版）")
        waitUntilDisplayed("Windows版KoDriverへ接続するIPアドレスが未設定です")
        composeTestRule.onNodeWithText("Windows版KoDriverへ接続するIPアドレスが未設定です").assertHasClickAction()
        clickItem("Windows版KoDriverへ接続するIPアドレスが未設定です")
        waitUntilDisplayed("が動作しているPCのIPアドレスを入力してください。", substring = true)
        navigateBack()
    }

    @Test
    fun `GT7選択時に接続状況バナーをタップして戻る`() {
        launchActivity()
        selectSimulator("Gran Turismo 7（PS5）")
        waitUntilDisplayed("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        composeTestRule.onNodeWithText("ゲーム機・SimHubへ接続するIPアドレスが未設定です").assertHasClickAction()
        clickItem("ゲーム機・SimHubへ接続するIPアドレスが未設定です")
        waitUntilDisplayed("を実行しているPCのIPアドレスを入力してください。", substring = true)
        navigateBack()
    }

    @Test
    fun `その他タブの項目を順にタップする`() {
        launchActivity()
        clickItem("その他")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            // アプリ外（システム設定）へ遷移するため、タップによるIntent起動は feature:other-list のRobolectricテストで検証する。
            scrollToItem("ローカルネットワークへのアクセス許可")
            composeTestRule.onNodeWithText("ローカルネットワークへのアクセス許可").assertExists()
        }
        clickItemAndNavigateBack("Windows版KoDriverへ接続するIPアドレス")
        clickItemAndNavigateBack("ゲーム機・SimHubへ接続するIPアドレス")
        clickItemAndNavigateBack("音量")
        clickItemAndNavigateBack("読み上げ音声")
        clickScrollableItem("読み上げ速度")
        waitUntilDisplayed("読み上げの速さを設定します。")
        navigateBack()
        clickScrollableItem("声の高さ")
        waitUntilDisplayed("読み上げ音声の高さを設定します。")
        navigateBack()
        clickScrollableItem("音声の用途")
        waitUntilDisplayed("読み上げ音声を、メディアや通話などどの用途の音声として扱うか設定します。右の再生ボタンで、その用途で試聴できます。")
        navigateBack()
        clickScrollableItem("読み上げ開始音")
        clickItem("キャンセル")
        clickScrollableItem("テレメトリ受信中は画面をスリープさせない")
        clickScrollableItem("テーマ")
        clickItem("キャンセル")
        // ダイナミックカラーは Android 12（API 31）以上でのみ一覧に表示される。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            clickScrollableItem("ダイナミックカラー")
        }
        clickScrollableItem("ハプティックフィードバック")
        clickScrollableItem("自動タブの設定を初期化")
        clickItem("キャンセル")
        clickScrollableItem("手動タブの設定を初期化")
        clickItem("キャンセル")
        clickScrollableItem("ログを全て削除")
        clickItem("キャンセル")
        clickItemAndNavigateBack("フィードバックを送信")
        clickItemAndNavigateBack("ライセンス")
    }

    @Test
    fun `TTSエンジン未インストール時にインストール案内項目が表示される`() {
        fakeTextToSpeechRepository.unavailableReason = TextToSpeechUnavailableReason.EngineMissing

        launchActivity()
        clickItem("その他")

        scrollToItem("音声合成アプリをインストール")
        waitUntilDisplayed("音声合成アプリをインストール")
    }

    @Test
    fun `TTS日本語データ未インストール時に設定案内項目が表示される`() {
        fakeTextToSpeechRepository.unavailableReason = TextToSpeechUnavailableReason.LanguageDataMissing

        launchActivity()
        clickItem("その他")

        scrollToItem("音声合成の日本語データを設定")
        waitUntilDisplayed("音声合成の日本語データを設定")
    }

    @Test
    fun `LMU選択時にアプリバージョンを5回連続タップするとデバッグ状態画面へ遷移する`() {
        assertDebugStateScreen("Le Mans Ultimate（Windows版）", "ゲームフェーズ (LMUのみ)")
    }

    @Test
    fun `GT7選択時にアプリバージョンを5回連続タップするとデバッグ状態画面へ遷移する`() {
        assertDebugStateScreen("Gran Turismo 7（PS5）", "車両クラス")
    }

    @Test
    fun `ACE選択時にアプリバージョンを5回連続タップするとデバッグ状態画面へ遷移する`() {
        assertDebugStateScreen("Assetto Corsa EVO（Windows版）", "車両位置")
    }

    @Test
    fun `LMUの手動タブにセットがない場合は空状態を表示する`() {
        launchActivity()
        selectSimulator("Le Mans Ultimate（Windows版）")

        clickItem("手動")
        waitUntilDisplayed("LMUの手動読み上げセットはまだありません")
        waitUntilDisplayed("手動読み上げについて")
    }

    @Test
    fun `GT7の手動タブにセットがない場合は空状態を表示する`() {
        assertManualReadoutEmptyState("Gran Turismo 7（PS5）", "GT7")
    }

    @Test
    fun `ACEの手動タブにセットがない場合は空状態を表示する`() {
        assertManualReadoutEmptyState("Assetto Corsa EVO（Windows版）", "ACE")
    }

    private fun assertManualReadoutEmptyState(
        simulatorDisplayName: String,
        simulatorShortName: String,
    ) {
        launchActivity()
        selectSimulator(simulatorDisplayName)
        waitUntilDisplayed("手動")
        clickItem("手動")
        waitUntilDisplayed("${simulatorShortName}の手動読み上げセットはまだありません")
        waitUntilDisplayed("手動読み上げについて")
    }

    private fun assertDebugStateScreen(
        simulatorName: String,
        simulatorSpecificText: String,
    ) {
        launchActivity()
        selectSimulator(simulatorName)
        clickItem("その他")
        scrollToItem("Android版KoDriverバージョン")
        repeat(5) { clickItem("Android版KoDriverバージョン") }
        waitUntilDisplayed("デバッグ状態")
        // 各Paneに固定されたシミュレーター名をグリッド内で探し、ナビ項目だけで成功するのを防ぐ。
        // 背面の「その他」一覧もスクロール可能なため、グリッドをタグで特定してスクロールする。
        composeTestRule
            .onNodeWithTag("debug_state_grid")
            .performScrollToNode(hasText(simulatorName))
        composeTestRule.waitForIdle()
        waitUntilDisplayed(simulatorName)
        listOf(
            "Le Mans Ultimate（Windows版）",
            "Gran Turismo 7（PS5）",
            "Assetto Corsa EVO（Windows版）",
        ).filter { it != simulatorName }.forEach { waitUntilNotDisplayed(it) }
        composeTestRule
            .onNodeWithTag("debug_state_grid")
            .performScrollToNode(hasText(simulatorSpecificText))
        composeTestRule.waitForIdle()
        waitUntilDisplayed(simulatorSpecificText)
        navigateBack()
    }

    private fun launchActivity() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeTestRule.waitForIdle()
        val title =
            when (fakeTextToSpeechRepository.unavailableReason) {
                TextToSpeechUnavailableReason.EngineMissing -> "音声読み上げを利用できません"
                TextToSpeechUnavailableReason.LanguageDataMissing -> "日本語の音声データがありません"
                TextToSpeechUnavailableReason.WindowsSpeechUnavailable -> "Windowsで日本語音声を利用できません"
            }
        waitUntilDisplayed(title)
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

    private fun waitUntilNotDisplayed(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 8_000L) {
            composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isEmpty()
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
        // スクロール直後の項目は一覧上部の「先頭へ戻る」ボタンの下に重なることがあり、
        // 座標でタップするとボタンに奪われるため、セマンティクスの OnClick で実行する。
        composeTestRule
            .onNode(hasContentDescription(text))
            .performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()
    }

    private fun clickLastItem(text: String) {
        val nodeIndex = composeTestRule.onAllNodes(hasText(text)).fetchSemanticsNodes().lastIndex
        val node = composeTestRule.onAllNodes(hasText(text)).get(nodeIndex)
        node.performScrollTo()
        composeTestRule.waitForIdle()
        node.performClick()
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
            "一覧の上にある項目ほど優先度が高くなります。キューがOFFの項目は、読み上げ中の項目より優先度が高ければ割り込み、同じか低ければ読み上げを省略します。読み上げ中の項目がなければ、そのまま読み上げます。"
        var isFakeOtherListModuleLoaded = false

        fun loadFakeOtherListModuleIfNeeded() {
            if (!isFakeOtherListModuleLoaded) {
                loadKoinModules(fakeOtherListModule)
                isFakeOtherListModuleLoaded = true
            }
        }
    }
}
