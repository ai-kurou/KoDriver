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
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidReadoutPreferencesRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tempFile: File
    private lateinit var repository: AndroidReadoutPreferencesRepository

    @BeforeTest
    fun setUp() {
        tempFile = File.createTempFile("readout_test", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(testDispatcher + SupervisorJob()),
            produceFile = { tempFile },
        )
        repository = AndroidReadoutPreferencesRepository(dataStore)
    }

    @AfterTest
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `enabledStatesは初期状態で空を返し保存後にON_OFF状態を返す`() = runTest(testDispatcher) {
        assertEquals(emptyMap(), repository.observeReadoutEnabledStates("lmu_windows").first())

        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.VehicleApproach.Root, true)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.Flag.Root, false)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.VehicleDamage.Root, true)

        val states = repository.observeReadoutEnabledStates("lmu_windows").first()
        assertEquals(true, states[ReadoutItemKey.LmuWindows.VehicleApproach.Root])
        assertEquals(false, states[ReadoutItemKey.LmuWindows.Flag.Root])
        assertEquals(true, states[ReadoutItemKey.LmuWindows.VehicleDamage.Root])
    }

    @Test
    fun `orderは初期状態で空を返し保存後に順序を返す`() = runTest(testDispatcher) {
        assertEquals(emptyList(), repository.observeReadoutOrder("lmu_windows").first())

        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ),
        )

        assertEquals(
            listOf(
                ReadoutItemKey.LmuWindows.Flag.Root,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root,
            ),
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
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.LmuWindows.VehicleApproach.Root, true)
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.LmuWindows.VehicleApproach.Root))

        assertEquals(emptyMap(), repository.observeReadoutEnabledStates("other").first())
        assertEquals(emptyList(), repository.observeReadoutOrder("other").first())
    }
}
