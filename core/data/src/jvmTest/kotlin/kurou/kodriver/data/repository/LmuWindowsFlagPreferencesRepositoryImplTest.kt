package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.LmuWindowsFlagPreferencesSerializer
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsFlagPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_flag_prefs_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = LmuWindowsFlagPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = LmuWindowsFlagPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertTrue(repository.observeFlagEnabledStates().first().isEmpty())

        repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, true)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.BlueFlag to true),
            repository.observeFlagEnabledStates().first(),
        )

        repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, false)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.BlueFlag to false),
            repository.observeFlagEnabledStates().first(),
        )
    }

    @Test
    fun `複数フラグを独立して保存・取得できる`() = testScope.runTest {
        repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, true)
        repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag, false)
        repository.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, true)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to false,
                ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
            ),
            repository.observeFlagEnabledStates().first(),
        )
    }
}
