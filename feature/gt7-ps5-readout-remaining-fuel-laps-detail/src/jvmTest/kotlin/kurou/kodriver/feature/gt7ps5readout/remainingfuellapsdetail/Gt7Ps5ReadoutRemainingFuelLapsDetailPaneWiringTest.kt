package kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
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
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ReadoutRemainingFuelLapsDetailPaneWiringTest {
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

    private val viewModel: Gt7Ps5ReadoutRemainingFuelLapsDetailViewModel = mockk()

    @Test
    fun `Paneは保存通知を待たず表示中の閾値をViewModelの試聴へ渡す`() {
        every { viewModel.uiState } returns
            MutableStateFlow(Gt7Ps5ReadoutRemainingFuelLapsDetailUiState(isTextToSpeechAvailable = true))
        every { viewModel.onRemainingFuelLapsChanged(5) } returns Unit
        every { viewModel.onReadoutTextPreviewClicked(GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, 5) } returns
            Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                KoDriverTheme { Gt7Ps5ReadoutRemainingFuelLapsDetailPane() }
            }
            rule
                .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(3f, 1f..5f, 3)))
                .performScrollTo()
                .performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
            rule.onAllNodesWithContentDescription("入力した文言を再生")[0].performScrollTo().performClick()
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onRemainingFuelLapsChanged(5) }
            verify(
                exactly = 1,
            ) { viewModel.onReadoutTextPreviewClicked(GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, 5) }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
