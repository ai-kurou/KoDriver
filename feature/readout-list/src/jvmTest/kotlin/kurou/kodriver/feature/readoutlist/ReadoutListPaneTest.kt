package kurou.kodriver.feature.readoutlist

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadoutListPaneTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト状態では読み上げ優先度ヒントを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState = ReadoutListUiState(),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule.onNodeWithText("読み上げ優先度").assertIsDisplayed()
    }

    @Test
    fun `読み上げ項目をタップするとonItemClickが呼ばれる`() {
        val clicked = mutableListOf<ReadoutItemKey>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root),
                            readoutEnabledStates =
                                mapOf(
                                    ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
                                ),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = { clicked += it },
                )
            }
        }

        rule.onNodeWithContentDescription("バーチャルエナジー残量").performClick()

        assertEquals(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root, clicked.single())
    }

    @Test
    fun `シェブロンをタップするとonItemClickが呼ばれる`() {
        val clicked = mutableListOf<ReadoutItemKey>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root),
                            readoutEnabledStates =
                                mapOf(
                                    ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
                                ),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = { clicked += it },
                )
            }
        }

        rule
            .onNodeWithTag(
                "readoutListChevronTouchTarget:${ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root.value}",
            ).performClick()

        assertEquals(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root, clicked.single())
    }

    @Test
    fun `スイッチとキュー追加トグルはON_OFF変更コールバックを呼ぶ`() {
        val readoutChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        val queueChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to true),
                            queueEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { item, enabled -> readoutChanges += item to enabled },
                    onQueueEnabledChanged = { item, enabled -> queueChanges += item to enabled },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule.onAllNodes(hasQueueToggleRole()).assertCountEquals(2)
        rule.onAllNodes(hasSwitchRole()).assertCountEquals(1)
        rule
            .onNodeWithTag("readoutListQueueTouchTarget:${ReadoutItemKey.LmuWindows.Flag.Root.value}")
            .assertIsEnabled()
            .performClick()
        rule.onAllNodes(hasSwitchRole())[0].assertIsEnabled().performClick()

        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root to true, queueChanges.single())
        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root to false, readoutChanges.single())
    }

    @Test
    fun `読み上げ開始音トグルはクリックするたびにローカルのON_OFF状態を反転する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to true),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        val startSoundToggle =
            rule.onNodeWithTag("readoutListStartSoundTouchTarget:${ReadoutItemKey.LmuWindows.Flag.Root.value}")

        startSoundToggle.assertIsEnabled().performClick()
        startSoundToggle.performClick()
    }

    @Test
    fun `スイッチとキュー追加トグルの外側タップ領域は項目タップではなくON_OFF変更コールバックを呼ぶ`() {
        val readoutChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        val queueChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        val clicked = mutableListOf<ReadoutItemKey>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to true),
                            queueEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { item, enabled -> readoutChanges += item to enabled },
                    onQueueEnabledChanged = { item, enabled -> queueChanges += item to enabled },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = { clicked += it },
                )
            }
        }

        rule
            .onNodeWithTag("readoutListQueueTouchTarget:${ReadoutItemKey.LmuWindows.Flag.Root.value}")
            .assertIsEnabled()
            .performClick()
        rule
            .onNodeWithTag("readoutListSwitchTouchTarget:${ReadoutItemKey.LmuWindows.Flag.Root.value}")
            .assertIsEnabled()
            .performClick()

        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root to true, queueChanges.single())
        assertEquals(ReadoutItemKey.LmuWindows.Flag.Root to false, readoutChanges.single())
        assertEquals(emptyList(), clicked)
    }

    @Test
    fun `読み上げOFFの項目はキュー追加トグルを無効にする`() {
        val queueChanges = mutableListOf<Pair<ReadoutItemKey, Boolean>>()
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                            queueEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { item, enabled -> queueChanges += item to enabled },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule
            .onNodeWithTag("readoutListQueueTouchTarget:${ReadoutItemKey.LmuWindows.Flag.Root.value}")
            .assertIsNotEnabled()
            .performClick()

        assertFalse(queueChanges.contains(ReadoutItemKey.LmuWindows.Flag.Root to true))
    }

    @Test
    fun `読み上げOFFの項目は読み上げ開始音トグルを無効にする`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to false),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule
            .onNodeWithTag("readoutListStartSoundTouchTarget:${ReadoutItemKey.LmuWindows.Flag.Root.value}")
            .assertIsNotEnabled()
            .performClick()
    }

    @Test
    fun `ACE選択時のみ読み上げタイミングヒントを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to true),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule
            .onNodeWithText(
                "レース開始前やローディング画面で読み上げが発生することがありますが、仕様上の挙動です。" +
                    "レース中（コース走行中）は設定通りに読み上げられます。" +
                    "なお、Assetto Corsa Evoは現在正式リリース前のため、" +
                    "シミュレーターとKoDriverのバージョンの組み合わせによっては正しく動作しない場合があります。",
            ).assertDoesNotExist()
    }

    @Test
    fun `ACE選択時は読み上げタイミングヒントを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.AceWindows,
                            items = listOf(ReadoutItemKey.AceWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.AceWindows.Flag.Root to true),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule
            .onNodeWithText(
                "レース開始前やローディング画面で読み上げが発生することがありますが、仕様上の挙動です。" +
                    "レース中（コース走行中）は設定通りに読み上げられます。" +
                    "なお、Assetto Corsa Evoは現在正式リリース前のため、" +
                    "シミュレーターとKoDriverのバージョンの組み合わせによっては正しく動作しない場合があります。",
            ).assertIsDisplayed()
    }

    @Test
    fun `GT7選択時（デスクトップ）は再生元ヒントを表示する`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.Gt7Ps5,
                            items = listOf(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.Gt7Ps5.TyreTemperature.Root to true),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule
            .onNodeWithText("読み上げ音声はWindows PCから再生されます。PS5からは再生されません。")
            .assertIsDisplayed()
    }

    @Test
    fun `GT7以外選択時は再生元ヒントを表示しない`() {
        rule.setContent {
            KoDriverTheme {
                ReadoutListPane(
                    uiState =
                        ReadoutListUiState(
                            selectedSimulator = Simulator.LmuWindows,
                            items = listOf(ReadoutItemKey.LmuWindows.Flag.Root),
                            readoutEnabledStates = mapOf(ReadoutItemKey.LmuWindows.Flag.Root to true),
                        ),
                    onMove = { _, _ -> },
                    onReadoutEnabledChanged = { _, _ -> },
                    onQueueEnabledChanged = { _, _ -> },
                    onStartSoundEnabledChanged = { _, _ -> },
                    onItemClick = {},
                )
            }
        }

        rule
            .onNodeWithText("読み上げ音声はWindows PCから再生されます。PS5からは再生されません。")
            .assertDoesNotExist()
    }

    private fun hasSwitchRole(): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)

    private fun hasQueueToggleRole(): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)
}

class ReadoutItemStartIndexTest {
    @Test
    fun `ACE以外・GT7デスクトップヒント非表示の場合はヘッダー1件分のオフセットを返す`() {
        assertEquals(1, readoutItemStartIndex(isAceSelected = false, isGt7Ps5DesktopHintShown = false))
    }

    @Test
    fun `ACEを選択している場合はタイミングヒント分を加えた2件分のオフセットを返す`() {
        assertEquals(2, readoutItemStartIndex(isAceSelected = true, isGt7Ps5DesktopHintShown = false))
    }

    @Test
    fun `GT7デスクトップヒント表示時は再生元ヒント分を加えた2件分のオフセットを返す`() {
        assertEquals(2, readoutItemStartIndex(isAceSelected = false, isGt7Ps5DesktopHintShown = true))
    }
}

class ShouldShowGt7Ps5DesktopReadoutHintTest {
    @Test
    fun `GT7選択時はtrueを返す（jvmTestはデスクトップ扱い）`() {
        assertTrue(shouldShowGt7Ps5DesktopReadoutHint(Simulator.Gt7Ps5))
    }

    @Test
    fun `GT7以外を選択している場合はfalseを返す`() {
        assertFalse(shouldShowGt7Ps5DesktopReadoutHint(Simulator.LmuWindows))
        assertFalse(shouldShowGt7Ps5DesktopReadoutHint(Simulator.AceWindows))
    }
}
