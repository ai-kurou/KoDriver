package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsTyreWearPreferencesRepositoryFactoryTest {

    private val tempDir =
        Files
        .createTempDirectory(
        "kodriver_lmu_windows_tyre_wear_preferences_repository_factory_test",
    ).toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は thresholdPercentage が 50`() =
        testScope.runTest {
        val repository = createLmuWindowsTyreWearPreferencesRepository(tempDir.absolutePath)

        assertEquals(50, repository.observeThresholdPercentage().first())
    }

    @Test
    fun `保存した thresholdPercentage を読み出せる`() =
        testScope.runTest {
        val repository = createLmuWindowsTyreWearPreferencesRepository(tempDir.absolutePath)

        repository.saveThresholdPercentage(30)

        assertEquals(30, repository.observeThresholdPercentage().first())
    }
}
