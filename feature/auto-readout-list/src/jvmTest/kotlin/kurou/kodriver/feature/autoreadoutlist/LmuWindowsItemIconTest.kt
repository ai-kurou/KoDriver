package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsItemIconTest {
    @Test
    fun `手動専用のクラス内ギャップは車両アイコンを返す`() {
        assertEquals(Icons.Filled.DirectionsCar, lmuWindowsItemIcon(LmuWindowsReadoutItemKey.ClassCarGap.Ahead))
        assertEquals(Icons.Filled.DirectionsCar, lmuWindowsItemIcon(LmuWindowsReadoutItemKey.ClassCarGap.Behind))
    }
}
