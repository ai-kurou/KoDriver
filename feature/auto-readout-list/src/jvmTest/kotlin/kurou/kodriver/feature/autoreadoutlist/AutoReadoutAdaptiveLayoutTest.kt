package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class AutoReadoutAdaptiveLayoutTest {
    init {
        registerAllReadoutItemKeys()
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `largeとextraLarge幅では未選択時は一覧のみで選択時は一覧と詳細を表示する`() {
        val selected = mutableStateOf(false)
        val width = mutableIntStateOf(1200)
        rule.setContent {
            val sizeClass = WindowSizeClass(minWidthDp = width.intValue, minHeightDp = 0)
            val directive = calculatePaneScaffoldDirective(WindowAdaptiveInfo(sizeClass, Posture()))
            KoDriverTheme {
                CompositionLocalProvider(LocalDensity provides Density(0.5f)) {
                    Box(modifier = Modifier.requiredSize(width.intValue.dp, 640.dp)) {
                        AutoReadoutContent(
                            uiState =
                                AutoReadoutListUiState(
                                    selectedSimulator = Simulator.LmuWindows,
                                    items = listOf(LmuWindowsReadoutItemKey.Flag.Root),
                                    selectedItem =
                                        if (selected.value) {
                                            AutoReadoutListItemType.fromId(
                                                Simulator.LmuWindows,
                                                LmuWindowsReadoutItemKey.Flag.Root,
                                            )
                                        } else {
                                            null
                                        },
                                ),
                            onMove = { _, _ -> },
                            onReadoutEnabledChanged = { _, _ -> },
                            onQueueEnabledChanged = { _, _ -> },
                            onStartSoundEnabledChanged = { _, _ -> },
                            onItemSelected = {},
                            onClearSelectedItem = {},
                            windowSizeClass = sizeClass,
                            scaffoldDirective = directive,
                            detailContent = { Text("詳細スロット") },
                        )
                    }
                }
            }
        }
        listOf(1200, 1600).forEach { widthDp ->
            rule.runOnIdle { width.intValue = widthDp }
            rule.onNodeWithText("読み上げ優先度").assertIsDisplayed()
            rule.onNodeWithText("詳細スロット").assertDoesNotExist()
            rule.runOnIdle { selected.value = true }
            rule.onNodeWithText("読み上げ優先度").assertIsDisplayed()
            rule.onNodeWithText("詳細スロット").assertIsDisplayed()
            rule.runOnIdle { selected.value = false }
            rule.onNodeWithText("読み上げ優先度").assertIsDisplayed()
            rule.onNodeWithText("詳細スロット").assertDoesNotExist()
        }
    }
}
