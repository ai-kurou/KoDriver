package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.ReadoutItemKey
import kurou.kodriver.data.datasource.AceWindowsFlagPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsFlagPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_ace_flag_prefs_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = AceWindowsFlagPreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = AceWindowsFlagPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() =
        testScope.runTest {
            assertTrue(repository.observeFlagEnabledStates().first().isEmpty())

            repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.BlueFlag, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.Flag.BlueFlag to true),
                repository.observeFlagEnabledStates().first(),
            )

            repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.BlueFlag, false)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.Flag.BlueFlag to false),
                repository.observeFlagEnabledStates().first(),
            )
        }

    @Test
    fun `複数フラグを独立して保存・取得できる`() =
        testScope.runTest {
            repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.BlueFlag, true)
            repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.YellowFlag, false)
            repository.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.RedFlag, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.Flag.BlueFlag to true,
                    ReadoutItemKey.AceWindows.Flag.YellowFlag to false,
                    ReadoutItemKey.AceWindows.Flag.RedFlag to true,
                ),
                repository.observeFlagEnabledStates().first(),
            )
        }
}
