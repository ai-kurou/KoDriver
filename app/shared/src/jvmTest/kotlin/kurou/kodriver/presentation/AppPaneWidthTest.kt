package kurou.kodriver.presentation

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class AppPaneWidthTest {
    @Test
    fun `AppPaneWidthはKoDriverListPaneWidthと同じ400dpである`() {
        assertEquals(400.dp, AppPaneWidth)
    }
}
