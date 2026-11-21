package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import org.junit.Test

/** 燃料の項目の詳細ペイン上部バーを、本番と同じタイトル・背景画像で撮影する。 */
class AutoReadoutDetailPaneFuelBackgroundScreenshotTest {
    init {
        registerAllReadoutItemKeys()
    }

    @Test
    fun `LMU 燃料残量`() = captureDetailPane(LmuWindowsReadoutItemKey.RemainingFuel.Root, dark = false)

    @Test
    fun `LMU 燃料残量 ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.RemainingFuel.Root, dark = true)

    @Test
    fun `GT7 燃料残量`() = captureDetailPane(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, dark = false)

    @Test
    fun `GT7 燃料残量 ダークテーマ`() = captureDetailPane(Gt7Ps5ReadoutItemKey.RemainingFuel.Root, dark = true)

    @Test
    fun `GT7 燃料残り周回数`() = captureDetailPane(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root, dark = false)

    @Test
    fun `GT7 燃料残り周回数 ダークテーマ`() = captureDetailPane(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root, dark = true)

    @Test
    fun `ACE 燃料残量`() = captureDetailPane(AceWindowsReadoutItemKey.RemainingFuel.Root, dark = false)

    @Test
    fun `ACE 燃料残量 ダークテーマ`() = captureDetailPane(AceWindowsReadoutItemKey.RemainingFuel.Root, dark = true)

    @Test
    fun `ACE 燃料残り周回数`() = captureDetailPane(AceWindowsReadoutItemKey.RemainingFuelLaps.Root, dark = false)

    @Test
    fun `ACE 燃料残り周回数 ダークテーマ`() = captureDetailPane(AceWindowsReadoutItemKey.RemainingFuelLaps.Root, dark = true)
}
