package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsFlagPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_flag_prefs_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsFlagPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsFlagPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() =
        runTest {
            assertTrue(repository.observeFlagEnabledStates().first().isEmpty())

            repository.saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.BlueFlag, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.BlueFlag to true),
                repository.observeFlagEnabledStates().first(),
            )

            repository.saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.BlueFlag, false)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.BlueFlag to false),
                repository.observeFlagEnabledStates().first(),
            )
        }

    @Test
    fun `複数フラグを独立して保存・取得できる`() =
        runTest {
            repository.saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.BlueFlag, true)
            repository.saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.SectorYellowFlag, false)
            repository.saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.RedFlag, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.Flag.BlueFlag to true,
                    LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to false,
                    LmuWindowsReadoutItemKey.Flag.RedFlag to true,
                ),
                repository.observeFlagEnabledStates().first(),
            )
        }
}
