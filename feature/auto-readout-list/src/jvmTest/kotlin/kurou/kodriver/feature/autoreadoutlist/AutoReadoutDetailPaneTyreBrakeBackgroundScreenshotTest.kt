package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import org.junit.Test

/** タイヤ・ブレーキの項目の詳細ペイン上部バーを、本番と同じタイトル・背景画像で撮影する。 */
class AutoReadoutDetailPaneTyreBrakeBackgroundScreenshotTest {
    init {
        registerAllReadoutItemKeys()
    }

    @Test
    fun `LMU タイヤ温度`() = captureDetailPane(LmuWindowsReadoutItemKey.TyreTemperature.Root, dark = false)

    @Test
    fun `LMU タイヤ温度 ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.TyreTemperature.Root, dark = true)

    @Test
    fun `LMU タイヤ摩耗`() = captureDetailPane(LmuWindowsReadoutItemKey.TyreWear.Root, dark = false)

    @Test
    fun `LMU タイヤ摩耗 ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.TyreWear.Root, dark = true)

    @Test
    fun `GT7 タイヤ温度`() = captureDetailPane(Gt7Ps5ReadoutItemKey.TyreTemperature.Root, dark = false)

    @Test
    fun `GT7 タイヤ温度 ダークテーマ`() = captureDetailPane(Gt7Ps5ReadoutItemKey.TyreTemperature.Root, dark = true)

    @Test
    fun `ACE タイヤ温度`() = captureDetailPane(AceWindowsReadoutItemKey.TyreTemperature.Root, dark = false)

    @Test
    fun `ACE タイヤ温度 ダークテーマ`() = captureDetailPane(AceWindowsReadoutItemKey.TyreTemperature.Root, dark = true)

    @Test
    fun `LMU ブレーキ温度`() = captureDetailPane(LmuWindowsReadoutItemKey.BrakeTemperature.Root, dark = false)

    @Test
    fun `LMU ブレーキ温度 ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.BrakeTemperature.Root, dark = true)

    @Test
    fun `LMU ブレーキ摩耗`() = captureDetailPane(LmuWindowsReadoutItemKey.BrakeWear.Root, dark = false)

    @Test
    fun `LMU ブレーキ摩耗 ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.BrakeWear.Root, dark = true)
}
