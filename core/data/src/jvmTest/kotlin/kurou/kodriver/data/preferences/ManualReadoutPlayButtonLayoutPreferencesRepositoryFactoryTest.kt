package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ManualReadoutPlayButtonLayoutPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_manual_readout_play_button_layout_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はfalse`() =
        runTest {
            val repository = createManualReadoutPlayButtonLayoutPreferencesRepository(tempDir.absolutePath)

            assertFalse(repository.observeManualReadoutPlayButtonLayoutEnabled().first())
        }

    @Test
    fun `保存した値を読み出せる`() =
        runTest {
            val repository = createManualReadoutPlayButtonLayoutPreferencesRepository(tempDir.absolutePath)
            repository.saveManualReadoutPlayButtonLayoutEnabled(true)

            assertTrue(repository.observeManualReadoutPlayButtonLayoutEnabled().first())
        }
}
