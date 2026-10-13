package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AutoReadoutPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_readout_repo_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `readout設定が正しいファイルに書き込まれる`() =
        runTest {
            val repository = createAutoReadoutPreferencesRepository(tempDir.absolutePath)
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                repository.observeAutoReadoutEnabledStates("lmu_windows").first(),
            )
        }
}
