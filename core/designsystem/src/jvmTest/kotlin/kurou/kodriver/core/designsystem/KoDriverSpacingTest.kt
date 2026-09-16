package kurou.kodriver.core.designsystem

import androidx.compose.ui.unit.dp
import org.junit.Test
import kotlin.test.assertEquals

class KoDriverSpacingTest {
    @Test
    fun `KoDriverSpacingは4dpグリッドに沿った値を持つ`() {
        assertEquals(4.dp, KoDriverSpacing.extraSmall)
        assertEquals(8.dp, KoDriverSpacing.small)
        assertEquals(12.dp, KoDriverSpacing.medium)
        assertEquals(16.dp, KoDriverSpacing.large)
        assertEquals(24.dp, KoDriverSpacing.extraLarge)
    }
}
