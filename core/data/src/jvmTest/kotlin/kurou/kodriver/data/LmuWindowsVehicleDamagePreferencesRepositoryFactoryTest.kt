@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleDamagePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_vehicle_damage_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は enabledStates が空Map`() =
        runTest {
            val repository = createLmuWindowsVehicleDamagePreferencesRepository(tempDir.absolutePath)

            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `保存した enabledStates を読み出せる`() =
        runTest {
            val repository = createLmuWindowsVehicleDamagePreferencesRepository(tempDir.absolutePath)

            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true),
                repository.observeEnabledStates().first(),
            )
        }
}
