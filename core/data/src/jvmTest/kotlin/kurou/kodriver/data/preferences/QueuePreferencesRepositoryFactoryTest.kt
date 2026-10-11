package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QueuePreferencesRepositoryFactoryTest {
    @Test
    fun `createQueuePreferencesRepository は保存と取得が正常に動作する`() =
        runTest {
            val tempDir = Files.createTempDirectory("kodriver_queue_pref_factory_test").toFile()
            try {
                val repo = createQueuePreferencesRepository(tempDir.absolutePath)

                assertTrue(repo.observeQueueEnabledStates().first().isEmpty())

                repo.saveQueueEnabledState(LmuWindowsReadoutItemKey.Flag.Root, true)
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.Root to true),
                    repo.observeQueueEnabledStates().first(),
                )
            } finally {
                tempDir.deleteRecursively()
            }
        }
}
