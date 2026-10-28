package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class OtherServerIpDetailPaneContentTest {
    @get:Rule
    val rule = createComposeRule()

    private val haptic: HapticFeedback = mockk(relaxUnitFun = true)

    private fun setContent(
        uiState: OtherServerIpDetailUiState = OtherServerIpDetailUiState(inputIp = "192.168.1.1", isInputValid = true),
        onIpChanged: (String) -> Unit = {},
        onSave: () -> Unit = {},
        onSaveAnyway: () -> Unit = {},
        onDismiss: () -> Unit = {},
        onOpenGuide: () -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                OtherServerIpDetailPaneContent(
                    uiState = uiState,
                    onIpChanged = onIpChanged,
                    onSave = onSave,
                    onSaveAnyway = onSaveAnyway,
                    onDismiss = onDismiss,
                    onOpenGuide = onOpenGuide,
                    onBack = onBack,
                )
            }
        }
    }

    @Test
    fun `保存ボタンをクリックするとonSaveが呼ばれる`() {
        var saveCount = 0
        setContent(
            onSave = {
                verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
                saveCount++
            },
        )

        rule.onNodeWithText("保存").performClick()

        assertEquals(1, saveCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `このまま保存ボタンをクリックするとonSaveAnywayが呼ばれる`() {
        var saveAnywayCount = 0
        setContent(
            uiState =
                OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    isInputValid = true,
                    connectivityWarning = true,
                ),
            onSaveAnyway = {
                verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
                saveAnywayCount++
            },
        )

        rule.onNodeWithText("このまま保存").performClick()

        assertEquals(1, saveAnywayCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `isSavedがtrueになるとonDismissとonBackが呼ばれる`() {
        var dismissCount = 0
        var backCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(isSaved = true),
            onDismiss = { dismissCount++ },
            onBack = { backCount++ },
        )

        rule.waitForIdle()

        assertEquals(1, dismissCount)
        assertEquals(1, backCount)
    }

    @Test
    fun `インストールガイドリンクをクリックするとonOpenGuideが呼ばれる`() {
        var guideCount = 0
        setContent(onOpenGuide = { guideCount++ })

        rule.onNodeWithText("インストールガイドを開く").performClick()

        assertEquals(1, guideCount)
    }

    @Test
    fun `説明文のWindows版KoDriverリンクをクリックするとonOpenGuideが呼ばれる`() {
        var guideCount = 0
        setContent(onOpenGuide = { guideCount++ })

        val node = rule.onNodeWithText("が動作しているPC", substring = true)
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        node
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(textLayoutResults)
        val textLayoutResult = textLayoutResults.first()
        val linkStart =
            textLayoutResult.layoutInput.text.text
                .indexOf("Windows版KoDriver")
        val boundingBox = textLayoutResult.getBoundingBox(linkStart)
        node.performTouchInput { click(boundingBox.center) }

        assertEquals(1, guideCount)
    }

    @Test
    fun `入力欄でEnterキーを押すとonSaveが呼ばれる`() {
        var saveCount = 0
        setContent(
            onSave = {
                verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
                saveCount++
            },
        )

        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(1, saveCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `接続警告表示中に入力欄でEnterキーを押すとonSaveAnywayが呼ばれる`() {
        var saveAnywayCount = 0
        setContent(
            uiState =
                OtherServerIpDetailUiState(
                    inputIp = "192.168.1.1",
                    isInputValid = true,
                    connectivityWarning = true,
                ),
            onSaveAnyway = {
                verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
                saveAnywayCount++
            },
        )

        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(1, saveAnywayCount)
        verify(exactly = 1) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `保存ボタンが無効な状態で入力欄でEnterキーを押してもonSaveは呼ばれない`() {
        var saveCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(inputIp = "invalid", isInputValid = false),
            onSave = { saveCount++ },
        )

        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(0, saveCount)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `空入力では保存ボタンとIME保存でコールバックも触覚も発生しない`() {
        var saveCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(inputIp = ""),
            onSave = { saveCount++ },
        )

        rule.onNodeWithText("保存").assertIsNotEnabled()
        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(0, saveCount)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `不正な入力では保存ボタンとIME保存でコールバックも触覚も発生しない`() {
        var saveCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(inputIp = "invalid", isInputValid = false),
            onSave = { saveCount++ },
        )

        rule.onNodeWithText("保存").assertIsNotEnabled()
        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(0, saveCount)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `接続確認中では保存ボタンとIME保存でコールバックも触覚も発生しない`() {
        var saveCount = 0
        setContent(
            uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.1", isCheckingConnectivity = true),
            onSave = { saveCount++ },
        )

        rule.onNodeWithText("保存").assertIsNotEnabled()
        rule.onNodeWithText("IPアドレス").performImeAction()

        assertEquals(0, saveCount)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `通常の文字入力では触覚が発生しない`() {
        val inputs = mutableListOf<String>()
        setContent(
            uiState = OtherServerIpDetailUiState(inputIp = ""),
            onIpChanged = { inputs.add(it) },
        )

        rule.onNodeWithText("IPアドレス").performTextInput("192.168.1.2")

        assertEquals(listOf("192.168.1.2"), inputs)
        verify(exactly = 0) { haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
        confirmVerified(haptic)
    }

    @Test
    fun `戻るボタンをクリックするとonDismissとonBackが呼ばれる`() {
        var dismissCount = 0
        var backCount = 0
        setContent(
            onDismiss = { dismissCount++ },
            onBack = { backCount++ },
        )

        rule.onNode(hasContentDescription("戻る")).performClick()

        assertEquals(1, dismissCount)
        assertEquals(1, backCount)
    }
}
