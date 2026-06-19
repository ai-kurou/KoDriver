package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.FlagPreferencesSerializer
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FlagPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_flag_prefs_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = FlagPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = FlagPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertTrue(repository.observeFlagEnabledStates().first().isEmpty())

        repository.saveFlagEnabledState(ReadoutItemKey.BLUE_FLAG, true)
        assertEquals(mapOf(ReadoutItemKey.BLUE_FLAG to true), repository.observeFlagEnabledStates().first())

        repository.saveFlagEnabledState(ReadoutItemKey.BLUE_FLAG, false)
        assertEquals(mapOf(ReadoutItemKey.BLUE_FLAG to false), repository.observeFlagEnabledStates().first())
    }

    @Test
    fun `複数フラグを独立して保存・取得できる`() = testScope.runTest {
        repository.saveFlagEnabledState(ReadoutItemKey.BLUE_FLAG, true)
        repository.saveFlagEnabledState(ReadoutItemKey.SECTOR_YELLOW_FLAG, false)
        repository.saveFlagEnabledState(ReadoutItemKey.RED_FLAG, true)

        assertEquals(
            mapOf(
                ReadoutItemKey.BLUE_FLAG to true,
                ReadoutItemKey.SECTOR_YELLOW_FLAG to false,
                ReadoutItemKey.RED_FLAG to true,
            ),
            repository.observeFlagEnabledStates().first(),
        )
    }
}
