package kurou.kodriver.feature.acewindowsreadout.flagdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.Rule
import org.junit.Test

class AceWindowsReadoutFlagDetailPaneScreenshotTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `デフォルト`() {
        captureAceWindowsReadoutFlagDetailPane(
            enabledStates = FlagReadoutItem.entries.associate { it.key to true },
        )
    }

    @Test
    fun `全カード無効`() {
        captureAceWindowsReadoutFlagDetailPane(
            enabledStates = FlagReadoutItem.entries.associate { it.key to false },
        )
    }

    private fun captureAceWindowsReadoutFlagDetailPane(enabledStates: Map<ReadoutItemKey, Boolean>) {
        rule.setContent {
            KoDriverTheme {
                Surface {
                    Box(modifier = Modifier.requiredSize(480.dp, 800.dp)) {
                        AceWindowsReadoutFlagDetailPaneContent(
                            uiState = AceWindowsReadoutFlagDetailUiState(enabledStates = enabledStates),
                        )
                    }
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}
