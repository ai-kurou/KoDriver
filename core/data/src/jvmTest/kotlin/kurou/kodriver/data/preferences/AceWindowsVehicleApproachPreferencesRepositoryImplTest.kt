package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsVehicleApproachPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_ace_vehicle_approach_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = AceWindowsVehicleApproachPreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("vehicle_approach.pb") },
        )
    private val repository = AceWindowsVehicleApproachPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は各Defaults定数と一致する`() =
        testScope.runTest {
            assertEquals(
                ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
                repository.observeThresholdMeters().first(),
            )
        }

    @Test
    fun `保存した閾値を取得できる`() =
        testScope.runTest {
            repository.saveThresholdMeters(7.0)

            assertEquals(7.0, repository.observeThresholdMeters().first())
        }

    @Test
    fun `enabledStates の初期値は空Map`() =
        testScope.runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, true)
            repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `異なるキーで保存した値がすべて保持される`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, true)
            repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to true,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState後に閾値を保存してもenabledStatesは保持される`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            repository.saveThresholdMeters(7.0)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false),
                repository.observeEnabledStates().first(),
            )
        }
}
