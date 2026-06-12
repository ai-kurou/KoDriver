package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class OtherContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ライセンス詳細を表示`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                        OtherContent(
                            uiState = OtherListUiState(selectedItem = OtherListItemType.License),
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
