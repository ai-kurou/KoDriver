package kurou.kodriver.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ListRowSelectionStyleTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `選択状態に応じて背景色と枠線と影が切り替わる`() {
        var isSelected by mutableStateOf(false)
        var actual: ListRowSelectionStyle? = null
        var unselected: ListRowSelectionStyle? = null
        var selected: ListRowSelectionStyle? = null
        rule.setContent {
            KoDriverTheme {
                val colors = MaterialTheme.colorScheme
                unselected = ListRowSelectionStyle(colors.surfaceContainerLow, Color.Transparent, 0.dp)
                selected = ListRowSelectionStyle(colors.secondaryContainer, colors.primary, 8.dp)
                actual = animateListRowSelectionStyle(isSelected)
            }
        }
        rule.waitForIdle()
        assertEquals(unselected, actual)

        isSelected = true
        rule.waitForIdle()
        assertEquals(selected, actual)

        isSelected = false
        rule.waitForIdle()
        assertEquals(unselected, actual)
    }
}
