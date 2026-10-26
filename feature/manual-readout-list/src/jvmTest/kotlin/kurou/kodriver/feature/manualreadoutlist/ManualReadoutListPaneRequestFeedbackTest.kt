package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutListPaneRequestFeedbackTest {
    private val onReadSet: (String) -> Unit = mockk()
    private val onSetClick: (String) -> Unit = mockk()

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `再生タイルは依頼中表示から約1秒半後に再生アイコンに戻り連打で延長する`() {
        assertRequestFeedback(playButtonLayoutEnabled = true)
    }

    @Test
    fun `標準表示は依頼中表示から約1秒半後に再生アイコンに戻り連打で延長する`() {
        assertRequestFeedback(playButtonLayoutEnabled = false)
    }

    @Test
    fun `編集モードのタイルタップでは依頼中表示を出さない`() {
        val readSetIds = mutableListOf<String>()
        val clickedSetIds = mutableListOf<String>()
        showPane(
            playButtonLayoutEnabled = true,
            onReadSet = { readSetIds += it },
            onSetClick = { clickedSetIds += it },
        )

        rule.onNodeWithContentDescription("編集モードにする").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithText("LMU(Windows)セット1").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription("読み上げを依頼しました").assertDoesNotExist()
        assertEquals(emptyList<String>(), readSetIds)
        assertEquals(listOf("set-1"), clickedSetIds)
    }

    @Test
    fun `項目なしの再生タイルでは依頼中表示を出さない`() {
        showPane(playButtonLayoutEnabled = true, itemKeys = emptyList())
        assertNoRequestFeedback()
    }

    @Test
    fun `再生不可の再生タイルでは依頼中表示を出さない`() {
        showPane(playButtonLayoutEnabled = true, canReadSets = false)
        assertNoRequestFeedback()
    }

    @Test
    fun `再生不可の標準表示では依頼中表示を出さない`() {
        showPane(playButtonLayoutEnabled = false, canReadSets = false)
        assertNoRequestFeedback()
    }

    private fun assertRequestFeedback(playButtonLayoutEnabled: Boolean) {
        every { onReadSet("set-1") } returns Unit
        showPane(playButtonLayoutEnabled)

        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription("読み上げを依頼しました").assertIsDisplayed()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertDoesNotExist()
        rule.mainClock.advanceTimeBy(1_600)
        rule.onNodeWithContentDescription("読み上げを依頼しました").assertDoesNotExist()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsDisplayed().performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithContentDescription("読み上げを依頼しました").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithContentDescription("読み上げを依頼しました").assertIsDisplayed()
        rule.mainClock.advanceTimeBy(600)
        rule.onNodeWithContentDescription("読み上げを依頼しました").assertDoesNotExist()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsDisplayed()
        verify(exactly = 3) { onReadSet("set-1") }
        verify(exactly = 0) { onSetClick("set-1") }
        confirmVerified(onReadSet, onSetClick)
    }

    private fun assertNoRequestFeedback() {
        rule.onNodeWithContentDescription("セット 1 を読み上げ").performClick()
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription("読み上げを依頼しました").assertDoesNotExist()
        rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsDisplayed()
        verify(exactly = 0) { onReadSet("set-1") }
        confirmVerified(onReadSet)
    }

    private fun showPane(
        playButtonLayoutEnabled: Boolean,
        canReadSets: Boolean = true,
        itemKeys: List<String> = listOf("SESSION"),
        onReadSet: (String) -> Unit = this.onReadSet,
        onSetClick: (String) -> Unit = this.onSetClick,
    ) {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            KoDriverTheme {
                ManualReadoutListPaneContent(
                    uiState =
                        ManualReadoutListUiState(
                            playButtonLayoutEnabled = playButtonLayoutEnabled,
                            canReadSets = canReadSets,
                            sets = listOf(ManualReadoutSet("set-1", itemKeys = itemKeys)),
                        ),
                    simulatorId = "lmu_windows",
                    simulatorName = "LMU",
                    onAddSet = {},
                    onReadSet = onReadSet,
                    onSetClick = onSetClick,
                    showHotkeyChip = false,
                )
            }
        }
        rule.mainClock.advanceTimeBy(300)
    }
}
