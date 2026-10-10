package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsFlagPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_ace_flag_prefs_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = AceWindowsFlagPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = AceWindowsFlagPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() =
        runTest {
            assertTrue(repository.observeFlagEnabledStates().first().isEmpty())

            repository.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.BlueFlag, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(AceWindowsReadoutItemKey.Flag.BlueFlag to true),
                repository.observeFlagEnabledStates().first(),
            )

            repository.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.BlueFlag, false)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(AceWindowsReadoutItemKey.Flag.BlueFlag to false),
                repository.observeFlagEnabledStates().first(),
            )
        }

    @Test
    fun `複数フラグを独立して保存・取得できる`() =
        runTest {
            repository.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.BlueFlag, true)
            repository.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.YellowFlag, false)
            repository.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.RedFlag, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    AceWindowsReadoutItemKey.Flag.BlueFlag to true,
                    AceWindowsReadoutItemKey.Flag.YellowFlag to false,
                    AceWindowsReadoutItemKey.Flag.RedFlag to true,
                ),
                repository.observeFlagEnabledStates().first(),
            )
        }
}
