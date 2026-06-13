package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.SoundVolumePreferencesSerializer
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SoundVolumeRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_sound_volume_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = SoundVolumePreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = SoundVolumeRepositoryImpl(dataStore)

    @Test
    fun `初期値は100・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertEquals(100, repository.volume().first())

        repository.saveVolume(70)
        assertEquals(70, repository.volume().first())

        repository.saveVolume(0)
        assertEquals(0, repository.volume().first())
    }
}
