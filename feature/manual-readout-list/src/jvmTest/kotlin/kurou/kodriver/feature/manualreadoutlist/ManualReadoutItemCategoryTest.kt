package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ManualReadoutItemCategoryTest {
    @Test
    fun `detailの全20項目を対応するカテゴリに分類する`() {
        val expected =
            mapOf(
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value to ManualReadoutItemCategory.VIRTUAL_ENERGY,
                LmuWindowsReadoutItemKey.RemainingFuel.Laps.value to ManualReadoutItemCategory.FUEL,
                LmuWindowsReadoutItemKey.PitTiming.TyreWear.value to ManualReadoutItemCategory.TYRE,
                LmuWindowsReadoutItemKey.MyBestLap.Root.value to ManualReadoutItemCategory.LAP,
                AceWindowsReadoutItemKey.MyBestLap.Root.value to ManualReadoutItemCategory.LAP,
                LmuWindowsReadoutItemKey.RemainingFuel.Root.value to ManualReadoutItemCategory.FUEL,
                AceWindowsReadoutItemKey.RemainingFuel.Root.value to ManualReadoutItemCategory.FUEL,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value to ManualReadoutItemCategory.FUEL,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value to ManualReadoutItemCategory.FUEL,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value to ManualReadoutItemCategory.FUEL,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to ManualReadoutItemCategory.VIRTUAL_ENERGY,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root.value to ManualReadoutItemCategory.LAP,
                AceWindowsReadoutItemKey.TyreTemperature.Root.value to ManualReadoutItemCategory.TYRE,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value to ManualReadoutItemCategory.TYRE,
                LmuWindowsReadoutItemKey.TyreWear.Root.value to ManualReadoutItemCategory.TYRE,
                LmuWindowsReadoutItemKey.BrakeWear.Root.value to ManualReadoutItemCategory.BRAKE,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root.value to ManualReadoutItemCategory.BRAKE,
                LmuWindowsReadoutItemKey.TyreTemperature.Root.value to ManualReadoutItemCategory.TYRE,
                LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value to ManualReadoutItemCategory.LAP,
                LmuWindowsReadoutItemKey.ClassCarGap.Behind.value to ManualReadoutItemCategory.LAP,
            )
        expected.forEach { (key, category) -> assertEquals(category, manualReadoutItemCategory(key), key) }
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
