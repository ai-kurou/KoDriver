package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutVehicleApproachDetailPaneTest {
    @Test
    fun `ヘルプボタンをタップするとヘルプシートが表示される`() =
        composeScreenshotTest {
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(),
                    )
                }
            }

            onNode(hasContentDescription("閾値の説明を表示")).performClick()

            onNode(hasText("閾値は自車中心から相手車両中心までの距離です", substring = true)).assertIsDisplayed()
        }

    @Test
    fun `左接近・右接近チップをタップするとonStartReadoutTypeChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedType: VehicleApproachStartReadoutType? = null
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState =
                            LmuWindowsReadoutVehicleApproachDetailUiState(
                                startReadoutEnabled = true,
                                startReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
                            ),
                        onStartReadoutTypeChanged = { changedType = it },
                    )
                }
            }

            onNode(hasText("左接近・右接近")).performClick()

            assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, changedType)
        }

    @Test
    fun `縦方向スライダーの値を確定するとonLongitudinalThresholdChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedValue: Double? = null
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(longitudinalThresholdMeters = 1.0),
                        onLongitudinalThresholdChanged = { changedValue = it },
                    )
                }
            }

            onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 1.0f, range = 0.1f..10f, steps = 98)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }

            assertEquals(5.0, changedValue)
        }

    @Test
    fun `横方向スライダーの値を確定するとonLateralThresholdChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedValue: Double? = null
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(lateralThresholdMeters = 5.0),
                        onLateralThresholdChanged = { changedValue = it },
                    )
                }
            }

            onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 5.0f, range = 2f..8f, steps = 59)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }

            assertEquals(4.0, changedValue)
        }

    @Test
    fun `継続接近時間スライダーの値を確定するとonSustainedApproachDurationSecondsChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedValue: Int? = null
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(sustainedApproachDurationSeconds = 4),
                        onSustainedApproachDurationSecondsChanged = { changedValue = it },
                    )
                }
            }

            onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo(current = 4.0f, range = 4f..10f, steps = 5)),
            ).performSemanticsAction(SemanticsActions.SetProgress) { it(8f) }

            assertEquals(8, changedValue)
        }

    @Test
    fun `1周目の読み上げスキップスイッチをタップするとonSkipFirstLapChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedEnabled: Boolean? = null
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(skipFirstLap = true),
                        onSkipFirstLapChanged = { changedEnabled = it },
                    )
                }
            }

            onNode(hasContentDescription("フォーメーションラップ・1周目スキップ")).performClick()

            assertEquals(false, changedEnabled)
        }

    @Test
    fun `接近開始時の読み上げスイッチをタップするとonStartReadoutEnabledChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedEnabled: Boolean? = null
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(startReadoutEnabled = false),
                        onStartReadoutEnabledChanged = { changedEnabled = it },
                    )
                }
            }

            onNode(hasText("接近開始時の読み上げ")).performClick()

            assertEquals(true, changedEnabled)
        }

    @Test
    fun `接近継続時の読み上げスイッチをタップするとonSustainedReadoutEnabledChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedEnabled: Boolean? = null
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(sustainedReadoutEnabled = false),
                        onSustainedReadoutEnabledChanged = { changedEnabled = it },
                    )
                }
            }

            onNode(hasText("接近継続時の読み上げ")).performClick()

            assertEquals(true, changedEnabled)
        }

    @Test
    fun `左側維持・右側維持チップをタップするとonSustainedReadoutTypeChangedが呼ばれる`() =
        composeScreenshotTest {
            var changedType: VehicleApproachSustainedReadoutType? = null
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState =
                            LmuWindowsReadoutVehicleApproachDetailUiState(
                                sustainedReadoutEnabled = true,
                                sustainedReadoutType = VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
                            ),
                        onSustainedReadoutTypeChanged = { changedType = it },
                    )
                }
            }

            onNode(hasText("左側維持・右側維持")).performClick()

            assertEquals(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED, changedType)
        }

    @Test
    fun `縦方向閾値のリセットボタンをタップするとonResetLongitudinalThresholdが呼ばれる`() =
        composeScreenshotTest {
            var resetCalled = false
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(longitudinalThresholdMeters = 8.0),
                        onResetLongitudinalThreshold = { resetCalled = true },
                    )
                }
            }

            onAllNodes(hasContentDescription("デフォルト値にリセット"))[0].performClick()

            assertEquals(true, resetCalled)
        }

    @Test
    fun `横方向閾値のリセットボタンをタップするとonResetLateralThresholdが呼ばれる`() =
        composeScreenshotTest {
            var resetCalled = false
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(lateralThresholdMeters = 7.0),
                        onResetLateralThreshold = { resetCalled = true },
                    )
                }
            }

            onAllNodes(hasContentDescription("デフォルト値にリセット"))[1].performClick()

            assertEquals(true, resetCalled)
        }

    @Test
    fun `継続接近時間のリセットボタンをタップするとonResetSustainedApproachDurationSecondsが呼ばれる`() =
        composeScreenshotTest {
            var resetCalled = false
            setContent {
                MaterialTheme(colorScheme = lightColorScheme()) {
                    LmuWindowsReadoutVehicleApproachDetailPaneContent(
                        uiState = LmuWindowsReadoutVehicleApproachDetailUiState(sustainedApproachDurationSeconds = 8),
                        onResetSustainedApproachDurationSeconds = { resetCalled = true },
                    )
                }
            }

            onAllNodes(hasContentDescription("デフォルト値にリセット"))[2].performClick()

            assertEquals(true, resetCalled)
        }
}
