@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
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
class LmuWindowsVehicleDamagePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_vehicle_damage_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleDamagePreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsVehicleDamagePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `enabledStates の初期値は空Map`() =
        testScope.runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true)
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `異なるキーで保存した値がすべて保持される`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, true)
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true,
                    ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }
}
