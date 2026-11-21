package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import org.junit.Test

/** 自己ベストラップ・フラッグの項目の詳細ペイン上部バーを、本番と同じタイトル・背景画像で撮影する。 */
class AutoReadoutDetailPaneBestLapFlagBackgroundScreenshotTest {
    init {
        registerAllReadoutItemKeys()
    }

    @Test
    fun `LMU 自己ベストラップ`() = captureDetailPane(LmuWindowsReadoutItemKey.MyBestLap.Root, dark = false)

    @Test
    fun `LMU 自己ベストラップ ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.MyBestLap.Root, dark = true)

    @Test
    fun `GT7 自己ベストラップ`() = captureDetailPane(Gt7Ps5ReadoutItemKey.MyBestLap.Root, dark = false)

    @Test
    fun `GT7 自己ベストラップ ダークテーマ`() = captureDetailPane(Gt7Ps5ReadoutItemKey.MyBestLap.Root, dark = true)

    @Test
    fun `ACE 自己ベストラップ`() = captureDetailPane(AceWindowsReadoutItemKey.MyBestLap.Root, dark = false)

    @Test
    fun `ACE 自己ベストラップ ダークテーマ`() = captureDetailPane(AceWindowsReadoutItemKey.MyBestLap.Root, dark = true)

    @Test
    fun `LMU フラッグ`() = captureDetailPane(LmuWindowsReadoutItemKey.Flag.Root, dark = false)

    @Test
    fun `LMU フラッグ ダークテーマ`() = captureDetailPane(LmuWindowsReadoutItemKey.Flag.Root, dark = true)

    @Test
    fun `ACE フラッグ`() = captureDetailPane(AceWindowsReadoutItemKey.Flag.Root, dark = false)

    @Test
    fun `ACE フラッグ ダークテーマ`() = captureDetailPane(AceWindowsReadoutItemKey.Flag.Root, dark = true)
}
