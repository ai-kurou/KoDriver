package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlagPreferencesRepositoryTest {

    @Test
    fun `createFlagPreferencesRepository は保存と取得が正常に動作する`() = runBlocking {
        val tempDir = Files.createTempDirectory("kodriver_flag_pref_factory_test").toFile()
        try {
            val repo = createFlagPreferencesRepository(tempDir.absolutePath)

            assertTrue(repo.observeFlagEnabledStates().first().isEmpty())

            repo.saveFlagEnabledState(ReadoutItemKey.BlueFlag, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.BlueFlag to true),
                repo.observeFlagEnabledStates().first(),
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
