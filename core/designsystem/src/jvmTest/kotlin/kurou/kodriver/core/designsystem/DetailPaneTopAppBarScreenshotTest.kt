package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test

class DetailPaneTopAppBarScreenshotTest {
    @Test
    fun `戻るボタンあり`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                            DetailPaneTopAppBar(
                                title = "車両接近",
                                canNavigateBack = true,
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `戻るボタンあり ダークテーマ`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme(darkTheme = true) {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                            DetailPaneTopAppBar(
                                title = "車両接近",
                                canNavigateBack = true,
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `戻るボタンなし`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                            DetailPaneTopAppBar(
                                title = "車両接近",
                                canNavigateBack = false,
                                onBack = {},
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `20文字のセット名は操作ありで末尾を省略する`() =
        composeScreenshotTest {
            setContent {
                KoDriverTheme {
                    Surface {
                        Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                            DetailPaneTopAppBar(
                                title = "WWWWWWWWWWWWWWWWWWWW",
                                canNavigateBack = true,
                                onBack = {},
                                actions = { Text("保存") },
                            )
                        }
                    }
                }
            }
            onAllNodes(isRoot()).get(0).captureRoboImage()
        }

    @Test
    fun `背景画像 タイヤ`() = captureWithBackground(ReadoutBackground.TYRE, darkTheme = false)

    @Test
    fun `背景画像 タイヤ ダークテーマ`() = captureWithBackground(ReadoutBackground.TYRE, darkTheme = true)

    @Test
    fun `背景画像 ブレーキ`() = captureWithBackground(ReadoutBackground.BRAKE, darkTheme = false)

    @Test
    fun `背景画像 ブレーキ ダークテーマ`() = captureWithBackground(ReadoutBackground.BRAKE, darkTheme = true)

    @Test
    fun `背景画像 燃料`() = captureWithBackground(ReadoutBackground.FUEL, darkTheme = false)

    @Test
    fun `背景画像 燃料 ダークテーマ`() = captureWithBackground(ReadoutBackground.FUEL, darkTheme = true)

    @Test
    fun `背景画像 自己ベストラップ`() = captureWithBackground(ReadoutBackground.BEST_LAP, darkTheme = false)

    @Test
    fun `背景画像 自己ベストラップ ダークテーマ`() = captureWithBackground(ReadoutBackground.BEST_LAP, darkTheme = true)

    @Test
    fun `背景画像 フラッグ`() = captureWithBackground(ReadoutBackground.FLAG, darkTheme = false)

    @Test
    fun `背景画像 フラッグ ダークテーマ`() = captureWithBackground(ReadoutBackground.FLAG, darkTheme = true)

    @Test
    fun `背景画像 バーチャルエネルギー`() = captureWithBackground(ReadoutBackground.VIRTUAL_ENERGY, darkTheme = false)

    @Test
    fun `背景画像 バーチャルエネルギー ダークテーマ`() = captureWithBackground(ReadoutBackground.VIRTUAL_ENERGY, darkTheme = true)

    @Test
    fun `背景画像 車両接近`() = captureWithBackground(ReadoutBackground.VEHICLE_APPROACH, darkTheme = false)

    @Test
    fun `背景画像 車両接近 ダークテーマ`() = captureWithBackground(ReadoutBackground.VEHICLE_APPROACH, darkTheme = true)

    private fun captureWithBackground(
        background: ReadoutBackground,
        darkTheme: Boolean,
    ) = composeScreenshotTest {
        setContent {
            KoDriverTheme(darkTheme = darkTheme) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 64.dp)) {
                        DetailPaneTopAppBar(
                            title = "タイヤ温度",
                            canNavigateBack = true,
                            onBack = {},
                            background = background,
                        )
                    }
                }
            }
        }
        onAllNodes(isRoot()).get(0).captureRoboImage()
    }
}
