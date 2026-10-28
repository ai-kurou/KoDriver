package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AutoReadoutListPaneHapticTest {
    private val haptic: HapticFeedback = mockk()
    private val item = LmuWindowsReadoutItemKey.VehicleApproach.Root
    private val selections = mutableListOf<ReadoutItemKey>()

    init {
        registerAllReadoutItemKeys()
    }

    @get:Rule
    val rule = createComposeRule()

    private fun showPane() {
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    AutoReadoutListPane(
                        uiState =
                            AutoReadoutListUiState(
                                selectedSimulator = Simulator.LmuWindows,
                                items = listOf(item),
                                autoReadoutEnabledStates = mapOf(item to true),
                            ),
                        onMove = { _, _ -> },
                        onReadoutEnabledChanged = { _, _ -> },
                        onQueueEnabledChanged = { _, _ -> },
                        onStartSoundEnabledChanged = { _, _ -> },
                        onItemClick = { selections += it },
                    )
                }
            }
        }
    }

    @Test
    fun `シェブロンのタップは詳細選択と触覚を1回だけ返す`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        showPane()

        rule.onNodeWithTag("autoReadoutListChevronTouchTarget:${item.value}").performClick()

        assertEquals(listOf<ReadoutItemKey>(item), selections)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `項目名のタップは既存の詳細選択と触覚を1回だけ返す`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        showPane()

        rule.onNodeWithContentDescription("車両接近").performClick()

        assertEquals(listOf<ReadoutItemKey>(item), selections)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `キュー非対応の空白タップは既存の詳細選択と触覚を1回だけ返す`() {
        every { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) } returns Unit
        showPane()

        rule.onNodeWithTag("autoReadoutListQueueBlankTouchTarget:${item.value}").performClick()

        assertEquals(listOf<ReadoutItemKey>(item), selections)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }
}
