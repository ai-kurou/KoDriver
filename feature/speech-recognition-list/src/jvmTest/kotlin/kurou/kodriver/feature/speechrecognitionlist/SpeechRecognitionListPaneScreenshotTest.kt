package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import org.junit.Test

class SpeechRecognitionListPaneScreenshotTest {
    @Test
    fun `項目あり`() =
        composeScreenshotTest {
            setPane(
                SpeechRecognitionListUiState(
                    groups =
                        listOf(
                            SpeechCommandGuideGroup(
                                category = ManualReadoutItemCategory.FUEL,
                                items =
                                    listOf(
                                        SpeechCommandGuideItem("fuel", "燃料残量", listOf("燃料", "ガス", "残り燃料")),
                                    ),
                            ),
                            SpeechCommandGuideGroup(
                                category = ManualReadoutItemCategory.TYRE,
                                items =
                                    listOf(
                                        SpeechCommandGuideItem("tyre_temp", "タイヤ温度", listOf("タイヤ温度", "タイヤ", "温度")),
                                        SpeechCommandGuideItem("tyre_wear", "タイヤ摩耗", listOf("タイヤ摩耗", "摩耗", "タイヤ残り")),
                                    ),
                            ),
                            SpeechCommandGuideGroup(
                                category = ManualReadoutItemCategory.LAP,
                                items =
                                    listOf(
                                        SpeechCommandGuideItem("best_lap", "ベストラップ", listOf("ベスト", "自己ベスト", "ベストラップ")),
                                    ),
                            ),
                        ),
                ),
            )
            onRoot().captureRoboImage()
        }
}

private fun DesktopComposeUiTest.setPane(uiState: SpeechRecognitionListUiState) {
    setContent {
        KoDriverTheme {
            Surface {
                Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                    SpeechRecognitionListPaneContent(uiState = uiState)
                }
            }
        }
    }
}
