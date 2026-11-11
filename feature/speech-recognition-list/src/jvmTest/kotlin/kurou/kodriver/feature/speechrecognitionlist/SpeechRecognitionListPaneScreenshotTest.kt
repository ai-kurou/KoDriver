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
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kurou.kodriver.domain.model.toSpeechCommandGuideGroups
import org.junit.Test

private const val PRODUCTION_GROUP_COUNT = 2

class SpeechRecognitionListPaneScreenshotTest {
    @Test
    fun `項目あり_LMU`() = captureProductionPhrases(Simulator.LmuWindows, ::registerLmuWindowsReadoutItemKeys)

    @Test
    fun `項目あり_GT7`() = captureProductionPhrases(Simulator.Gt7Ps5, ::registerGt7Ps5ReadoutItemKeys)

    @Test
    fun `項目あり_ACE`() = captureProductionPhrases(Simulator.AceWindows, ::registerAceWindowsReadoutItemKeys)
}

/**
 * 本番の語句定義をそのまま使って描画する。語句を変更するとゴールデン画像も変わる。
 * 全項目は画面に収まらないことがあるため、先頭の [PRODUCTION_GROUP_COUNT] カテゴリに絞る。
 */
private fun captureProductionPhrases(
    simulator: Simulator,
    register: () -> Unit,
) = composeScreenshotTest {
    register()
    val groups =
        checkNotNull(ReadoutItemKeyRegistry.manualReadoutSpecOf(simulator.id))
            .toSpeechCommandGuideGroups()
            .take(PRODUCTION_GROUP_COUNT)
    setPane(SpeechRecognitionListUiState(groups = groups))
    onRoot().captureRoboImage()
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
