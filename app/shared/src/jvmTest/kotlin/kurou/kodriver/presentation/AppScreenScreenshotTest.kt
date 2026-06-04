package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.nav_more
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import kurou.kodriver.feature.readout.ReadoutContent
import kurou.kodriver.feature.readout.ReadoutViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test

class AppScreenScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    private fun createViewModel(): ReadoutViewModel {
        val simulatorRepo = object : SimulatorPreferencesRepository {
            private val flow = MutableStateFlow<String?>(null)
            override fun selectedSimulator(): Flow<String?> = flow
            override suspend fun saveSelectedSimulator(simulator: String) { flow.update { simulator } }
        }
        val readoutRepo = object : ReadoutPreferencesRepository {
            override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
                MutableStateFlow(emptyMap())
            override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) = Unit
            override fun observeReadoutOrder(simulator: String): Flow<List<String>> =
                MutableStateFlow(emptyList())
            override suspend fun saveReadoutOrder(simulator: String, order: List<String>) = Unit
        }
        return ReadoutViewModel(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepo),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepo),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepo),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutRepo),
            observeReadoutOrder = ObserveReadoutOrderUseCase(readoutRepo),
            saveReadoutOrder = SaveReadoutOrderUseCase(readoutRepo),
        )
    }

    @Test
    fun `読み上げタブ`() {
        rule.setContent {
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreen(readoutContent = {
                    ReadoutContent(
                        scaffoldDirective = twoPaneDirective,
                        viewModel = createViewModel(),
                    )
                })
            }
        }
        rule.onRoot().captureRoboImage()
    }

    @Test
    fun `その他タブ`() {
        var navMore by mutableStateOf("")

        rule.setContent {
            navMore = stringResource(Res.string.nav_more)
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreen(readoutContent = {
                    ReadoutContent(
                        scaffoldDirective = twoPaneDirective,
                        viewModel = createViewModel(),
                    )
                })
            }
        }
        rule.onNodeWithText(navMore).performClick()
        rule.waitForIdle()
        rule.onRoot().captureRoboImage()
    }
}
