package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.core.designsystem.readoutItemDisplayName
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadoutItemDisplayNameTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `全読み上げキーの表示名を解決できる`() {
        var names by mutableStateOf(emptyList<String>())
        rule.setContent {
            names = ReadoutItemKey.entries.map { key -> readoutItemDisplayName(key.value) }
        }
        assertEquals(ReadoutItemKey.entries.size, names.size)
        assertTrue(names.all { it.isNotBlank() })
    }
}
