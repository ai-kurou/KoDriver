package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemKeys
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemKeys
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

class ManualReadoutSetDetailPaneItemAddTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `バーチャルエネルギーの残り周回数を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("バーチャルエネルギーの残り周回数").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("バーチャルエネルギーは残り約3周")
    }

    @Test
    fun `タイヤ摩耗の残り周回数を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("タイヤ摩耗の残り周回数").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("タイヤは残り約3周")
    }

    @Test
    fun `燃料の残り周回数を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("燃料の残り周回数").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("燃料は残り約3周")
    }

    @Test
    fun `自己ベストラップを追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("自己ベストラップ").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("自己ベストラップ 1分23秒456")
    }

    @Test
    fun `ACEのタイヤ温度を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.AceWindows.id,
                    setNumber = 1,
                    availableItemKeys = aceWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("タイヤ温度").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("タイヤ温度90℃")
    }

    @Test
    fun `GT7の自己ベストラップを追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.Gt7Ps5.id,
                    setNumber = 1,
                    availableItemKeys = gt7Ps5ManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("自己ベストラップ").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("自己ベストラップ 1分23秒456")
    }

    @Test
    fun `クラス内ギャップを追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = listOf(LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("クラス内前方車とのギャップ").performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("クラス内前方1.5秒")
    }

    @Test
    fun `タイヤ温度を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("タイヤ温度").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("タイヤ温度90℃")
    }

    @Test
    fun `ブレーキ温度を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("ブレーキ温度").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("ブレーキ温度500℃")
    }

    @Test
    fun `ブレーキ残量を追加すると既定文言とプレビューを表示する`() {
        val state =
            mutableStateOf(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    setNumber = 1,
                    availableItemKeys = lmuWindowsManualReadoutItemKeys(),
                ),
            )
        rule.setContent {
            KoDriverTheme {
                ManualReadoutSetDetailPane(
                    uiState = state.value,
                    onAddItem = { state.value = state.value.copy(selectedItemKeys = listOf(it)) },
                    onDelete = {},
                )
            }
        }
        rule.onNodeWithText("＋ 項目を追加").performClick()
        rule.onNodeWithText("ブレーキ残量").performScrollTo().performClick()
        rule.onNodeWithTag("manual_readout_preview_sentence").assertTextEquals("ブレーキ残量50%")
    }
}
