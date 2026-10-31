package kurou.kodriver.data.preferences

import kotlin.test.Test
import kotlin.test.assertEquals

class DebugStateCardOrderPreferencesSerializerTest {
    @Test
    fun `デフォルト値は空リスト`() {
        assertEquals(
            DebugStateCardOrderPreferences(cardOrder = emptyList()),
            DebugStateCardOrderPreferencesSerializer.defaultValue,
        )
    }
}
