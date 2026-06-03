package kurou.kodriver.feature.readout

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.junit.Rule
import org.junit.Test

class ReadoutContentScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    private fun createViewModel(): ReadoutViewModel {
        val simulatorRepo = FakeSimulatorPreferencesRepository()
        val readoutRepo = FakeReadoutPreferencesRepository()
        return ReadoutViewModel(
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepo),
            saveSelectedSimulator = SaveSelectedSimulatorUseCase(simulatorRepo),
            observeReadoutEnabledStates = ObserveReadoutEnabledStatesUseCase(readoutRepo),
            saveReadoutEnabledState = SaveReadoutEnabledStateUseCase(readoutRepo),
        )
    }

    @Test
    fun `一覧と詳細の同時表示`() {
        rule.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Surface {
                    ReadoutContent(
                        modifier = Modifier.requiredSize(840.dp, 640.dp),
                        scaffoldDirective = twoPaneDirective,
                        viewModel = createViewModel(),
                    )
                }
            }
        }
        rule.onRoot().captureRoboImage()
    }
}
