package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kurou.kodriver.core.designsystem.KoDriverTheme
import kotlin.test.Test

class AutoReadoutListPaneStateRestorationTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `状態復元後も読み上げ優先度ヒントのシートの表示状態が維持される`() =
        runComposeUiTest {
            var registry by mutableStateOf(SaveableStateRegistry(null) { true })
            var visible by mutableStateOf(true)
            setContent {
                if (visible) {
                    CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                        KoDriverTheme {
                            AutoReadoutListPane(
                                uiState = AutoReadoutListUiState(),
                                onMove = { _, _ -> },
                                onReadoutEnabledChanged = { _, _ -> },
                                onQueueEnabledChanged = { _, _ -> },
                                onStartSoundEnabledChanged = { _, _ -> },
                                onItemClick = {},
                            )
                        }
                    }
                }
            }
            val startSoundHint = "読み上げ開始音アイコンをONにすると、その項目の読み上げ前に開始音を鳴らします"
            onNodeWithText(startSoundHint).assertDoesNotExist()
            onNode(hasContentDescription("一覧の上にある項目ほど優先度が高くなります", substring = true)).performClick()
            onNodeWithText(startSoundHint).assertIsDisplayed()

            val saved = registry.performSave()
            visible = false
            waitForIdle()
            registry = SaveableStateRegistry(saved) { true }
            visible = true
            waitForIdle()

            waitUntilExactlyOneExists(hasText(startSoundHint))
            onNodeWithText(startSoundHint).assertIsDisplayed()
        }
}
