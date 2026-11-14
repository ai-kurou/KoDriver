package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.ui.graphics.Color
import kurou.kodriver.core.designsystem.ExtendedColorScheme
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AutoReadoutItemCategoryTest {
    @Test
    fun `LMUの項目を手動タブと同じカテゴリに分類する`() {
        val expected =
            mapOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to ManualReadoutItemCategory.VIRTUAL_ENERGY,
                LmuWindowsReadoutItemKey.RemainingFuel.Root to ManualReadoutItemCategory.FUEL,
                LmuWindowsReadoutItemKey.TyreTemperature.Root to ManualReadoutItemCategory.TYRE,
                LmuWindowsReadoutItemKey.TyreWear.Root to ManualReadoutItemCategory.TYRE,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root to ManualReadoutItemCategory.BRAKE,
                LmuWindowsReadoutItemKey.BrakeWear.Root to ManualReadoutItemCategory.BRAKE,
                LmuWindowsReadoutItemKey.MyBestLap.Root to ManualReadoutItemCategory.LAP,
            )
        for ((key, category) in expected) {
            assertEquals(category, autoReadoutItemCategory(key), key.value)
        }
    }

    @Test
    fun `LMUのカテゴリを持たない項目はnullを返す`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.Flag.Root,
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
                LmuWindowsReadoutItemKey.PitTiming.Root,
                LmuWindowsReadoutItemKey.ClassCarGap.Root,
            )
        for (key in keys) {
            assertNull(autoReadoutItemCategory(key), key.value)
        }
    }

    @Test
    fun `GT7の全項目をカテゴリに分類する`() {
        val expected =
            mapOf(
                Gt7Ps5ReadoutItemKey.MyBestLap.Root to ManualReadoutItemCategory.LAP,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to ManualReadoutItemCategory.FUEL,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root to ManualReadoutItemCategory.FUEL,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root to ManualReadoutItemCategory.TYRE,
            )
        for ((key, category) in expected) {
            assertEquals(category, autoReadoutItemCategory(key), key.value)
        }
    }

    @Test
    fun `ACEの項目を分類しフラッグと車両接近はnullを返す`() {
        val expected =
            mapOf(
                AceWindowsReadoutItemKey.RemainingFuel.Root to ManualReadoutItemCategory.FUEL,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root to ManualReadoutItemCategory.FUEL,
                AceWindowsReadoutItemKey.TyreTemperature.Root to ManualReadoutItemCategory.TYRE,
                AceWindowsReadoutItemKey.MyBestLap.Root to ManualReadoutItemCategory.LAP,
            )
        for ((key, category) in expected) {
            assertEquals(category, autoReadoutItemCategory(key), key.value)
        }
        assertNull(autoReadoutItemCategory(AceWindowsReadoutItemKey.VehicleApproach.Root))
        assertNull(autoReadoutItemCategory(AceWindowsReadoutItemKey.Flag.Root))
    }

    @Test
    fun `どのシミュレーターにも属さないキーはnullを返す`() {
        val key =
            object : ReadoutItemKey {
                override val value = "unknown"
            }

        assertNull(autoReadoutItemCategory(key))
    }

    @Test
    fun `カテゴリごとに拡張カラーの対応する色を返す`() {
        val colors =
            ExtendedColorScheme(
                warningContainer = Color.Unspecified,
                onWarningContainer = Color.Unspecified,
                heatCool = Color.Unspecified,
                heatOk = Color.Unspecified,
                heatWarm = Color.Unspecified,
                heatHot = Color.Unspecified,
                categoryVirtualEnergy = Color(0xFF000001),
                categoryFuel = Color(0xFF000002),
                categoryTyre = Color(0xFF000003),
                categoryBrake = Color(0xFF000004),
                categoryLap = Color(0xFF000005),
            )

        val expected =
            mapOf(
                ManualReadoutItemCategory.VIRTUAL_ENERGY to colors.categoryVirtualEnergy,
                ManualReadoutItemCategory.FUEL to colors.categoryFuel,
                ManualReadoutItemCategory.TYRE to colors.categoryTyre,
                ManualReadoutItemCategory.BRAKE to colors.categoryBrake,
                ManualReadoutItemCategory.LAP to colors.categoryLap,
            )
        for ((category, color) in expected) {
            assertEquals(color, autoReadoutCategoryColor(category, colors), category.name)
        }
    }
}
