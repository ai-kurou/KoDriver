package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import org.junit.Test

/** バーチャルエネルギー・車両接近・ピットタイミング・車両故障の項目の詳細ペイン上部バーを、本番と同じタイトル・背景画像で撮影する。 */
class AutoReadoutDetailPaneOtherBackgroundScreenshotTest {
    init {
        registerAllReadoutItemKeys()
    }

    @Test
    fun `LMU バーチャルエネルギー残量`() = captureDetailPane(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root, dark = false)

    @Test
    fun `LMU バーチャルエネルギー残量 ダークテーマ`() =
        captureDetailPane(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root, dark = true)

    @Test
    fun `LMU 車両接近`() = captureDetailPane(LmuWindowsReadoutItemKey.VehicleApproach.Root, dark = false)

    @Test
    fun `LMU 車両接近 ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.VehicleApproach.Root, dark = true)

    @Test
    fun `ACE 車両接近`() = captureDetailPane(AceWindowsReadoutItemKey.VehicleApproach.Root, dark = false)

    @Test
    fun `ACE 車両接近 ダークテーマ`() = captureDetailPane(AceWindowsReadoutItemKey.VehicleApproach.Root, dark = true)

    @Test
    fun `LMU ピットタイミング`() = captureDetailPane(LmuWindowsReadoutItemKey.PitTiming.Root, dark = false)

    @Test
    fun `LMU ピットタイミング ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.PitTiming.Root, dark = true)

    @Test
    fun `LMU 車両故障`() = captureDetailPane(LmuWindowsReadoutItemKey.VehicleDamage.Root, dark = false)

    @Test
    fun `LMU 車両故障 ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.VehicleDamage.Root, dark = true)
}
