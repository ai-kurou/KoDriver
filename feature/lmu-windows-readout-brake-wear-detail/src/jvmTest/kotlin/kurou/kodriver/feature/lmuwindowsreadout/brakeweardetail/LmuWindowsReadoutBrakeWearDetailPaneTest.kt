package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.WheelIndex
import org.junit.Rule
import org.junit.Test

class LmuWindowsReadoutBrakeWearDetailPaneTest {
    @get:Rule
    val rule = createComposeRule()

    private val remaining =
        LmuWindowsBrakeWearRemainingData(
            wheels =
                mapOf(
                    WheelIndex.FRONT_LEFT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0305f), 50),
                    WheelIndex.FRONT_RIGHT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0310f), 55),
                    WheelIndex.REAR_LEFT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0330f), 73),
                    WheelIndex.REAR_RIGHT to LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0335f), 77),
                ),
        )

    @Test
    fun `説明文とカードのタイトルが表示される`() {
        rule.setContent { KoDriverTheme { LmuWindowsReadoutBrakeWearDetailPaneContent() } }

        rule
            .onNodeWithText(
                "ブレーキの残量を4輪それぞれ%と厚さ（mm）で表示します。" +
                    "LMUのREST APIから取得するため、デスクトップ版でLMUを起動しているときのみ取得できます。\n" +
                    "新品時の厚さは取得できないため、観測した最大の厚さを100%とします。" +
                    "摩耗した状態で観測を始めた場合、実際より多く表示されます。",
            ).assertIsDisplayed()
        rule.onNodeWithText("ブレーキ残量").assertIsDisplayed()
    }

    @Test
    fun `値を取得できない場合は取得できませんと表示する`() {
        rule.setContent { KoDriverTheme { LmuWindowsReadoutBrakeWearDetailPaneContent() } }

        rule.onNodeWithText("取得できません").assertIsDisplayed()
    }

    @Test
    fun `4輪の残量と厚さを輪ごとのラベルで表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState = LmuWindowsReadoutBrakeWearDetailUiState(remaining = remaining),
                )
            }
        }

        rule.onNodeWithText("FL: 50%（30.5 mm）").assertIsDisplayed()
        rule.onNodeWithText("FR: 55%（31.0 mm）").assertIsDisplayed()
        rule.onNodeWithText("RL: 73%（33.0 mm）").assertIsDisplayed()
        rule.onNodeWithText("RR: 77%（33.5 mm）").assertIsDisplayed()
    }

    @Test
    fun `一部の輪の値がない場合は取得できた輪だけ表示する`() {
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutBrakeWearDetailPaneContent(
                    uiState =
                        LmuWindowsReadoutBrakeWearDetailUiState(
                            remaining =
                                LmuWindowsBrakeWearRemainingData(
                                    wheels =
                                        mapOf(
                                            WheelIndex.REAR_RIGHT to remaining.wheels.getValue(WheelIndex.REAR_RIGHT),
                                        ),
                                ),
                        ),
                )
            }
        }

        rule.onNodeWithText("RR: 77%（33.5 mm）").assertIsDisplayed()
        rule.onNodeWithText("FL: 50%（30.5 mm）").assertDoesNotExist()
    }
}
