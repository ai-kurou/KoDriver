package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreWearPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_tyre_wear_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は thresholdPercentage が 50`() =
        runTest {
            val repository = createLmuWindowsTyreWearPreferencesRepository(tempDir.absolutePath)

            assertEquals(50, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `保存した thresholdPercentage を読み出せる`() =
        runTest {
            val repository = createLmuWindowsTyreWearPreferencesRepository(tempDir.absolutePath)

            repository.saveThresholdPercentage(30)

            assertEquals(30, repository.observeThresholdPercentage().first())
        }
}
