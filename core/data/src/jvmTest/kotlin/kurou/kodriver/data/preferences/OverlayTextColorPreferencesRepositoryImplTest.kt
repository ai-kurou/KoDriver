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
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayTextColorPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_text_color_repo_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = OverlayTextColorPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = OverlayTextColorPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は既定の文字色・保存した値を返す・上書きで更新される`() =
        runTest {
            assertEquals(0xFFFFC107L, repository.observeOverlayTextColor().first())

            repository.saveOverlayTextColor(0xFF123456L)
            assertEquals(0xFF123456L, repository.observeOverlayTextColor().first())

            repository.saveOverlayTextColor(0x00123456L)
            assertEquals(0x00123456L, repository.observeOverlayTextColor().first())
        }

    @Test
    fun `プレビューは保存せず配信され解除すると保存値に戻る`() =
        runTest {
            repository.saveOverlayTextColor(0xFF123456L)

            listOf(0xFFFFFFFFL, 0xFFFF0000L, 0x00000000L).forEach { preview ->
                repository.setPreviewOverlayTextColor(preview)

                assertEquals(preview, repository.observeOverlayTextColor().first())
                assertEquals(0xFF123456L, dataStore.data.first().argb)
            }
            repository.setPreviewOverlayTextColor(null)

            assertEquals(0xFF123456L, repository.observeOverlayTextColor().first())
        }

    @Test
    fun `プレビュー中に保存した値は解除後に配信される`() =
        runTest {
            repository.setPreviewOverlayTextColor(0xFFFFFFFFL)
            repository.saveOverlayTextColor(0xFF123456L)

            assertEquals(0xFFFFFFFFL, repository.observeOverlayTextColor().first())
            assertEquals(0xFF123456L, dataStore.data.first().argb)
            repository.setPreviewOverlayTextColor(null)

            assertEquals(0xFF123456L, repository.observeOverlayTextColor().first())
        }

    @Test
    fun `購読中のオーバーレイにプレビュー設定と解除が配信される`() =
        runTest {
            repository.saveOverlayTextColor(0xFF123456L)
            val values = Channel<Long>(Channel.UNLIMITED)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.observeOverlayTextColor().collect { values.send(it) }
            }
            assertEquals(0xFF123456L, values.receive())

            repository.setPreviewOverlayTextColor(0xFFFFFFFFL)
            assertEquals(0xFFFFFFFFL, values.receive())
            assertEquals(0xFF123456L, dataStore.data.first().argb)

            repository.setPreviewOverlayTextColor(null)
            assertEquals(0xFF123456L, values.receive())
        }
}
