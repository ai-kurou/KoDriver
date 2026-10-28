package kurou.kodriver.core.designsystem

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class KoDriverDestructiveConfirmDialogTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `指定した文言を表示し確認ボタンはハプティックとonConfirmを一度だけ呼ぶ`() {
        val haptic = RecordingHapticFeedback()
        var confirmCount = 0
        var dismissCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverDestructiveConfirmDialog(
                        title = "ログを削除",
                        body = "選択したログを削除します。",
                        confirmLabel = "削除",
                        dismissLabel = "キャンセル",
                        onConfirm = { confirmCount++ },
                        onDismiss = { dismissCount++ },
                    )
                }
            }
        }

        rule.onNodeWithText("ログを削除").assertIsDisplayed()
        rule.onNodeWithText("選択したログを削除します。").assertIsDisplayed()
        rule.onNodeWithText("キャンセル").assertIsDisplayed()
        rule.onNodeWithText("削除").performSemanticsAction(SemanticsActions.OnClick) { it() }

        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
        assertEquals(1, confirmCount)
        assertEquals(0, dismissCount)
    }

    @Test
    fun `キャンセルボタンはハプティックを発生させずonDismissだけを呼ぶ`() {
        val haptic = RecordingHapticFeedback()
        var confirmCount = 0
        var dismissCount = 0
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                KoDriverTheme {
                    KoDriverDestructiveConfirmDialog(
                        title = "設定をリセット",
                        body = "Assetto Corsa EVO の設定をリセットします。",
                        confirmLabel = "リセット",
                        dismissLabel = "キャンセル",
                        onConfirm = { confirmCount++ },
                        onDismiss = { dismissCount++ },
                    )
                }
            }
        }

        rule.onNodeWithText("キャンセル").performSemanticsAction(SemanticsActions.OnClick) { it() }

        assertEquals(emptyList(), haptic.performedTypes)
        assertEquals(0, confirmCount)
        assertEquals(1, dismissCount)
    }

    private class RecordingHapticFeedback : HapticFeedback {
        val performedTypes = mutableListOf<HapticFeedbackType>()

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            performedTypes += hapticFeedbackType
        }
    }
}
