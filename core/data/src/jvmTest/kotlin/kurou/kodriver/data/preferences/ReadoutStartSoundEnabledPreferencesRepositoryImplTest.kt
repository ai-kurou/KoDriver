package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutStartSoundEnabledPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_start_sound_enabled_prefs_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = ReadoutStartSoundEnabledPreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = ReadoutStartSoundEnabledPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() =
        testScope.runTest {
            assertTrue(repository.observeStartSoundEnabledStates().first().isEmpty())

            repository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to false),
                repository.observeStartSoundEnabledStates().first(),
            )

            repository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to true),
                repository.observeStartSoundEnabledStates().first(),
            )
        }

    @Test
    fun `複数項目を独立して保存・取得できる`() =
        testScope.runTest {
            repository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false)
            repository.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.Root, true)
            repository.saveStartSoundEnabledState(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.Flag.Root to false,
                    ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false,
                ),
                repository.observeStartSoundEnabledStates().first(),
            )
        }
}
