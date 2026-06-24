@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailPaneContent
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OtherContentScreenshotTest {

    @Test
    fun `音量詳細を表示`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                        OtherContent(
                            uiState = OtherListUiState(selectedItem = OtherListItemType.Volume),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = twoPaneDirective,
                            detailContent = { itemType, canNavigateBack, onBack ->
                                if (itemType == OtherListItemType.Volume) {
                                    OtherVolumeDetailPaneContent(
                                        uiState = OtherVolumeDetailUiState(volume = 80),
                                        canNavigateBack = canNavigateBack,
                                        onBack = onBack,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
