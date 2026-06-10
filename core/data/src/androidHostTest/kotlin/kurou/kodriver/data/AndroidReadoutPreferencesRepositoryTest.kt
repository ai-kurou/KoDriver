@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
        assertEquals(emptyMap(), repository.observeReadoutEnabledStates("lmu").first())

        repository.saveReadoutEnabledState("lmu", "vehicle_approach", true)
        repository.saveReadoutEnabledState("lmu", "flag", false)

        val states = repository.observeReadoutEnabledStates("lmu").first()
        assertEquals(true, states["vehicle_approach"])
        assertEquals(false, states["flag"])
    }

    @Test
    fun `orderは初期状態で空を返し保存後に順序を返す`() = runTest(testDispatcher) {
        assertEquals(emptyList(), repository.observeReadoutOrder("lmu").first())

        repository.saveReadoutOrder("lmu", listOf("flag", "vehicle_approach"))

        assertEquals(listOf("flag", "vehicle_approach"), repository.observeReadoutOrder("lmu").first())
    }

    @Test
    fun `空のorderを保存すると空リストを返す`() = runTest(testDispatcher) {
        repository.saveReadoutOrder("lmu", emptyList())

        assertEquals(emptyList(), repository.observeReadoutOrder("lmu").first())
    }

    @Test
    fun `異なるシミュレータのデータは互いに影響しない`() = runTest(testDispatcher) {
        repository.saveReadoutEnabledState("lmu", "vehicle_approach", true)
        repository.saveReadoutOrder("lmu", listOf("vehicle_approach"))

        assertEquals(emptyMap(), repository.observeReadoutEnabledStates("other").first())
        assertEquals(emptyList(), repository.observeReadoutOrder("other").first())
    }
}
