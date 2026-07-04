@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailPaneContent
import kurou.kodriver.feature.otherconsoleipdetail.OtherConsoleIpDetailUiState
import kurou.kodriver.feature.otherlist.OtherListItemType
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.otherserveripdetail.OtherServerIpDetailPaneContent
import kurou.kodriver.feature.otherserveripdetail.OtherServerIpDetailUiState
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailPaneContent
import kurou.kodriver.feature.othervolumedetail.OtherVolumeDetailUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w840dp-h640dp")
class OtherContentScreenshotTest {

    @Test
    fun `Windows版KoDriverへ接続するIPアドレス詳細を表示`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            AppTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherContent(
                            uiState = OtherListUiState(selectedItem = OtherListItemType.ServerIp),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = twoPaneDirective,
                            detailContent = { itemType, canNavigateBack, onBack ->
                                if (itemType == OtherListItemType.ServerIp) {
                                    OtherServerIpDetailPaneContent(
                                        uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100"),
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

    @Test
    fun `ゲーム機とSimHubへ接続するIPアドレス詳細を表示`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            AppTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OtherContent(
                            uiState = OtherListUiState(selectedItem = OtherListItemType.ConsoleIp),
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            scaffoldDirective = twoPaneDirective,
                            detailContent = { itemType, canNavigateBack, onBack ->
                                if (itemType == OtherListItemType.ConsoleIp) {
                                    OtherConsoleIpDetailPaneContent(
                                        uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100"),
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

    @Test
    fun `音量詳細を表示`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            AppTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
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
