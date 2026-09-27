package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayWindowBounds
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayWindowBoundsPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_window_bounds_repo_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = OverlayWindowBoundsPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = OverlayWindowBoundsPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値を返す・保存した値を返す・上書きで更新される`() =
        runTest {
            assertEquals(OverlayWindowBounds(), repository.observeOverlayWindowBounds().first())

            val moved = OverlayWindowBounds(x = -1920, y = 0, width = 300, height = 100)
            repository.saveOverlayWindowBounds(moved)
            assertEquals(moved, repository.observeOverlayWindowBounds().first())

            val resized = moved.copy(width = 800, height = 400)
            repository.saveOverlayWindowBounds(resized)
            assertEquals(resized, repository.observeOverlayWindowBounds().first())
        }
}
