package kurou.kodriver.core.designsystem

import androidx.compose.ui.unit.dp
import org.junit.Test
import kotlin.test.assertEquals

class KoDriverListPaneWidthTest {
    @Test
    fun `KoDriverListPaneWidthは400dpである`() {
        assertEquals(400.dp, KoDriverListPaneWidth)
    }
}
