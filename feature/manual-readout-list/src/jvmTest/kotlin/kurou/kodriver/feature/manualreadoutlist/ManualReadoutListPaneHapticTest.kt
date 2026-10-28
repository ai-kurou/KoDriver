package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutListPaneHapticTest {
    private val haptic: HapticFeedback = mockk()

    @get:Rule
    val rule = createComposeRule()

    private val state = mutableStateOf(ManualReadoutListUiState(playButtonLayoutEnabled = false))
    private var additions = 0
    private val selections = mutableListOf<String>()
    private val readRequests = mutableListOf<String>()

    private fun showPane() {
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    ManualReadoutListPaneContent(
                        uiState = state.value,
                        simulatorId = "lmu_windows",
                        simulatorName = "LMU",
                        onAddSet = { additions++ },
                        onReadSet = { readRequests.add(it) },
                        onTogglePlayButtonLayout = {
                            state.value =
                                state.value.copy(playButtonLayoutEnabled = !state.value.playButtonLayoutEnabled)
                        },
                        onSetClick = {
                            selections.add(it)
                            state.value = state.value.copy(selectedSetId = it)
                        },
                        showHotkeyChip = false,
                    )
                }
            }
        }
    }

    @Test
    fun `標準カードの再生は1回だけ触覚を返し選択しない`() {
        assertPlaybackHaptic(playButtonLayoutEnabled = false)
    }

    @Test
    fun `再生タイルの再生は1回だけ触覚を返し選択しない`() {
        assertPlaybackHaptic(playButtonLayoutEnabled = true)
    }

    private fun assertPlaybackHaptic(playButtonLayoutEnabled: Boolean) {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        state.value =
            state.value.copy(
                playButtonLayoutEnabled = playButtonLayoutEnabled,
                canReadSets = true,
                sets = listOf(ManualReadoutSet("first", itemKeys = listOf("SESSION"))),
            )
        showPane()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()

        assertEquals(listOf("first"), readRequests)
        assertEquals(emptyList<String>(), selections)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `再生不可の標準カードと再生タイルは触覚を返さない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        state.value =
            state.value.copy(
                canReadSets = false,
                sets = listOf(ManualReadoutSet("first", itemKeys = listOf("SESSION"))),
            )
        showPane()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()
        rule.runOnIdle { state.value = state.value.copy(playButtonLayoutEnabled = true) }
        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()

        assertEquals(emptyList<String>(), readRequests)
        assertEquals(emptyList<String>(), selections)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `項目なしの再生タイルは触覚を返さない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        state.value =
            state.value.copy(
                playButtonLayoutEnabled = true,
                canReadSets = true,
                sets = listOf(ManualReadoutSet("first")),
            )
        showPane()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()

        assertEquals(emptyList<String>(), readRequests)
        assertEquals(emptyList<String>(), selections)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `追加FABと追加タイルは操作ごとに1回だけ触覚を返す`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        showPane()
        rule.onNodeWithContentDescription("セットを追加").performClick()
        rule.runOnIdle { state.value = state.value.copy(playButtonLayoutEnabled = true) }
        rule.onNodeWithText("セットを追加").performClick()

        assertEquals(2, additions)
        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `表示切替は未選択側の操作だけ触覚を返す`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        showPane()
        rule.onNodeWithContentDescription("標準表示").performClick()
        rule.onNodeWithContentDescription("再生ボタン表示").performClick()
        rule.onNodeWithContentDescription("再生ボタン表示").performClick()
        rule.onNodeWithContentDescription("標準表示").performClick()

        verify(exactly = 2) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `編集タップは異なるセットの選択で触覚を返し長押しの既存触覚と重複しない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        state.value =
            state.value.copy(
                playButtonLayoutEnabled = true,
                sets = listOf(ManualReadoutSet("first", name = "セットA"), ManualReadoutSet("second", name = "セットB")),
            )
        showPane()
        rule.onNodeWithText("編集").performClick()
        rule.onNodeWithText("セットA").performClick()
        rule.onNodeWithText("セットA").performClick()
        rule.onNodeWithText("セットB").performClick()
        rule.onNodeWithText("セットA").performSemanticsAction(SemanticsActions.OnLongClick)

        assertEquals(listOf("first", "first", "second", "first"), selections)
        assertEquals(emptyList<String>(), readRequests)
        verify(exactly = 3) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `標準行の既存触覚は1回で状態更新だけでは触覚を返さない`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        showPane()
        rule.runOnIdle {
            state.value = state.value.copy(sets = listOf(ManualReadoutSet("first", name = "セットA")))
        }
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        rule.onNodeWithText("セットA").performClick()

        assertEquals(listOf("first"), selections)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}
