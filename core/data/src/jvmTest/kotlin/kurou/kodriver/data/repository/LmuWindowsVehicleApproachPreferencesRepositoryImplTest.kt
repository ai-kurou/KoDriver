package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.core.model.VehicleApproachStartReadoutType
import kurou.kodriver.core.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.data.datasource.LmuWindowsVehicleApproachPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsVehicleApproachPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_vehicle_approach_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleApproachPreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsVehicleApproachPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `skipFirstLap の初期値は true`() =
        testScope.runTest {
            assertEquals(true, repository.observeSkipFirstLap().first())
        }

    @Test
    fun `saveSkipFirstLap で保存した値を observeSkipFirstLap で取得できる`() =
        testScope.runTest {
            repository.saveSkipFirstLap(true)
            assertEquals(true, repository.observeSkipFirstLap().first())
        }

    @Test
    fun `saveSkipFirstLap を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveSkipFirstLap(true)
            repository.saveSkipFirstLap(false)
            assertEquals(false, repository.observeSkipFirstLap().first())
        }

    @Test
    fun `startReadoutType の初期値は CAR_LEFT_RIGHT`() =
        testScope.runTest {
            assertEquals(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT, repository.observeStartReadoutType().first())
        }

    @Test
    fun `saveStartReadoutType で保存した値を observeStartReadoutType で取得できる`() =
        testScope.runTest {
            repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
            assertEquals(
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
                repository.observeStartReadoutType().first(),
            )
        }

    @Test
    fun `saveStartReadoutType を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
            repository.saveStartReadoutType(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT)
            assertEquals(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT, repository.observeStartReadoutType().first())
        }

    @Test
    fun `startReadoutType が未知の ID のとき CAR_LEFT_RIGHT を返す`() =
        testScope.runTest {
            dataStore.updateData { it.copy(startReadoutType = "unknown") }

            assertEquals(VehicleApproachStartReadoutType.CAR_LEFT_RIGHT, repository.observeStartReadoutType().first())
        }

    @Test
    fun `sustainedReadoutType の初期値は KEEP_LEFT_RIGHT`() =
        testScope.runTest {
            assertEquals(
                VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
                repository.observeSustainedReadoutType().first(),
            )
        }

    @Test
    fun `saveSustainedReadoutType で保存した値を observeSustainedReadoutType で取得できる`() =
        testScope.runTest {
            repository.saveSustainedReadoutType(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)
            assertEquals(
                VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
                repository.observeSustainedReadoutType().first(),
            )
        }

    @Test
    fun `saveSustainedReadoutType を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveSustainedReadoutType(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)
            repository.saveSustainedReadoutType(VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT)
            assertEquals(
                VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
                repository.observeSustainedReadoutType().first(),
            )
        }

    @Test
    fun `sustainedReadoutType が未知の ID のとき KEEP_LEFT_RIGHT を返す`() =
        testScope.runTest {
            dataStore.updateData { it.copy(sustainedReadoutType = "unknown") }

            assertEquals(
                VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
                repository.observeSustainedReadoutType().first(),
            )
        }

    @Test
    fun `enabledStates の初期値は空Map`() =
        testScope.runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, true)
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState で異なるキーを保存しても互いに独立して保持される`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, false)
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to false,
                    ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }
}
