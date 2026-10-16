package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.simulator_name_ace_windows
import kurou.kodriver.core.designsystem.generated.resources.simulator_name_lmu_windows
import org.junit.Rule
import org.junit.Test

class DebugStateComponentsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `有効状態によらずキーに対応するタイトルと状態の内容を表示する`() {
        val titles =
            mapOf(
                true to Res.string.simulator_name_lmu_windows,
                false to Res.string.simulator_name_ace_windows,
            )
        val contents: Map<Boolean, @Composable (String) -> Unit> =
            mapOf(
                true to { state -> Text(text = "有効 $state") },
                false to { state -> Text(text = "無効 $state") },
            )
        rule.setContent {
            KoDriverTheme {
                Column(Modifier.width(360.dp)) {
                    listOf(true, false).forEach { enabled ->
                        DebugStateCard(
                            cardKey = enabled,
                            uiState = "テレメトリ",
                            enabled = enabled,
                            titles = titles,
                            contents = contents,
                            frame = { title, modifier, content ->
                                DebugStateCardFrame(
                                    title = title,
                                    symbol = "🎮",
                                    accent = MaterialTheme.colorScheme.primary,
                                    modifier = modifier,
                                    content = content,
                                )
                            },
                        )
                    }
                }
            }
        }

        listOf("Le Mans Ultimate（Windows版）", "Assetto Corsa EVO（Windows版）", "有効 テレメトリ", "無効 テレメトリ").forEach {
            rule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun `熱レベルと欠損値と状態チップと未取得文言を表示する`() {
        rule.setContent {
            KoDriverTheme {
                Column(Modifier.width(360.dp)) {
                    HeatLevel.entries.forEach { level ->
                        DebugStateHeatTile(text = level.name, heatLevel = level)
                    }
                    DebugStateHeatTile(text = "-℃", heatLevel = null)
                    DebugStateStatusChip(text = "グリーンフラッグ")
                    DebugStateUnavailableContent(text = "未取得")
                }
            }
        }

        (HeatLevel.entries.map { it.name } + listOf("-℃", "グリーンフラッグ", "未取得")).forEach {
            rule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun `装飾の有無によらずカード内容を表示して記号は読み上げない`() {
        rule.setContent {
            KoDriverTheme {
                Column(Modifier.width(360.dp)) {
                    listOf(true, false).forEach { showHandle ->
                        DebugStateCardFrame(
                            title = "タイトル $showHandle",
                            symbol = "🎮",
                            accent = MaterialTheme.colorScheme.primary,
                            showHandle = showHandle,
                        ) {
                            Text(text = "内容 $showHandle")
                        }
                    }
                }
            }
        }

        listOf(true, false).forEach {
            rule.onNodeWithText("タイトル $it").assertIsDisplayed()
            rule.onNodeWithText("内容 $it").assertIsDisplayed()
        }
        rule.onNodeWithText("🎮").assertDoesNotExist()
    }

    @Test
    fun `シミュレーターIDに対応する表示名を表示する`() {
        rule.setContent {
            KoDriverTheme {
                Column {
                    SimulatorInfoContent(simulatorId = "lmu_windows")
                    SimulatorInfoContent(simulatorId = "ace_windows")
                    SimulatorInfoContent(simulatorId = "gt7_ps5")
                }
            }
        }

        listOf("Le Mans Ultimate（Windows版）", "Assetto Corsa EVO（Windows版）", "Gran Turismo 7（PS5）").forEach {
            rule.onNodeWithText(it).assertIsDisplayed()
        }
    }
}
