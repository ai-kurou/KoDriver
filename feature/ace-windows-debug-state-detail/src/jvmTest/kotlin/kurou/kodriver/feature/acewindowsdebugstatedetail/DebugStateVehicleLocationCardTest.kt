package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.DebugStateCardKey
import org.junit.Rule
import org.junit.Test

class DebugStateVehicleLocationCardTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `ACEでステータス情報が未取得の場合は未取得の文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                AceWindowsDebugStateDetailPaneContent(
                    uiState =
                        AceWindowsDebugStateDetailUiState(
                            cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
                        ),
                    canNavigateBack = true,
                    onBack = {},
                )
            }
        }

        rule.onNodeWithText("車両位置").assertIsDisplayed()
        rule.onNodeWithText("未取得").assertIsDisplayed()
    }

    @Test
    fun `ACEの場合はstatusとcarLocationの各値に対応する表示文言を表示する`() {
        val expectedStatuses =
            listOf(
                AceWindowsStatusType.OFF to "Off（未起動）",
                AceWindowsStatusType.REPLAY to "Replay（リプレイ中）",
                AceWindowsStatusType.LIVE to "Live（走行中）",
                AceWindowsStatusType.PAUSE to "Pause（ポーズ中）",
                AceWindowsStatusType.UNKNOWN to "不明",
            )
        val expectedCarLocations =
            listOf(
                AceWindowsCarLocation.UNASSIGNED to "Unassigned（未割当）",
                AceWindowsCarLocation.PITLANE to "PitLane（ピットレーン）",
                AceWindowsCarLocation.PITENTRY to "PitEntry（ピット進入）",
                AceWindowsCarLocation.PITEXIT to "PitExit（ピット退出）",
                AceWindowsCarLocation.TRACK to "Track（コース上）",
                AceWindowsCarLocation.UNKNOWN to "不明",
            )

        // statusとcarLocationの数が異なるため、少ない方のstatusを循環させて両方の全enum値を1ループで網羅する。
        expectedCarLocations.forEachIndexed { index, (carLocation, expectedCarLocationText) ->
            val (status, expectedStatusText) = expectedStatuses[index % expectedStatuses.size]
            rule.setContent {
                MaterialTheme {
                    AceWindowsDebugStateDetailPaneContent(
                        uiState =
                            AceWindowsDebugStateDetailUiState(
                                cardOrder = listOf(DebugStateCardKey.VEHICLE_LOCATION),
                                aceWindows =
                                    AceWindowsDebugState(
                                        status = AceWindowsStatusData(status = status, carLocation = carLocation),
                                    ),
                            ),
                        canNavigateBack = true,
                        onBack = {},
                    )
                }
            }

            rule.onNodeWithText("車両位置").assertIsDisplayed()
            rule.onNodeWithText("ステータス: $expectedStatusText").assertIsDisplayed()
            rule.onNodeWithText(expectedCarLocationText).assertIsDisplayed()
        }
    }

    @Test
    fun `statusとcarLocationが両方不明の場合もそれぞれの文言を表示する`() {
        rule.setContent {
            MaterialTheme {
                VehicleLocationContent(
                    AceWindowsStatusData(
                        status = AceWindowsStatusType.UNKNOWN,
                        carLocation = AceWindowsCarLocation.UNKNOWN,
                    ),
                )
            }
        }

        rule.onNodeWithText("ステータス: 不明").assertIsDisplayed()
        rule.onNodeWithText("不明").assertIsDisplayed()
    }
}
