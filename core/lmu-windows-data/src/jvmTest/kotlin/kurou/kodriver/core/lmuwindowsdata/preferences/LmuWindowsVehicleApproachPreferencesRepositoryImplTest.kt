package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsVehicleApproachPreferencesRepositoryImplTest {
    @BeforeTest
    fun setUp() {
        registerLmuWindowsReadoutItemKeys()
    }

    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_vehicle_approach_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleApproachPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsVehicleApproachPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `skipFirstLap の初期値は true`() =
        runTest {
            assertEquals(true, repository.observeSkipFirstLap().first())
        }

    @Test
    fun `saveSkipFirstLap で保存した値を observeSkipFirstLap で取得できる`() =
        runTest {
            repository.saveSkipFirstLap(true)
            assertEquals(true, repository.observeSkipFirstLap().first())
        }

    @Test
    fun `saveSkipFirstLap を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveSkipFirstLap(true)
            repository.saveSkipFirstLap(false)
            assertEquals(false, repository.observeSkipFirstLap().first())
        }

    @Test
    fun `enabledStates の初期値は空Map`() =
        runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Sustained to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, true)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Sustained to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState で異なるキーを保存しても互いに独立して保持される`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, false)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.StartReadout, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleApproach.Sustained to false,
                    LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `保存した設定をすべて既定値に戻せる`() =
        runTest {
            val readoutTextRepository = LmuWindowsVehicleApproachReadoutTextPreferencesRepositoryImpl(dataStore)
            val defaultStartLeftReadoutText = readoutTextRepository.observeStartLeftReadoutText().first()
            val defaultStartRightReadoutText = readoutTextRepository.observeStartRightReadoutText().first()
            val defaultSustainedLeftReadoutText = readoutTextRepository.observeSustainedLeftReadoutText().first()
            val defaultSustainedRightReadoutText = readoutTextRepository.observeSustainedRightReadoutText().first()
            val defaultSkipFirstLap = repository.observeSkipFirstLap().first()
            val defaultEnabledStates = repository.observeEnabledStates().first()

            repository.saveSkipFirstLap(false)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, false)

            readoutTextRepository.saveStartLeftReadoutText("変更した文言")
            readoutTextRepository.saveStartRightReadoutText("変更した文言")
            readoutTextRepository.saveSustainedLeftReadoutText("変更した文言")
            readoutTextRepository.saveSustainedRightReadoutText("変更した文言")

            assertNotEquals(LmuWindowsVehicleApproachPreferencesSerializer.defaultValue, dataStore.data.first())
            repository.resetToDefault()

            assertEquals(LmuWindowsVehicleApproachPreferencesSerializer.defaultValue, dataStore.data.first())
            assertEquals(defaultSkipFirstLap, repository.observeSkipFirstLap().first())
            assertEquals(defaultEnabledStates, repository.observeEnabledStates().first())
            assertEquals(defaultStartLeftReadoutText, readoutTextRepository.observeStartLeftReadoutText().first())
            assertEquals(defaultStartRightReadoutText, readoutTextRepository.observeStartRightReadoutText().first())
            assertEquals(
                defaultSustainedLeftReadoutText,
                readoutTextRepository.observeSustainedLeftReadoutText().first(),
            )
            assertEquals(
                defaultSustainedRightReadoutText,
                readoutTextRepository.observeSustainedRightReadoutText().first(),
            )
        }
}
