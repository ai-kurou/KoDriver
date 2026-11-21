package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.feature.autoreadoutlist.generated.resources.Res
import kurou.kodriver.feature.autoreadoutlist.generated.resources.brake_row_background
import kurou.kodriver.feature.autoreadoutlist.generated.resources.tyre_row_background
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AutoReadoutItemBackgroundImageTest {
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
    fun `タイヤとブレーキ以外の項目は背景画像を表示しない`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.RemainingFuel.Root,
                LmuWindowsReadoutItemKey.Flag.Root,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                AceWindowsReadoutItemKey.RemainingFuel.Root,
            )
        for (key in keys) {
            assertNull(autoReadoutBackgroundImage(key), key.value)
        }
    }
}
