package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayVisiblePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_visible_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = OverlayVisiblePreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = OverlayVisiblePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値はtrue・保存した値を返す・上書きで更新される`() =
        testScope.runTest {
            assertTrue(repository.observeOverlayVisible().first())

            repository.saveOverlayVisible(false)
            assertFalse(repository.observeOverlayVisible().first())

            repository.saveOverlayVisible(true)
            assertTrue(repository.observeOverlayVisible().first())
        }
}
