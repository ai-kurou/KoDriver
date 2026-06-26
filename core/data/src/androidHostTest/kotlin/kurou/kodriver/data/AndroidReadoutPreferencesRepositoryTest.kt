@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import org.junit.After
import org.junit.Before
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidReadoutPreferencesRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tempFile: File
    private lateinit var repository: AndroidReadoutPreferencesRepository

    @Before
    fun setUp() {
        tempFile = File.createTempFile("readout_test", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(testDispatcher + SupervisorJob()),
            produceFile = { tempFile },
        )
        repository = AndroidReadoutPreferencesRepository(dataStore)
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `enabledStatesは初期状態で空を返し保存後にON_OFF状態を返す`() = runTest(testDispatcher) {
        assertEquals(emptyMap(), repository.observeReadoutEnabledStates("lmu_windows").first())

        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.Flag, false)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleDamage, true)

        val states = repository.observeReadoutEnabledStates("lmu_windows").first()
        assertEquals(true, states[ReadoutItemKey.VehicleApproach])
        assertEquals(false, states[ReadoutItemKey.Flag])
        assertEquals(true, states[ReadoutItemKey.VehicleDamage])
    }

    @Test
    fun `orderは初期状態で空を返し保存後に順序を返す`() = runTest(testDispatcher) {
        assertEquals(emptyList(), repository.observeReadoutOrder("lmu_windows").first())

        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleApproach, ReadoutItemKey.VehicleDamage),
        )

        assertEquals(
            listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleApproach, ReadoutItemKey.VehicleDamage),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `空のorderを保存すると空リストを返す`() = runTest(testDispatcher) {
        repository.saveReadoutOrder("lmu_windows", emptyList())

        assertEquals(emptyList(), repository.observeReadoutOrder("lmu_windows").first())
    }

    @Test
    fun `異なるシミュレータのデータは互いに影響しない`() = runTest(testDispatcher) {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VehicleApproach))

        assertEquals(emptyMap(), repository.observeReadoutEnabledStates("other").first())
        assertEquals(emptyList(), repository.observeReadoutOrder("other").first())
    }
}
