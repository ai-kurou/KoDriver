package kurou.kodriver.feature.telemetrylogdetail

import androidx.compose.ui.test.junit4.v2.createComposeRule
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryLogDetailContentWiringTest {
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

    private val viewModel: TelemetryLogDetailViewModel = mockk()

    @Test
    fun `ログIDをSideEffectでViewModelへ反映する`() {
        every { viewModel.uiState } returns MutableStateFlow(TelemetryLogDetailUiState())
        every { viewModel.setLogId(10L) } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            rule.setContent {
                TelemetryLogDetailContent(id = 10L)
            }
            rule.waitForIdle()

            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.setLogId(10L) }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
