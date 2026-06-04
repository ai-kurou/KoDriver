package kurou.kodriver

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kurou.kodriver.data.createReadoutPreferencesRepository
import kurou.kodriver.data.createSimulatorPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.appModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Path

class AppScenarioTest {

    @get:Rule
    val rule = createComposeRule()

    private val tempDirPath: Path = Files.createTempDirectory("kodriver-test")
    private val tempDir = tempDirPath.toString()

    private val testDataModule = module {
        single<SimulatorPreferencesRepository> {
            createSimulatorPreferencesRepository(directory = tempDir)
        }
        single<ReadoutPreferencesRepository> {
            createReadoutPreferencesRepository(directory = tempDir)
        }
        factory { ObserveSelectedSimulatorUseCase(get()) }
        factory { SaveSelectedSimulatorUseCase(get()) }
        factory { ObserveReadoutEnabledStatesUseCase(get()) }
        factory { SaveReadoutEnabledStateUseCase(get()) }
        factory { ObserveReadoutOrderUseCase(get()) }
        factory { SaveReadoutOrderUseCase(get()) }
    }

    @Before
    fun setUp() {
        startKoin {
            modules(listOf(testDataModule) + appModules)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        tempDirPath.toFile().deleteRecursively()
    }

    @Test
    fun `シミュレータ選択後に最上位の読み上げ項目をタップしその他タブへ移動する`() {
        rule.setContent { AppScreen() }

        rule.onNodeWithTag("simulator_dropdown_trigger").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("simulator_item_lmu").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("readout_item_0").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("nav_more").performClick()
        rule.waitForIdle()
    }
}
