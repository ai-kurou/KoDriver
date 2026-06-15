package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.FlagPreferencesSerializer
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

        repository.saveFlagEnabledState("green", true)
        assertEquals(mapOf("green" to true), repository.observeFlagEnabledStates().first())

        repository.saveFlagEnabledState("green", false)
        assertEquals(mapOf("green" to false), repository.observeFlagEnabledStates().first())
    }

    @Test
    fun `複数フラグを独立して保存・取得できる`() = testScope.runTest {
        repository.saveFlagEnabledState("green", true)
        repository.saveFlagEnabledState("yellow", false)
        repository.saveFlagEnabledState("red", true)

        assertEquals(
            mapOf("green" to true, "yellow" to false, "red" to true),
            repository.observeFlagEnabledStates().first(),
        )
    }
}
