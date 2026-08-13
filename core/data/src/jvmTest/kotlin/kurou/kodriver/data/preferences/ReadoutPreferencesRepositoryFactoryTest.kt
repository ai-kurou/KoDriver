package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_readout_repo_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `readout設定が正しいファイルに書き込まれる`() =
        runTest {
            val repository = createReadoutPreferencesRepository(tempDir.absolutePath)
            repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.VehicleApproach.Root, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleApproach.Root to true),
                repository.observeReadoutEnabledStates("lmu_windows").first(),
            )
        }
}
