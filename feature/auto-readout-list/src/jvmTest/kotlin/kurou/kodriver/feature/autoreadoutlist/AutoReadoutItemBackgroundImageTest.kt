package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            assertTrue(hasTyreBackgroundImage(key), key.value)
        }
    }

    @Test
    fun `タイヤ以外の項目は背景画像を表示しない`() {
        val keys: List<ReadoutItemKey> =
            listOf(
                LmuWindowsReadoutItemKey.BrakeWear.Root,
                LmuWindowsReadoutItemKey.RemainingFuel.Root,
                LmuWindowsReadoutItemKey.Flag.Root,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
                AceWindowsReadoutItemKey.RemainingFuel.Root,
            )
        for (key in keys) {
            assertFalse(hasTyreBackgroundImage(key), key.value)
        }
    }
}
