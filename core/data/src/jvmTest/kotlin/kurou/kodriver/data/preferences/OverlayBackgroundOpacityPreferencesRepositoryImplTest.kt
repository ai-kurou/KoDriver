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
class OverlayBackgroundOpacityPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_background_opacity_repo_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = OverlayBackgroundOpacityPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = OverlayBackgroundOpacityPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は50・保存した値を返す・上書きで更新される`() =
        runTest {
            assertEquals(50, repository.observeOverlayBackgroundOpacity().first())

            repository.saveOverlayBackgroundOpacity(70)
            assertEquals(70, repository.observeOverlayBackgroundOpacity().first())

            repository.saveOverlayBackgroundOpacity(0)
            assertEquals(0, repository.observeOverlayBackgroundOpacity().first())
        }
}
