package kurou.kodriver.feature.readout

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kodriver.feature.readout.generated.resources.Res
import kodriver.feature.readout.generated.resources.item_vehicle_approach
import kodriver.feature.readout.generated.resources.select_simulator_hint
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import org.jetbrains.compose.resources.stringResource
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ReadoutContentTest {

    @get:Rule
    val rule = createComposeRule()

    private val singlePaneDirective = PaneScaffoldDirective(
        maxHorizontalPartitions = 1,
        horizontalPartitionSpacerSize = 0.dp,
        maxVerticalPartitions = 1,
        verticalPartitionSpacerSize = 0.dp,
        defaultPanePreferredWidth = 360.dp,
        excludedBounds = emptyList(),
    )

    private fun createViewModel(): ReadoutViewModel {
        val simulatorRepo = FakeSimulatorPreferencesRepository()
        val readoutRepo = FakeReadoutPreferencesRepository()
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
    fun `詳細ペインに遷移後にbackHandlerのコールバックを呼ぶと一覧に戻る`() {
        var backEnabled = false
        var capturedOnBack: (() -> Unit)? = null
        var selectSimulatorHint by mutableStateOf("")
        var vehicleApproachText by mutableStateOf("")

        rule.setContent {
            selectSimulatorHint = stringResource(Res.string.select_simulator_hint)
            vehicleApproachText = stringResource(Res.string.item_vehicle_approach)
            ReadoutContent(
                scaffoldDirective = singlePaneDirective,
                backHandler = { enabled, onBack ->
                    backEnabled = enabled
                    capturedOnBack = onBack
                },
                viewModel = createViewModel(),
            )
        }

        assertFalse(backEnabled)

        // ドロップダウンを開く
        rule.onNodeWithText(selectSimulatorHint).performClick()
        rule.waitForIdle()

        // ドロップダウンのアイテムを選択してリストを表示
        rule.onNodeWithText("Le Mans Ultimate").performClick()
        rule.waitForIdle()

        // LazyColumn のアイテムをタップして詳細ペインへ遷移
        rule.onNodeWithText(vehicleApproachText).performClick()
        rule.waitForIdle()

        assertTrue(backEnabled)

        rule.runOnIdle { capturedOnBack?.invoke() }
        rule.waitForIdle()

        assertFalse(backEnabled)
    }
}
