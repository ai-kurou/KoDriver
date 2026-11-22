package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Test

/** 再生ボタン表示のタイル背景画像を、画像の枚数（カテゴリ数）ごとと、ダークテーマ・不明なキーのみの場合で撮影する。 */
class ManualReadoutListPanePlayTileBackgroundScreenshotTest {
    init {
        registerAllManualReadoutSimulators()
    }

    @Test
    fun `背景画像が3枚の再生ボタン表示`() =
        capturePlayButtons(
            listOf(
                ManualReadoutSet(
                    id = "set-1",
                    name = "燃料とタイヤとブレーキ",
                    itemKeys =
                        listOf(
                            LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                            LmuWindowsReadoutItemKey.TyreWear.Root.value,
                            LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                        ),
                ),
            ),
        )

    @Test
    fun `同じカテゴリの3項目は背景画像が1枚の再生ボタン表示`() =
        capturePlayButtons(
            listOf(
                ManualReadoutSet(
                    id = "set-1",
                    name = "タイヤ3項目",
                    itemKeys =
                        listOf(
                            LmuWindowsReadoutItemKey.TyreWear.Root.value,
                            LmuWindowsReadoutItemKey.TyreTemperature.Root.value,
                            LmuWindowsReadoutItemKey.PitTiming.TyreWear.value,
                        ),
                ),
            ),
        )

    @Test
    fun `ダークテーマで背景画像が3枚の再生ボタン表示`() =
        capturePlayButtons(
            listOf(
                ManualReadoutSet(
                    id = "set-1",
                    name = "燃料とタイヤとブレーキ",
                    itemKeys =
                        listOf(
                            LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                            LmuWindowsReadoutItemKey.TyreWear.Root.value,
                            LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                        ),
                ),
            ),
            darkTheme = true,
        )

    @Test
    fun `不明なキーだけのセットは背景画像なしで項目数を表示する再生ボタン表示`() =
        capturePlayButtons(
            listOf(ManualReadoutSet(id = "set-1", name = "不明なキー", itemKeys = listOf("unknown-1", "unknown-2"))),
        )

    @OptIn(ExperimentalTestApi::class)
    private fun capturePlayButtons(
        sets: List<ManualReadoutSet>,
        darkTheme: Boolean = false,
    ) = composeScreenshotTest {
        setContent {
            KoDriverTheme(darkTheme = darkTheme) {
                Surface {
                    Box(modifier = Modifier.requiredSize(360.dp, 1080.dp)) {
                        ManualReadoutListPaneContent(
                            uiState =
                                ManualReadoutListUiState(
                                    playButtonLayoutEnabled = true,
                                    canReadSets = true,
                                    sets = sets,
                                ),
                            simulatorId = "lmu_windows",
                            simulatorName = "LMU",
                            onAddSet = {},
                            onReadSet = {},
                            onTogglePlayButtonLayout = {},
                            showHotkeyChip = true,
                        )
                    }
                }
            }
        }
        onRoot().captureRoboImage()
    }
}
