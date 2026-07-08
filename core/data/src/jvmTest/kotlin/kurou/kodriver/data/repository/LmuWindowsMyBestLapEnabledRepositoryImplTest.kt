package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.ReadoutPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsMyBestLapEnabledRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_lmu_my_best_lap_enabled_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = ReadoutPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = LmuWindowsMyBestLapEnabledRepositoryImpl(
        ReadoutPreferencesRepositoryImpl(dataStore),
    )

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は未設定(null)`() = testScope.runTest {
        assertNull(repository.observeEnabled().first())
    }

    @Test
    fun `保存した有効状態を取得できる`() = testScope.runTest {
        repository.saveEnabled(true)

        assertEquals(true, repository.observeEnabled().first())
    }

    @Test
    fun `有効状態を上書き保存できる`() = testScope.runTest {
        repository.saveEnabled(true)
        repository.saveEnabled(false)

        assertEquals(false, repository.observeEnabled().first())
    }
}
