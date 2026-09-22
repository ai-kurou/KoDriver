package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayTextSize
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayTextSizePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_text_size_preferences_repo_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = OverlayTextSizePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = OverlayTextSizePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `overlayTextSizeの初期値はMEDIUM`() =
        runTest {
            assertEquals(OverlayTextSize.MEDIUM, repository.observeOverlayTextSize().first())
        }

    @Test
    fun `saveOverlayTextSizeで保存した値をobserveOverlayTextSizeで取得できる`() =
        runTest {
            repository.saveOverlayTextSize(OverlayTextSize.LARGE)

            assertEquals(OverlayTextSize.LARGE, repository.observeOverlayTextSize().first())
        }

    @Test
    fun `saveOverlayTextSizeを複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveOverlayTextSize(OverlayTextSize.LARGE)
            repository.saveOverlayTextSize(OverlayTextSize.SMALL)

            assertEquals(OverlayTextSize.SMALL, repository.observeOverlayTextSize().first())
        }

    @Test
    fun `overlayTextSizeが未知のIDのときMEDIUMを返す`() =
        runTest {
            dataStore.updateData { it.copy(size = "unknown") }

            assertEquals(OverlayTextSize.MEDIUM, repository.observeOverlayTextSize().first())
        }
}
