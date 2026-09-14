@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.defaultRoborazziOptions
import kurou.kodriver.buildlogic.screenshottest.rememberFoldedVerticalHingeDirective
import kurou.kodriver.buildlogic.screenshottest.rememberNonFlatVerticalHingePosture
import kurou.kodriver.buildlogic.screenshottest.rememberTabletopPosture
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w840dp-h640dp")
class TelemetryLogContentScreenshotTest {
    @Test
    fun `折りたたみ端末の縦ヒンジを避けて2ペイン表示`() {
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TelemetryLogContentScaffold(
                            uiState = previewTelemetryLogListUiState.copy(selectedLogId = 2),
                            scaffoldDirective = rememberFoldedVerticalHingeDirective(),
                            detailContent = { logId -> Text("Detail: $logId") },
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `テーブルトップ姿勢では一覧のみをヒンジより上側に収めて表示する`() {
        // テーブルトップ姿勢では detailPane を閉じて一覧のみ表示するため、
        // 無選択状態（selectedLogId = null）の一覧がヒンジ上端までに収まることを確認する。
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        TelemetryLogContentScaffold(
                            uiState = previewTelemetryLogListUiState.copy(selectedLogId = null),
                            windowPosture = rememberTabletopPosture(),
                            detailContent = { logId -> Text("Detail: $logId") },
                        )
                    }
                }
            }
        }
    }

    @Suppress("MutableStateAutoboxing")
    @Test
    fun `平らでない縦ヒンジの姿勢では一覧のみを表示する`() {
        // 平らでない縦ヒンジ（本を途中まで開いた姿勢）へ遷移すると、選択中だった detailPane が
        // shouldCollapseDetailPane 経由で自動的に閉じられ、一覧のみが潰れずに表示されることを確認する。
        captureRoboImage(roborazziOptions = defaultRoborazziOptions) {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        var selectedLogId by remember { mutableStateOf<Long?>(2L) }
                        TelemetryLogContentScaffold(
                            uiState = previewTelemetryLogListUiState.copy(selectedLogId = selectedLogId),
                            onLogSelected = { selectedLogId = it },
                            onClearSelectedLog = { selectedLogId = null },
                            windowPosture = rememberNonFlatVerticalHingePosture(),
                            detailContent = { logId -> Text("Detail: $logId") },
                        )
                    }
                }
            }
        }
    }
}
