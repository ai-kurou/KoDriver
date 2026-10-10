package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
            OverlayTextSize.entries.forEach { overlayTextSize ->
                repository.saveOverlayTextSize(overlayTextSize)

                assertEquals(overlayTextSize.id, dataStore.data.first().size)
                assertEquals(overlayTextSize, repository.observeOverlayTextSize().first())
            }
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

    @Test
    fun `プレビューは保存せず配信され解除すると保存値に戻る`() =
        runTest {
            repository.saveOverlayTextSize(OverlayTextSize.SMALL)

            OverlayTextSize.entries.forEach { preview ->
                repository.setPreviewOverlayTextSize(preview)

                assertEquals(preview, repository.observeOverlayTextSize().first())
                assertEquals(OverlayTextSize.SMALL.id, dataStore.data.first().size)
            }
            repository.setPreviewOverlayTextSize(null)

            assertEquals(OverlayTextSize.SMALL, repository.observeOverlayTextSize().first())
        }

    @Test
    fun `プレビュー中に保存した値は解除後に配信される`() =
        runTest {
            repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE)
            repository.saveOverlayTextSize(OverlayTextSize.SMALL)

            assertEquals(OverlayTextSize.LARGE, repository.observeOverlayTextSize().first())
            assertEquals(OverlayTextSize.SMALL.id, dataStore.data.first().size)
            repository.setPreviewOverlayTextSize(null)

            assertEquals(OverlayTextSize.SMALL, repository.observeOverlayTextSize().first())
        }

    @Test
    fun `購読中のオーバーレイにプレビュー設定と解除が配信される`() =
        runTest {
            repository.saveOverlayTextSize(OverlayTextSize.SMALL)
            val values = Channel<OverlayTextSize>(Channel.UNLIMITED)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.observeOverlayTextSize().collect { values.send(it) }
            }
            assertEquals(OverlayTextSize.SMALL, values.receive())

            repository.setPreviewOverlayTextSize(OverlayTextSize.LARGE)
            assertEquals(OverlayTextSize.LARGE, values.receive())
            assertEquals(OverlayTextSize.SMALL.id, dataStore.data.first().size)

            repository.setPreviewOverlayTextSize(null)
            assertEquals(OverlayTextSize.SMALL, values.receive())
        }
}
