package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsFlagPreferencesRepositoryFactoryTest {
    @Test
    fun `createLmuWindowsFlagPreferencesRepository は保存と取得が正常に動作する`() =
        runTest {
            val tempDir = Files.createTempDirectory("kodriver_flag_pref_factory_test").toFile()
            try {
                val repo = createLmuWindowsFlagPreferencesRepository(tempDir.absolutePath)

                assertTrue(repo.observeFlagEnabledStates().first().isEmpty())

                repo.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.BlueFlag, true)
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.BlueFlag to true),
                    repo.observeFlagEnabledStates().first(),
                )
            } finally {
                tempDir.deleteRecursively()
            }
        }
}
