package kurou.kodriver.feature.other

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class OtherListPaneScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    private val twoPaneDirective = PaneScaffoldDirective(
        maxHorizontalPartitions = 2,
        horizontalPartitionSpacerSize = 16.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        excludedBounds = emptyList(),
    )

    @Test
    fun `ライセンス項目を表示`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        OtherListPane(
                            uiState = OtherListUiState(),
                            onItemClick = {},
                        )
                    }
                }
            }
        }

        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `ライセンス項目選択済みでハイライト表示`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                        OtherListPane(
                            uiState = OtherListUiState(selectedItem = OtherItemType.License),
                            onItemClick = {},
                        )
                    }
                }
            }
        }

        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `ライセンス詳細を表示`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                        OtherContent(
                            uiState = OtherListUiState(selectedItem = OtherItemType.License),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = twoPaneDirective,
                            detailContent = {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    androidx.compose.material3.Text("License Detail")
                                }
                            },
                        )
                    }
                }
            }
        }

        rule.onRoot().captureRoboImage()
    }
}
