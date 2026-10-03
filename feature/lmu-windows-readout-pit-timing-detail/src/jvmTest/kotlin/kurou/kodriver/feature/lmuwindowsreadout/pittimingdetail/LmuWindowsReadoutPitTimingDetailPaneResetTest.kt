package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LmuWindowsReadoutPitTimingDetailPaneResetTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `全4文言は編集するとリセットでき既定文言を表示して保存を通知する`() {
        val changes = mutableListOf<Pair<Int, String>>()
        setResetTestContent(changes = changes)

        defaultTexts.forEachIndexed { index, defaultText ->
            val editedText = "編集済み文言$index"
            rule.onAllNodes(hasSetTextAction())[index].performScrollTo().performTextReplacement(editedText)
            rule
                .onAllNodesWithContentDescription("デフォルトに戻す")[textResetIndices[index]]
                .performScrollTo()
                .assertIsEnabled()
                .performClick()
            rule.onAllNodes(hasSetTextAction())[index].assertEditableTextEquals(defaultText)
            rule
                .onAllNodesWithContentDescription("デフォルトに戻す")[textResetIndices[index]]
                .assertIsNotEnabled()
        }
        assertEquals(
            defaultTexts.flatMapIndexed { index, defaultText ->
                listOf(index to "編集済み文言$index", index to defaultText)
            },
            changes,
        )
    }

    @Test
    fun `全4文言は既定値と同じならリセットできず保存も通知しない`() {
        val changes = mutableListOf<Pair<Int, String>>()
        setResetTestContent(changes = changes)

        textResetIndices.forEach { index ->
            rule
                .onAllNodesWithContentDescription("デフォルトに戻す")[index]
                .performScrollTo()
                .assertIsNotEnabled()
                .performClick()
        }
        assertEquals(emptyList(), changes)
    }

    @Test
    fun `TTS利用不可では編集済みの全4文言をリセットできず保存も通知しない`() {
        val changes = mutableListOf<Pair<Int, String>>()
        setResetTestContent(
            initialState =
                LmuWindowsReadoutPitTimingDetailUiState(
                    virtualEnergyText = "編集済みエナジー",
                    virtualEnergyImminentText = "編集済み必須エナジー",
                    tyreWearText = "編集済みタイヤ",
                    tyreWearImminentText = "編集済み必須タイヤ",
                    isTextToSpeechAvailable = false,
                ),
            changes = changes,
        )

        textResetIndices.forEach { index ->
            rule
                .onAllNodesWithContentDescription("デフォルトに戻す")[index]
                .performScrollTo()
                .assertIsNotEnabled()
                .performClick()
        }
        assertEquals(emptyList(), changes)
    }

    @Test
    fun `両通常文言はlaps挿入直後にリセットすると表示も既定値に戻る`() {
        val changes = mutableListOf<Pair<Int, String>>()
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = LmuWindowsReadoutPitTimingDetailUiState(isTextToSpeechAvailable = true),
                    onVirtualEnergyTextChanged = { changes += 0 to it },
                    onTyreWearTextChanged = { changes += 2 to it },
                )
            }
        }

        listOf(0, 2).forEachIndexed { chipIndex, fieldIndex ->
            rule.onAllNodesWithText("{laps}を挿入")[chipIndex].performScrollTo().performClick()
            rule
                .onAllNodes(
                    hasSetTextAction(),
                )[fieldIndex]
                .assertEditableTextEquals(defaultTexts[fieldIndex] + "{laps}")
            rule
                .onAllNodesWithContentDescription("デフォルトに戻す")[textResetIndices[fieldIndex]]
                .performScrollTo()
                .assertIsEnabled()
                .performClick()
            rule.onAllNodes(hasSetTextAction())[fieldIndex].assertEditableTextEquals(defaultTexts[fieldIndex])
            rule
                .onAllNodesWithContentDescription("デフォルトに戻す")[textResetIndices[fieldIndex]]
                .assertIsNotEnabled()
        }
        assertEquals(
            listOf(0, 2).flatMap { index ->
                listOf(index to defaultTexts[index] + "{laps}", index to defaultTexts[index])
            },
            changes,
        )
    }

    private fun setResetTestContent(
        changes: MutableList<Pair<Int, String>>,
        initialState: LmuWindowsReadoutPitTimingDetailUiState =
            LmuWindowsReadoutPitTimingDetailUiState(isTextToSpeechAvailable = true),
    ) {
        var uiState by mutableStateOf(initialState)
        rule.setContent {
            KoDriverTheme {
                LmuWindowsReadoutPitTimingDetailPaneContent(
                    uiState = uiState,
                    onVirtualEnergyTextChanged = {
                        changes += 0 to it
                        uiState = uiState.copy(virtualEnergyText = it)
                    },
                    onVirtualEnergyImminentTextChanged = {
                        changes += 1 to it
                        uiState = uiState.copy(virtualEnergyImminentText = it)
                    },
                    onTyreWearTextChanged = {
                        changes += 2 to it
                        uiState = uiState.copy(tyreWearText = it)
                    },
                    onTyreWearImminentTextChanged = {
                        changes += 3 to it
                        uiState = uiState.copy(tyreWearImminentText = it)
                    },
                )
            }
        }
    }

    private fun SemanticsNodeInteraction.assertEditableTextEquals(expected: String) =
        assert(
            SemanticsMatcher("EditableText == $expected") {
                it.config.getOrNull(SemanticsProperties.EditableText)?.text == expected
            },
        )

    private val defaultTexts =
        listOf(
            LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
            LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
            LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT,
            LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
        )

    // 各カードでは文言2つの後に周回数スライダーのリセットが並ぶ。
    private val textResetIndices = listOf(0, 1, 3, 4)
}
