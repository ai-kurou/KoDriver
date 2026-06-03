package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import kurou.kodriver.feature.readout.ReadoutContent
import kurou.kodriver.feature.readout.ReadoutViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class AppScreenScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    private fun createViewModel(): ReadoutViewModel {
        val simulatorRepo = object : SimulatorPreferencesRepository {
            private val flow = MutableStateFlow<String?>(null)
            override fun selectedSimulator(): Flow<String?> = flow
            override suspend fun saveSelectedSimulator(simulator: String) { flow.value = simulator }
        }
        val readoutRepo = object : ReadoutPreferencesRepository {
            override fun observeReadoutEnabledStates(simulator: String): Flow<Map<String, Boolean>> =
                MutableStateFlow(emptyMap())
            override suspend fun saveReadoutEnabledState(simulator: String, label: String, enabled: Boolean) = Unit
        }
        return ReadoutViewModel(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepo),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepo),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepo),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutRepo),
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
        rule.onNodeWithText("その他").performClick()
        rule.waitForIdle()
        rule.onRoot().captureRoboImage()
    }
}
