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
    fun `プレビューは保存せず配信され解除すると保存値に戻る`() =
        runTest {
            repository.saveOverlayBackgroundOpacity(20)

            listOf(0, 50, 100).forEach { preview ->
                repository.setPreviewOverlayBackgroundOpacity(preview)

                assertEquals(preview, repository.observeOverlayBackgroundOpacity().first())
                assertEquals(20, dataStore.data.first().opacity)
            }
            repository.setPreviewOverlayBackgroundOpacity(null)

            assertEquals(20, repository.observeOverlayBackgroundOpacity().first())
        }

    @Test
    fun `プレビュー中に保存した値は解除後に配信される`() =
        runTest {
            repository.setPreviewOverlayBackgroundOpacity(80)
            repository.saveOverlayBackgroundOpacity(20)

            assertEquals(80, repository.observeOverlayBackgroundOpacity().first())
            assertEquals(20, dataStore.data.first().opacity)
            repository.setPreviewOverlayBackgroundOpacity(null)

            assertEquals(20, repository.observeOverlayBackgroundOpacity().first())
        }

    @Test
    fun `購読中のオーバーレイにプレビュー設定と解除が配信される`() =
        runTest {
            repository.saveOverlayBackgroundOpacity(20)
            val values = Channel<Int>(Channel.UNLIMITED)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.observeOverlayBackgroundOpacity().collect { values.send(it) }
            }
            assertEquals(20, values.receive())

            repository.setPreviewOverlayBackgroundOpacity(80)
            assertEquals(80, values.receive())
            assertEquals(20, dataStore.data.first().opacity)

            repository.setPreviewOverlayBackgroundOpacity(null)
            assertEquals(20, values.receive())
        }
}
