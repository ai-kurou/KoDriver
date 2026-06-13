package kurou.kodriver

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.data.createFlagPreferencesRepository
import kurou.kodriver.data.createReadoutPreferencesRepository
import kurou.kodriver.data.createSimulatorPreferencesRepository
import kurou.kodriver.domain.model.LmuTelemetryData
import kurou.kodriver.domain.repository.FlagPreferencesRepository
import kurou.kodriver.domain.repository.LmuRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SaveReadoutOrderUseCase
import kurou.kodriver.domain.usecase.SaveSelectedSimulatorUseCase
import kurou.kodriver.feature.lmunarrator.fakeLmuNarratorDataModule
import kurou.kodriver.feature.readout.fakeReadoutDataModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.appModules
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Path

class AppTest {

    companion object {
        private lateinit var tempDirPath: Path
        private lateinit var tempDir: String

        @BeforeClass @JvmStatic
        fun setUpKoin() {
            tempDirPath = Files.createTempDirectory("kodriver-test")
            tempDir = tempDirPath.toString()
            val scenarioDataModule = module {
                single<SimulatorPreferencesRepository> {
                    createSimulatorPreferencesRepository(directory = tempDir)
                }
                single<ReadoutPreferencesRepository> {
                    createReadoutPreferencesRepository(directory = tempDir)
                }
                single<FlagPreferencesRepository> {
                    createFlagPreferencesRepository(directory = tempDir)
                }
                single<LmuRepository> { TestLmuRepository }
                factory { ObserveSelectedSimulatorUseCase(get()) }
                factory { SaveSelectedSimulatorUseCase(get()) }
                factory { ObserveReadoutEnabledStatesUseCase(get()) }
                factory { SaveReadoutEnabledStateUseCase(get()) }
                factory { ObserveReadoutOrderUseCase(get()) }
                factory { SaveReadoutOrderUseCase(get()) }
            }
            startKoin {
                modules(listOf(fakeLmuNarratorDataModule, fakeReadoutDataModule, scenarioDataModule) + appModules)
            }
        }

        @AfterClass @JvmStatic
        fun tearDownKoin() {
            stopKoin()
            tempDirPath.toFile().deleteRecursively()
        }
    }

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `アプリ画面が起動する`() {
        rule.setContent {
            AppScreen(readoutContent = {})
        }
        rule.waitForIdle()
    }

    @Test
    fun `シミュレータ選択後に最上位の読み上げ項目をタップしその他タブへ移動する`() {
        rule.setContent { AppScreen() }

        rule.onNodeWithTag("simulator_dropdown_trigger").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("simulator_item_lmu").performClick()
        rule.waitForIdle()

        rule.waitUntil(timeoutMillis = 5_000L) {
            rule.onAllNodesWithTag("readout_item_0").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("readout_item_0").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("nav_more").performClick()
        rule.waitForIdle()
    }
}

private object TestLmuRepository : LmuRepository {
    override fun telemetryStream(): Flow<LmuTelemetryData> = emptyFlow()
    override suspend fun isConnected(): Boolean = false
    override suspend fun disconnect() = Unit
}
