package kurou.kodriver.feature.speechrecognitionlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import io.github.takahirom.roborazzi.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.buildlogic.screenshottest.defaultRoborazziOptions
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kurou.kodriver.domain.model.toSpeechCommandGuideGroups
import org.junit.Test

private const val SNAPSHOT_DIR = "src/jvmTest/snapshots"
private const val SNAPSHOT_PREFIX =
    "kurou.kodriver.feature.speechrecognitionlist.SpeechRecognitionListPaneScreenshotTest"

class SpeechRecognitionListPaneScreenshotTest {
    @Test
    fun `項目あり_LMU`() = captureProductionPhrases(Simulator.LmuWindows, "LMU", ::registerLmuWindowsReadoutItemKeys)

    @Test
    fun `項目あり_GT7`() = captureProductionPhrases(Simulator.Gt7Ps5, "GT7", ::registerGt7Ps5ReadoutItemKeys)

    @Test
    fun `項目あり_ACE`() = captureProductionPhrases(Simulator.AceWindows, "ACE", ::registerAceWindowsReadoutItemKeys)
}

/**
 * 本番の語句定義をそのまま使い、全カテゴリを漏れなく撮影する。語句を変更するとゴールデン画像も変わる。
 * 一覧は縦にスクロールし1枚に収まらないため、カテゴリごとにその見出しまでスクロールして1枚ずつ撮る
 * （最後のカテゴリは一覧の最下部まで表示される）。
 */
private fun captureProductionPhrases(
    simulator: Simulator,
    label: String,
    register: () -> Unit,
) = composeScreenshotTest {
    register()
    val groups =
        checkNotNull(ReadoutItemKeyRegistry.manualReadoutSpecOf(simulator.id)).toSpeechCommandGuideGroups()
    setPane(SpeechRecognitionListUiState(groups = groups))
    // 先頭は呼びかけの案内（index 0）、続いて「見出し＋項目」がカテゴリ順に並ぶ
    var headerIndex = 1
    groups.forEachIndexed { page, group ->
        onNodeWithTag(SPEECH_RECOGNITION_LIST_PANE_TEST_TAG).performScrollToIndex(if (page == 0) 0 else headerIndex)
        waitForIdle()
        onRoot().captureRoboImage(
            filePath = "$SNAPSHOT_DIR/$SNAPSHOT_PREFIX.項目あり_${label}_${page + 1}_${group.category.name}.png",
            roborazziOptions = defaultRoborazziOptions,
        )
        headerIndex += 1 + group.items.size
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
