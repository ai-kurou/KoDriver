package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test

class ManualReadoutListPaneCategoryChipTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `標準表示は全カテゴリのチップを表示し未知キーを無視する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            sets =
                                listOf(
                                    ManualReadoutSet(
                                        "categories",
                                        itemKeys =
                                            listOf(
                                                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                                                LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                                                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                                                LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                                                LmuWindowsReadoutItemKey.MyBestLap.Root.value,
                                                "unknown",
                                            ),
                                    ),
                                ),
                        ),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }
        listOf("バーチャルエネルギー 1", "燃料 1", "タイヤ 1", "ブレーキ 1", "ラップ 1").forEach {
            rule.onNodeWithText(it, useUnmergedTree = true).assertIsDisplayed()
        }
        rule.onNodeWithText("unknown", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `不明なキーだけのセットは項目なしではなく項目数を表示する`() {
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            sets =
                                listOf(
                                    ManualReadoutSet("unknown-only", itemKeys = listOf("unknown-1", "unknown-2")),
                                ),
                        ),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = {},
                )
            }
        }
        rule.onNodeWithText("2項目", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("項目なし", useUnmergedTree = true).assertDoesNotExist()
    }
}
