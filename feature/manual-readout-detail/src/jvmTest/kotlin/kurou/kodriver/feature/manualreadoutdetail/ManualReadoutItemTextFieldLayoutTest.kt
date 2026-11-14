package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.width
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemTextDefault
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualReadoutItemTextFieldLayoutTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `クラス内前後車ギャップのヒントはチップの下に全幅で配置する`() {
        val key = LmuWindowsReadoutItemKey.ClassCarGap.Root.value
        val defaultText = lmuWindowsManualReadoutItemTextDefault(key)
        rule.setContent {
            KoDriverTheme {
                ManualReadoutItemTextField(
                    text = defaultText,
                    defaultText = defaultText,
                    onTextChanged = {},
                    itemKey = key,
                    simulatorId = Simulator.LmuWindows.id,
                )
            }
        }

        val chipBounds = rule.onNodeWithText("を挿入", substring = true).getBoundsInRoot()
        val hintBounds = rule.onNodeWithText("{ahead}は前の車との差の秒数", substring = true).getBoundsInRoot()
        val rootBounds = rule.onRoot().getBoundsInRoot()

        assertTrue(hintBounds.top >= chipBounds.bottom)
        assertEquals(rootBounds.width, hintBounds.width)
        assertEquals(chipBounds.left, hintBounds.left)
    }
}
