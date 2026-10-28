package kurou.kodriver.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private class HintRowHapticFeedback : HapticFeedback {
    val performedTypes = mutableListOf<HapticFeedbackType>()

    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        performedTypes += hapticFeedbackType
    }
}

class KoDriverHintRowTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `末尾テキストを表示しヘルプボタンでシートを開く`() {
        rule.setContent {
            KoDriverTheme {
                KoDriverHintRow(
                    label = "読み上げ優先度",
                    helpContentDescription = "優先度の説明",
                    trailingText = "上から順",
                ) {
                    KoDriverHintSheetRow(
                        icon = Icons.Default.Info,
                        title = "並べ替え",
                        description = "ドラッグで並べ替えます",
                    )
                }
            }
        }

        rule.onNodeWithText("読み上げ優先度").assertIsDisplayed()
        rule.onNodeWithText("上から順").assertIsDisplayed()
        rule.onNodeWithText("並べ替え").assertDoesNotExist()
        rule.onNodeWithContentDescription("優先度の説明").performClick()
        rule.onNodeWithText("並べ替え").assertIsDisplayed()
        rule.onNodeWithText("ドラッグで並べ替えます").assertIsDisplayed()
    }

    @Test
    fun `末尾テキストを省略してシートを開くたびにContextClickを発生させる`() {
        val haptic = HintRowHapticFeedback()
        rule.setContent {
            KoDriverTheme {
                CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                    KoDriverHintRow(
                        label = "手動読み上げ",
                        helpContentDescription = "操作の説明",
                    ) {
                        Text("セットを読み上げます")
                    }
                }
            }
        }

        rule.onNodeWithText("手動読み上げ").assertIsDisplayed()
        rule.onNodeWithText("セットを読み上げます").assertDoesNotExist()
        assertEquals(emptyList(), haptic.performedTypes)
        rule.onNodeWithContentDescription("操作の説明").performClick()
        rule.onNodeWithText("セットを読み上げます").assertIsDisplayed()
        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
        rule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            .performSemanticsAction(SemanticsActions.Dismiss)
        rule.onNodeWithText("セットを読み上げます").assertDoesNotExist()
        assertEquals(listOf(HapticFeedbackType.ContextClick), haptic.performedTypes)
        rule.onNodeWithContentDescription("操作の説明").performClick()
        rule.onNodeWithText("セットを読み上げます").assertIsDisplayed()
        assertEquals(listOf(HapticFeedbackType.ContextClick, HapticFeedbackType.ContextClick), haptic.performedTypes)
    }
}
