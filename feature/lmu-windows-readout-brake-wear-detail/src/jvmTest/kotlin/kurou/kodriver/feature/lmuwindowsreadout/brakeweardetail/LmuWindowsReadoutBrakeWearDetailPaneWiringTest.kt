package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutBrakeWearDetailPaneWiringTest {
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

    private val viewModel: LmuWindowsReadoutBrakeWearDetailViewModel = mockk()

    @Test
    fun `PaneはKoinの状態を表示し基準の設定とクリアを通知する`() {
        every { viewModel.uiState } returns
            MutableStateFlow(
                LmuWindowsReadoutBrakeWearDetailUiState(
                    current =
                        LmuWindowsBrakeWearInvestigationData(
                            wearablesBrakes = listOf(0.036, 0.035, 0.032, 0.031),
                        ),
                    baseline = LmuWindowsBrakeWearInvestigationData(wearablesBrakes = listOf(0.04)),
                ),
            )
        every { viewModel.onBaselineSet() } returns Unit
        every { viewModel.onBaselineCleared() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent { KoDriverTheme { LmuWindowsReadoutBrakeWearDetailPane() } }

            rule.onNodeWithText("FL: 0.03600000（基準との差 -0.00400000）").assertExists()
            rule.onNodeWithText("現在値を基準にする").performClick()
            rule.onNodeWithText("基準をクリア").performClick()

            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onBaselineSet() }
            verify(exactly = 1) { viewModel.onBaselineCleared() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
