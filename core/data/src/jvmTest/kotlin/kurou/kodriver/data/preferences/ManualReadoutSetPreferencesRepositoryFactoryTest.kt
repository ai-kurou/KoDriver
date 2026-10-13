package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutSetPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_manual_readout_set_preferences_repository_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は空リスト`() =
        runTest {
            val repository = createManualReadoutSetPreferencesRepository(tempDir.absolutePath, Simulator.LmuWindows)

            assertEquals(emptyList(), repository.observeSets().first())
        }

    @Test
    fun `保存したセットを読み出せる`() =
        runTest {
            val repository = createManualReadoutSetPreferencesRepository(tempDir.absolutePath, Simulator.LmuWindows)

            repository.saveSets(
                listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
            )

            assertEquals(
                listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
                repository.observeSets().first(),
            )
        }

    @Test
    fun `シミュレーターごとに独立してセットを保存する`() =
        runTest {
            val lmuRepository =
                createManualReadoutSetPreferencesRepository(tempDir.absolutePath, Simulator.LmuWindows)
            val aceRepository =
                createManualReadoutSetPreferencesRepository(tempDir.absolutePath, Simulator.AceWindows)

            val gt7Repository =
                createManualReadoutSetPreferencesRepository(tempDir.absolutePath, Simulator.Gt7Ps5)

            lmuRepository.saveSets(
                listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
            )
            assertEquals(emptyList(), aceRepository.observeSets().first())

            aceRepository.saveSets(
                listOf(ManualReadoutSet("set-1", listOf("BEST_LAP", "SIMULATOR")), ManualReadoutSet("set-2")),
            )
            assertEquals(emptyList(), gt7Repository.observeSets().first())
            gt7Repository.saveSets(
                listOf(ManualReadoutSet("set-1", listOf("CURRENT_LAP", "SIMULATOR")), ManualReadoutSet("set-2")),
            )
            assertEquals(
                listOf(ManualReadoutSet("set-1", listOf("CURRENT_LAP", "SIMULATOR")), ManualReadoutSet("set-2")),
                gt7Repository.observeSets().first(),
            )

            assertEquals(
                listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
                lmuRepository.observeSets().first(),
            )
            assertEquals(
                listOf(ManualReadoutSet("set-1", listOf("BEST_LAP", "SIMULATOR")), ManualReadoutSet("set-2")),
                aceRepository.observeSets().first(),
            )
        }
}
