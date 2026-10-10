package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.NarrationOutcome
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class TelemetryLogContentTest {
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
    fun `一覧ペインにログを表示する`() {
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState,
            )
        }

        rule.onNodeWithText("フラッグ").assertExists()
        rule.onNodeWithText("燃料残り周回数").assertExists()
        rule.onNodeWithText("09:30:20.000 / レース +00:00:20.000").assertExists()
        val telemetryJson = """{"flag":"green","sector1":"clear","sector2":"clear","sector3":"clear"}"""
        rule.onNodeWithText(telemetryJson).assertDoesNotExist()
    }

    @Test
    fun `ace_windowsのログもアイコン付きで一覧に表示する`() {
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState =
                    TelemetryLogListUiState(
                        logs =
                            listOf(
                                TelemetryLog(
                                    id = 1,
                                    createdAt = 1_800_000,
                                    simulator = Simulator.AceWindows,
                                    readoutItemKey = AceWindowsReadoutItemKey.Flag.Root,
                                    narratedText = "イエローフラッグ",
                                    narrationOutcome = NarrationOutcome.INTERRUPTED,
                                    telemetryJson = """{"flag":"green"}""",
                                ),
                            ),
                    ),
            )
        }

        rule.onNodeWithText("フラッグ").assertExists()
    }

    @Test
    fun `ログが0件の場合は空状態を表示する`() {
        rule.setContent {
            TelemetryLogContentScaffold()
        }

        rule.onNodeWithText("ログはまだありません").assertExists()
        rule.onNodeWithText("テレメトリを受信すると、ここに新しい順で表示されます。").assertExists()
    }

    @Test
    fun `ログをタップするとdetailPaneにログIDを渡す`() {
        rule.setContent {
            var selectedLogId by remember { mutableStateOf<Long?>(null) }
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(selectedLogId = selectedLogId),
                onLogSelected = { selectedLogId = it },
                detailContent = { id ->
                    Text("selected: $id")
                },
            )
        }

        rule.onNodeWithText("フラッグ").performClick()

        rule.onNodeWithText("selected: 2").assertExists()
    }

    @Test
    fun `詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        var backEnabled = false
        var capturedOnBack: (() -> Unit)? = null

        rule.setContent {
            var selectedLogId by remember { mutableStateOf<Long?>(null) }
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(selectedLogId = selectedLogId),
                onLogSelected = { selectedLogId = it },
                onClearSelectedLog = { selectedLogId = null },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                backHandler = { enabled, _, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
                detailContent = { id ->
                    Text("selected: $id")
                },
            )
        }

        assertFalse(backEnabled)

        rule.onNodeWithText("フラッグ").performClick()
        rule.waitUntil { backEnabled }
        rule.onNodeWithText("selected: 2").assertExists()
        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitUntil { !backEnabled }

        assertFalse(backEnabled)
        rule.onNodeWithText("フラッグ").assertExists()
    }

    @Test
    fun `detailPane表示中にテーブルトップ姿勢になると選択解除コールバックを呼ぶ`() {
        var windowPosture by mutableStateOf(Posture())
        var clearSelectedLogCallCount = 0

        rule.setContent {
            var selectedLogId by remember { mutableStateOf<Long?>(null) }
            LaunchedEffect(Unit) { selectedLogId = 2L }
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(selectedLogId = selectedLogId),
                onLogSelected = { selectedLogId = it },
                onClearSelectedLog = {
                    clearSelectedLogCallCount++
                    selectedLogId = null
                },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                windowPosture = windowPosture,
                detailContent = { id -> Text("selected: $id") },
            )
        }

        rule.onNodeWithText("selected: 2").assertExists()

        rule.runOnIdle { windowPosture = Posture(isTabletop = true) }

        rule.waitUntil { clearSelectedLogCallCount == 1 }
        rule.onNodeWithText("フラッグ").assertExists()
    }

    @Test
    fun `detailPane表示中に平らでない縦ヒンジの姿勢になると選択解除コールバックを呼ぶ`() {
        var windowPosture by mutableStateOf(Posture())
        var clearSelectedLogCallCount = 0

        rule.setContent {
            var selectedLogId by remember { mutableStateOf<Long?>(null) }
            LaunchedEffect(Unit) { selectedLogId = 2L }
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(selectedLogId = selectedLogId),
                onLogSelected = { selectedLogId = it },
                onClearSelectedLog = {
                    clearSelectedLogCallCount++
                    selectedLogId = null
                },
                scaffoldDirective = singlePaneDirective,
                windowSizeClass = compactWindowSizeClass,
                windowPosture = windowPosture,
                detailContent = { id -> Text("selected: $id") },
            )
        }

        rule.onNodeWithText("selected: 2").assertExists()

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

        rule.waitUntil { clearSelectedLogCallCount == 1 }
        rule.onNodeWithText("フラッグ").assertExists()
    }

    @Test
    fun `readoutItemDisplayNameは既知の読み上げ項目IDを日本語名に変換する`() {
        val expectedDisplayNames =
            listOf(
                LmuWindowsReadoutItemKey.VehicleApproach.Root to "車両接近",
                LmuWindowsReadoutItemKey.Flag.Root to "フラッグ",
                LmuWindowsReadoutItemKey.Flag.BlueFlag to "ブルーフラッグ",
                LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to "イエローフラッグ",
                LmuWindowsReadoutItemKey.Flag.FullCourseYellow to "フルコースイエロー",
                LmuWindowsReadoutItemKey.Flag.RedFlag to "レッドフラッグ",
                LmuWindowsReadoutItemKey.VehicleDamage.Root to "車両故障",
                LmuWindowsReadoutItemKey.VehicleDamage.Overheat to "オーバーヒート",
                LmuWindowsReadoutItemKey.TyreTemperature.Root to "タイヤ温度",
                LmuWindowsReadoutItemKey.PitTiming.Root to "ピットタイミング",
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to "バーチャルエナジー残量",
                LmuWindowsReadoutItemKey.TyreWear.Root to "タイヤ摩耗",
                LmuWindowsReadoutItemKey.MyBestLap.Root to "自己ベストラップ",
                Gt7Ps5ReadoutItemKey.MyBestLap.Root to "自己ベストラップ",
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to "燃料残り周回数",
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root to "燃料残量",
                AceWindowsReadoutItemKey.VehicleApproach.Root to "車両接近",
                AceWindowsReadoutItemKey.Flag.Root to "フラッグ",
                AceWindowsReadoutItemKey.Flag.BlueFlag to "ブルーフラッグ",
                AceWindowsReadoutItemKey.Flag.RedFlag to "レッドフラッグ",
                AceWindowsReadoutItemKey.RemainingFuel.Root to "燃料残量",
                AceWindowsReadoutItemKey.MyBestLap.Root to "自己ベストラップ",
                AceWindowsReadoutItemKey.TyreTemperature.Root to "タイヤ温度",
            )

        rule.setContent {
            expectedDisplayNames.forEach { (readoutItemKey, _) ->
                Text(readoutItemDisplayName(readoutItemKey))
            }
        }

        expectedDisplayNames.groupingBy { it.second }.eachCount().forEach { (displayName, count) ->
            rule.onAllNodesWithText(displayName).assertCountEquals(count)
        }
    }

    @Test
    fun `データベースをリセット項目をタップすると確認ダイアログを表示する`() {
        var clicked = false
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState,
                onResetClick = { clicked = true },
            )
        }

        rule.onNodeWithText("データベースをリセット").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `リセット実行中はデータベースをリセット項目のタップを無効化する`() {
        var clicked = false
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(isResetting = true),
                onResetClick = { clicked = true },
            )
        }

        rule.onNodeWithText("データベースをリセット").performClick()

        assertFalse(clicked)
    }

    @Test
    fun `ログが0件の場合はデータベースをリセット項目を表示しない`() {
        rule.setContent {
            TelemetryLogContentScaffold()
        }

        rule.onNodeWithText("データベースをリセット").assertDoesNotExist()
    }

    @Test
    fun `確認ダイアログの実行ボタンをタップするとonResetConfirmを呼ぶ`() {
        var confirmed = false
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(showResetConfirmDialog = true),
                onResetConfirm = { confirmed = true },
            )
        }

        rule.onNodeWithText("削除する").performClick()

        assertTrue(confirmed)
    }

    @Test
    fun `確認ダイアログのキャンセルボタンをタップするとonResetDismissを呼ぶ`() {
        var dismissed = false
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(showResetConfirmDialog = true),
                onResetDismiss = { dismissed = true },
            )
        }

        rule.onNodeWithText("キャンセル").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun `resetSucceededがtrueになるとスナックバーで成功を通知しconsumeする`() {
        var consumed = false
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(resetSucceeded = true),
                onResetResultConsumed = { consumed = true },
            )
        }

        rule.onNodeWithText("データベースをリセットしました").assertExists()
        rule.waitUntil(timeoutMillis = 5_000L) { consumed }
    }

    @Test
    fun `resetSucceededがfalseになるとスナックバーで失敗を通知する`() {
        rule.setContent {
            TelemetryLogContentScaffold(
                uiState = previewTelemetryLogListUiState.copy(resetSucceeded = false),
            )
        }

        rule.onNodeWithText("データベースのリセットに失敗しました").assertExists()
    }

    @Test
    fun `先頭から離れているときに新しいログが追加されると先頭へ戻るボタンを表示する`() {
        val logs = mutableStateOf(createTelemetryLogs())

        rule.setContent {
            TelemetryLogListPane(
                uiState = TelemetryLogListUiState(logs = logs.value),
            )
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("オーバーヒート"))
        rule.runOnIdle {
            logs.value = listOf(createTelemetryLog(id = 100, readoutItemKey = LmuWindowsReadoutItemKey.Flag.RedFlag)) +
                logs.value
        }

        rule.onNodeWithText("新しいログ").assertExists()

        rule.onNodeWithText("新しいログ").performClick()

        rule.onNodeWithText("レッドフラッグ").assertExists()
    }

    @Test
    fun `scrollToTopRequestが増えるとリストを先頭へ戻す`() {
        var scrollToTopRequest by mutableIntStateOf(0)
        val logs = createTelemetryLogs()

        rule.setContent {
            Box(modifier = Modifier.height(240.dp)) {
                TelemetryLogListPane(
                    uiState = TelemetryLogListUiState(logs = logs),
                    scrollToTopRequest = scrollToTopRequest,
                )
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("オーバーヒート"))
        rule.runOnIdle { scrollToTopRequest++ }

        rule.waitUntil {
            rule.onAllNodesWithText("タイヤ摩耗").fetchSemanticsNodes().isNotEmpty()
        }
    }
}

private fun createTelemetryLogs(): List<TelemetryLog> =
    (30 downTo 1).map { id ->
        createTelemetryLog(
            id = id.toLong(),
            simulator = if (id == 10) Simulator.AceWindows else Simulator.LmuWindows,
            readoutItemKey =
                when (id) {
                    30 -> LmuWindowsReadoutItemKey.TyreWear.Root
                    20 -> LmuWindowsReadoutItemKey.VehicleDamage.Overheat
                    10 -> AceWindowsReadoutItemKey.RemainingFuel.Root
                    else -> LmuWindowsReadoutItemKey.Flag.Root
                },
        )
    }

internal fun createTelemetryLog(
    id: Long,
    readoutItemKey: ReadoutItemKey = LmuWindowsReadoutItemKey.Flag.Root,
    simulator: Simulator = Simulator.LmuWindows,
    narratedText: String = "イエローフラッグ",
    narrationOutcome: NarrationOutcome = NarrationOutcome.INTERRUPTED,
) = TelemetryLog(
    id = id,
    createdAt = id,
    simulator = simulator,
    readoutItemKey = readoutItemKey,
    narratedText = narratedText,
    narrationOutcome = narrationOutcome,
    telemetryJson = """{"id":$id}""",
)
