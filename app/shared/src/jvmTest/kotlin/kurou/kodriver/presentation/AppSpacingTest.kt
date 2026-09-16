package kurou.kodriver.presentation

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class AppSpacingTest {
    @Test
    fun `AppSpacingはKoDriverSpacingと同じ値を持つ`() {
        assertEquals(4.dp, AppSpacing.extraSmall)
        assertEquals(8.dp, AppSpacing.small)
        assertEquals(12.dp, AppSpacing.medium)
        assertEquals(16.dp, AppSpacing.large)
        assertEquals(24.dp, AppSpacing.extraLarge)
    }
}
