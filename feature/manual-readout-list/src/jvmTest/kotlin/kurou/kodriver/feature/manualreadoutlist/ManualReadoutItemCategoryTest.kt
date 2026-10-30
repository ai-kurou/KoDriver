package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemDefinitions
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemDefinitions
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemDefinitions
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ManualReadoutItemCategoryTest {
    @Test
    fun `全simulatorの手動読み上げ項目定義を定義のカテゴリに分類する`() {
        val definitions =
            lmuWindowsManualReadoutItemDefinitions.values +
                aceWindowsManualReadoutItemDefinitions.values +
                gt7Ps5ManualReadoutItemDefinitions.values
        assertTrue(definitions.isNotEmpty())
        definitions.forEach { definition ->
            assertNotNull(manualReadoutItemCategory(definition.key), definition.key)
            assertEquals(definition.category, manualReadoutItemCategory(definition.key), definition.key)
        }
    }

    @Test
    fun `未知キーと空文字と手動対象外の自動キーは分類しない`() {
        listOf("unknown", "", LmuWindowsReadoutItemKey.PitTiming.Root.value).forEach {
            assertNull(manualReadoutItemCategory(it))
        }
    }

    @Test
    fun `集計は先頭の出現順と同カテゴリの件数を保ち未知キーを除く`() {
        assertEquals(
            listOf(ManualReadoutItemCategory.TYRE to 2, ManualReadoutItemCategory.FUEL to 2),
            manualReadoutCategoryCounts(
                listOf(
                    "unknown",
                    LmuWindowsReadoutItemKey.TyreWear.Root.value,
                    LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root.value,
                    LmuWindowsReadoutItemKey.RemainingFuel.Laps.value,
                ),
            ),
        )
        assertEquals(emptyList(), manualReadoutCategoryCounts(emptyList()))
        assertEquals(emptyList(), manualReadoutCategoryCounts(listOf("unknown")))
        assertEquals(
            listOf(ManualReadoutItemCategory.FUEL to 2),
            manualReadoutCategoryCounts(List(2) { LmuWindowsReadoutItemKey.RemainingFuel.Root.value }),
        )
    }
}
