package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_TYPE_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
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
                ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
                repository.observeLongitudinalThresholdMeters().first(),
            )
            assertEquals(
                ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
                repository.observeLateralThresholdMeters().first(),
            )
            assertEquals(
                ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_TYPE_DEFAULT,
                repository.observeStartReadoutType().first(),
            )
        }

    @Test
    fun `保存した前後方向の閾値を取得できる`() =
        testScope.runTest {
            repository.saveLongitudinalThresholdMeters(7.0)

            assertEquals(7.0, repository.observeLongitudinalThresholdMeters().first())
        }

    @Test
    fun `保存した左右方向の閾値を取得できる`() =
        testScope.runTest {
            repository.saveLateralThresholdMeters(6.0)

            assertEquals(6.0, repository.observeLateralThresholdMeters().first())
        }

    @Test
    fun `保存した開始時読み上げ文言を取得できる`() =
        testScope.runTest {
            repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)

            assertEquals(
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
                repository.observeStartReadoutType().first(),
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
            repository.saveLongitudinalThresholdMeters(7.0)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false),
                repository.observeEnabledStates().first(),
            )
        }
}
