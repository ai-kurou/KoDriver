package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsVehicleApproachReadoutTextPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_vehicle_approach_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleApproachPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsVehicleApproachReadoutTextPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `開始Left文言の初期値と保存値を取得できる`() =
        runTest {
            assertEquals("カーレフト", repository.observeStartLeftReadoutText().first())
            repository.saveStartLeftReadoutText("注意")
            assertEquals("注意", repository.observeStartLeftReadoutText().first())
            repository.saveStartLeftReadoutText("")
            assertEquals("", repository.observeStartLeftReadoutText().first())
        }

    @Test
    fun `開始Right文言の初期値と保存値を取得できる`() =
        runTest {
            assertEquals("カーライト", repository.observeStartRightReadoutText().first())
            repository.saveStartRightReadoutText("注意")
            assertEquals("注意", repository.observeStartRightReadoutText().first())
            repository.saveStartRightReadoutText("")
            assertEquals("", repository.observeStartRightReadoutText().first())
        }

    @Test
    fun `継続Left文言の初期値と保存値を取得できる`() =
        runTest {
            assertEquals("キープライト", repository.observeSustainedLeftReadoutText().first())
            repository.saveSustainedLeftReadoutText("注意")
            assertEquals("注意", repository.observeSustainedLeftReadoutText().first())
            repository.saveSustainedLeftReadoutText("")
            assertEquals("", repository.observeSustainedLeftReadoutText().first())
        }

    @Test
    fun `継続Right文言の初期値と保存値を取得できる`() =
        runTest {
            assertEquals("キープレフト", repository.observeSustainedRightReadoutText().first())
            repository.saveSustainedRightReadoutText("注意")
            assertEquals("注意", repository.observeSustainedRightReadoutText().first())
            repository.saveSustainedRightReadoutText("")
            assertEquals("", repository.observeSustainedRightReadoutText().first())
        }
}
