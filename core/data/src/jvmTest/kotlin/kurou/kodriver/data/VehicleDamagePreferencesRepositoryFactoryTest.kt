@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleDamagePreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_vehicle_damage_preferences_repository_factory_test")
        .toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は enabledStates が空Map`() = testScope.runTest {
        val repository = createVehicleDamagePreferencesRepository(tempDir.absolutePath)

        assertEquals(emptyMap(), repository.observeEnabledStates().first())
    }

    @Test
    fun `保存した enabledStates を読み出せる`() = testScope.runTest {
        val repository = createVehicleDamagePreferencesRepository(tempDir.absolutePath)

        repository.saveEnabledState(ReadoutItemKey.Overheat, true)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.Overheat to true),
            repository.observeEnabledStates().first(),
        )
    }
}
