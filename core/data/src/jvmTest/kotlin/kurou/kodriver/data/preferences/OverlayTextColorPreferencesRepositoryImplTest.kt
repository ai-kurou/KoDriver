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
}
