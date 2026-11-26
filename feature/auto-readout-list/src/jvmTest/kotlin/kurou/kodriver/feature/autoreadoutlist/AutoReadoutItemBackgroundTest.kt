package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.core.designsystem.ReadoutBackground
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AutoReadoutItemBackgroundTest {
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
            assertEquals(ReadoutBackground.TYRE, autoReadoutBackground(key), key.value)
        }
    }

    @Test
    fun `ブレーキ温度とブレーキ摩耗の項目はブレーキの背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                LmuWindowsReadoutItemKey.BrakeWear.Root,
                AceWindowsReadoutItemKey.BrakeTemperature.Root,
            )
        for (key in keys) {
            assertEquals(ReadoutBackground.BRAKE, autoReadoutBackground(key), key.value)
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
            assertEquals(ReadoutBackground.BEST_LAP, autoReadoutBackground(key), key.value)
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
            assertEquals(ReadoutBackground.FLAG, autoReadoutBackground(key), key.value)
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
            assertEquals(ReadoutBackground.FUEL, autoReadoutBackground(key), key.value)
        }
    }

    @Test
    fun `バーチャルエネルギー残量の項目は仮想エネルギーの背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
            )
        for (key in keys) {
            assertEquals(ReadoutBackground.VIRTUAL_ENERGY, autoReadoutBackground(key), key.value)
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
            assertEquals(ReadoutBackground.VEHICLE_APPROACH, autoReadoutBackground(key), key.value)
        }
    }

    @Test
    fun `ピットタイミングの項目はピットの背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.PitTiming.Root,
            )
        for (key in keys) {
            assertEquals(ReadoutBackground.PIT_TIMING, autoReadoutBackground(key), key.value)
        }
    }

    @Test
    fun `車両故障の項目は車両故障の背景画像を表示する`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.VehicleDamage.Root,
            )
        for (key in keys) {
            assertEquals(ReadoutBackground.VEHICLE_FAILURE, autoReadoutBackground(key), key.value)
        }
    }

    @Test
    fun `背景画像を持たない項目は背景画像を表示しない`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.ClassCarGap.Root,
            )
        for (key in keys) {
            assertNull(autoReadoutBackground(key), key.value)
        }
    }
}
