package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleDamagePreferencesRepositoryFactoryTest {
    @BeforeTest
    fun setUp() {
        registerLmuWindowsReadoutItemKeys()
    }

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

            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleDamage.Overheat to true),
                repository.observeEnabledStates().first(),
            )
        }
}
