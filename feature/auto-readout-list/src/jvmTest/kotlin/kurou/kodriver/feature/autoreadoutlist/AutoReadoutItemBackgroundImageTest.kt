package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.autoreadoutlist.generated.resources.Res
import kurou.kodriver.feature.autoreadoutlist.generated.resources.best_lap_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.brake_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.flag_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.fuel_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.tyre_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.vehicle_approach_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.virtual_energy_row_background
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AutoReadoutItemBackgroundImageTest {
    @Test
    fun `背景画像の一辺は幅が狭いとき幅基準になる`() {
        // 幅基準 400 * 0.55 = 220 < 高さ基準 200 * 1.5 = 300
        assertEquals(220f, backgroundImageSide(width = 400f, height = 200f))
    }

    @Test
    fun `背景画像の一辺は幅が広いとき高さ基準の上限で止まる`() {
        // 幅基準 1200 * 0.55 = 660 > 高さ基準 200 * 1.5 = 300
        assertEquals(300f, backgroundImageSide(width = 1200f, height = 200f))
    }

    @Test
    fun `背景画像の一辺は幅基準と高さ基準が等しいとき、その値になる`() {
        // 幅基準 600 * 0.55 = 330 == 高さ基準 220 * 1.5 = 330
        assertEquals(330f, backgroundImageSide(width = 600f, height = 220f))
    }

    @Test
    fun `タイヤ温度とタイヤ摩耗の項目は背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.TyreTemperature.Root,
                LmuWindowsReadoutItemKey.TyreWear.Root,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                AceWindowsReadoutItemKey.TyreTemperature.Root,
            )
        for (key in keys) {
            assertEquals(Res.drawable.tyre_row_background, autoReadoutBackgroundImage(key), key.value)
        }
    }

    @Test
    fun `ブレーキ温度とブレーキ摩耗の項目はブレーキの背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                LmuWindowsReadoutItemKey.BrakeWear.Root,
            )
        for (key in keys) {
            assertEquals(Res.drawable.brake_row_background, autoReadoutBackgroundImage(key), key.value)
        }
    }

    @Test
    fun `自己ベストラップの項目はドライバーの背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.MyBestLap.Root,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                AceWindowsReadoutItemKey.MyBestLap.Root,
            )
        for (key in keys) {
            assertEquals(Res.drawable.best_lap_row_background, autoReadoutBackgroundImage(key), key.value)
        }
    }

    @Test
    fun `フラッグの項目は旗の背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.Flag.Root,
                AceWindowsReadoutItemKey.Flag.Root,
            )
        for (key in keys) {
            assertEquals(Res.drawable.flag_row_background, autoReadoutBackgroundImage(key), key.value)
        }
    }

    @Test
    fun `燃料残量と燃料の残り周回数の項目は燃料の背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.RemainingFuel.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                AceWindowsReadoutItemKey.RemainingFuel.Root,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
            )
        for (key in keys) {
            assertEquals(Res.drawable.fuel_row_background, autoReadoutBackgroundImage(key), key.value)
        }
    }

    @Test
    fun `バーチャルエネルギー残量の項目は仮想エネルギーの背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
            )
        for (key in keys) {
            assertEquals(Res.drawable.virtual_energy_row_background, autoReadoutBackgroundImage(key), key.value)
        }
    }

    @Test
    fun `車両接近の項目は車両接近の背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.VehicleApproach.Root,
                AceWindowsReadoutItemKey.VehicleApproach.Root,
            )
        for (key in keys) {
            assertEquals(Res.drawable.vehicle_approach_row_background, autoReadoutBackgroundImage(key), key.value)
        }
    }

    @Test
    fun `タイヤ・ブレーキ・自己ベストラップ・フラッグ・燃料・バーチャルエネルギー残量・車両接近以外の項目は背景画像を表示しない`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.PitTiming.Root,
            )
        for (key in keys) {
            assertNull(autoReadoutBackgroundImage(key), key.value)
        }
    }
}
