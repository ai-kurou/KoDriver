package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5RemainingFuelPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_remaining_fuel_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は30パーセント`() =
        runTest {
            val repository =
                createGt7Ps5RemainingFuelPreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            assertEquals(
                GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT,
                repository.observeThresholdPercentage().first(),
            )
        }

    @Test
    fun `保存した燃料残量閾値を読み出せる`() =
        runTest {
            val repository =
                createGt7Ps5RemainingFuelPreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            repository.saveThresholdPercentage(45)

            assertEquals(45, repository.observeThresholdPercentage().first())
        }
}
