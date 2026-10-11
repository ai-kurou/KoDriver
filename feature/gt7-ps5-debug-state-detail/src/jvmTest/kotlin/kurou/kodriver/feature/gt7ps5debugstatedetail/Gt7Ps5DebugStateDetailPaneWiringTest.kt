package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5DebugStateDetailPaneWiringTest {
    private val rule = createComposeRule()

    @get:Rule
    val rules: TestRule =
        RuleChain
            .outerRule(
                object : ExternalResource() {
                    override fun before() {
                        Dispatchers.setMain(UnconfinedTestDispatcher())
                    }

                    override fun after() {
                        Dispatchers.resetMain()
                    }
                },
            ).around(rule)

    private val viewModel: Gt7Ps5DebugStateDetailViewModel = mockk()
    private val onBack: () -> Unit = mockk()

    @Test
    fun `PaneはKoinの状態と更新を表示して戻る操作を通知する`() {
        val state = MutableStateFlow(Gt7Ps5DebugStateDetailUiState())
        every { viewModel.uiState } returns state
        every { onBack() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme {
                    Gt7Ps5DebugStateDetailPane(
                        true,
                        onBack,
                        Modifier.requiredSize(900.dp, 600.dp).testTag("gt7-debug-pane"),
                    )
                }
            }
            rule.onNodeWithTag("gt7-debug-pane").assertExists()
            rule.onNodeWithText("Gran Turismo 7（PS5）").assertIsDisplayed()
            rule.runOnIdle {
                state.update {
                    it.copy(
                        enabledCardKeys = gt7Ps5SupportedCardKeys,
                        gt7Ps5 = Gt7Ps5DebugState(sampleGt7Ps5Telemetry(), Gt7Ps5VehicleClassData("GR3")),
                    )
                }
            }
            rule.onNodeWithText("GR3").assertIsDisplayed()
            rule.onNodeWithText("1:30.000").assertIsDisplayed()
            rule.onNodeWithContentDescription("戻る").performClick()

            // 状態取得は表示のためのスタブ。戻るコールバックの配線を検証する。
            verify(exactly = 1) { onBack() }
            confirmVerified(onBack)
        } finally {
            stopKoin()
        }
    }
}
