package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.LocalBottomEndOverlayInset
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutListPaneOverlayInsetTest {
    @get:Rule
    val rule = createComposeRule()

    private val inset = mutableStateOf(0.dp)
    private var density = 1f

    @Test
    fun `右下に重なる要素があるときは追加ボタンをその分だけ上に置く`() {
        rule.setContent {
            density = LocalDensity.current.density
            CompositionLocalProvider(LocalBottomEndOverlayInset provides inset.value) {
                KoDriverTheme {
                    ManualReadoutListPaneContent(
                        uiState = ManualReadoutListUiState(playButtonLayoutEnabled = false),
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        onAddSet = {},
                        onReadSet = {},
                        showHotkeyChip = false,
                    )
                }
            }
        }
        val withoutOverlay = addButtonBottom()

        inset.value = OVERLAY_INSET
        rule.waitForIdle()
        val withOverlay = addButtonBottom()

        assertEquals(OVERLAY_INSET.value * density, withoutOverlay - withOverlay, absoluteTolerance = 0.5f)
    }

    private fun addButtonBottom(): Float {
        val node = rule.onNodeWithText("セットを追加", useUnmergedTree = true).fetchSemanticsNode()
        return node.boundsInRoot.bottom
    }

    private companion object {
        val OVERLAY_INSET: Dp = 52.dp
    }
}
