package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.LmuWindowsBrakeWearRemainingData
import kurou.kodriver.domain.model.LmuWindowsBrakeWearWheelRemaining
import kurou.kodriver.domain.model.WheelIndex
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
    fun `PaneはKoinのViewModelの状態を表示する`() {
        every { viewModel.uiState } returns
            MutableStateFlow(
                LmuWindowsReadoutBrakeWearDetailUiState(
                    remaining =
                        LmuWindowsBrakeWearRemainingData(
                            wheels =
                                mapOf(
                                    WheelIndex.FRONT_LEFT to
                                        LmuWindowsBrakeWearWheelRemaining(BrakeThicknessMeters(0.0305f), 50),
                                ),
                        ),
                ),
            )
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent { KoDriverTheme { LmuWindowsReadoutBrakeWearDetailPane() } }

            rule.onNodeWithText("FL: 50%（30.5 mm）").assertExists()

            verify(exactly = 1) { viewModel.uiState }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
