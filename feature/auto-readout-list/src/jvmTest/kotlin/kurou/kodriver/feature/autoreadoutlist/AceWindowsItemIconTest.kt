package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsItemIconTest {
    @Test
    fun `手動専用の前後車ギャップは車両アイコンを返す`() {
        assertEquals(Icons.Filled.DirectionsCar, aceWindowsItemIcon(AceWindowsReadoutItemKey.CarGap.Root))
    }
}
