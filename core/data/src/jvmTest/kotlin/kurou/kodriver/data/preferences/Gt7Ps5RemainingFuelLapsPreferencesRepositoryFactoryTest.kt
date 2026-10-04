package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5RemainingFuelLapsPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_gt7_remaining_fuel_laps_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は3周`() =
        runTest {
            val repository =
                createGt7Ps5RemainingFuelLapsPreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            assertEquals(GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT, repository.observeRemainingFuelLaps().first())
        }

    @Test
    fun `保存した燃料残り周回数を読み出せる`() =
        runTest {
            val repository =
                createGt7Ps5RemainingFuelLapsPreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            repository.saveRemainingFuelLaps(5)

            assertEquals(5, repository.observeRemainingFuelLaps().first())
        }

    @Test
    fun `文言の初期値はドメインの既定値`() =
        runTest {
            val repository = createGt7Ps5RemainingFuelLapsPreferencesRepository(tempDir.absolutePath)
            assertEquals(GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
            assertEquals(
                GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
                repository.observeEmptyReadoutText().first(),
            )
        }

    @Test
    fun `文言を独立して上書き保存でき空欄も維持する`() =
        runTest {
            val repository = createGt7Ps5RemainingFuelLapsPreferencesRepository(tempDir.absolutePath)
            repository.saveRemainingFuelLaps(5)
            repository.saveReadoutText("残り{laps}周")
            repository.saveEmptyReadoutText("燃料なし")
            assertEquals("残り{laps}周", repository.observeReadoutText().first())
            assertEquals("燃料なし", repository.observeEmptyReadoutText().first())
            repository.saveReadoutText("")
            assertEquals("", repository.observeReadoutText().first())
            assertEquals("燃料なし", repository.observeEmptyReadoutText().first())
            repository.saveEmptyReadoutText("")
            assertEquals("", repository.observeEmptyReadoutText().first())
            assertEquals(5, repository.observeRemainingFuelLaps().first())
        }
}
