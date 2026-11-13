package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutCategoryColorTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun categoryColorMatchesExtendedColors() {
        val actual = mutableMapOf<ManualReadoutItemCategory, Color>()
        lateinit var expected: Map<ManualReadoutItemCategory, Color>
        rule.setContent {
            KoDriverTheme {
                val c = KoDriverExtendedColors.current
                expected =
                    mapOf(
                        ManualReadoutItemCategory.VIRTUAL_ENERGY to c.categoryVirtualEnergy,
                        ManualReadoutItemCategory.FUEL to c.categoryFuel,
                        ManualReadoutItemCategory.TYRE to c.categoryTyre,
                        ManualReadoutItemCategory.BRAKE to c.categoryBrake,
                        ManualReadoutItemCategory.LAP to c.categoryLap,
                    )
                ManualReadoutItemCategory.entries.forEach { actual[it] = manualReadoutCategoryColor(it) }
            }
        }
        rule.waitForIdle()
        assertEquals(expected, actual)
    }
}
