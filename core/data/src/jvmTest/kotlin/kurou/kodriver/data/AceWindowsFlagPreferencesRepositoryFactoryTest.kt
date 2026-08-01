package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AceWindowsFlagPreferencesRepositoryFactoryTest {
    @Test
    fun `createAceWindowsFlagPreferencesRepository は保存と取得が正常に動作する`() =
        runBlocking {
            val tempDir = Files.createTempDirectory("kodriver_ace_flag_pref_factory_test").toFile()
            try {
                val repo = createAceWindowsFlagPreferencesRepository(tempDir.absolutePath)

                assertTrue(repo.observeFlagEnabledStates().first().isEmpty())

                repo.saveFlagEnabledState(ReadoutItemKey.AceWindows.Flag.BlueFlag, true)
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.Flag.BlueFlag to true),
                    repo.observeFlagEnabledStates().first(),
                )
            } finally {
                tempDir.deleteRecursively()
            }
        }
}
