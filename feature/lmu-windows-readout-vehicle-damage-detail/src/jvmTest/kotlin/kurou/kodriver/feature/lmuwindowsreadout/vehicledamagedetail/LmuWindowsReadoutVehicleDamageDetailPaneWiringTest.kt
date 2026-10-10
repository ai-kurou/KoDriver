package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kurou.kodriver.core.designsystem.KoDriverTheme
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsReadoutVehicleDamageDetailPaneWiringTest {
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

    private val viewModel: LmuWindowsReadoutVehicleDamageDetailViewModel = mockk()

    @Test
    fun `ペインを破棄すると試聴を停止する`() {
        every { viewModel.uiState } returns MutableStateFlow(LmuWindowsReadoutVehicleDamageDetailUiState())
        every { viewModel.onPreviewStopped() } returns Unit
        startKoin { modules(module { single { viewModel } }) }
        try {
            var showPane by mutableStateOf(true)
            rule.setContent {
                KoDriverTheme {
                    if (showPane) LmuWindowsReadoutVehicleDamageDetailPane()
                }
            }
            rule.waitForIdle()
            showPane = false
            rule.waitForIdle()
            verify(exactly = 1) { viewModel.uiState }
            verify(exactly = 1) { viewModel.onPreviewStopped() }
            confirmVerified(viewModel)
        } finally {
            stopKoin()
        }
    }
}
