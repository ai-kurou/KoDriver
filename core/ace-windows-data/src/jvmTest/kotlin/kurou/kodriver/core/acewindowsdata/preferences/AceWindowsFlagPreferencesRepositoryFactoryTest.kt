package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AceWindowsFlagPreferencesRepositoryFactoryTest {
    @BeforeTest
    fun setUp() {
        registerAceWindowsReadoutItemKeys()
    }

    @Test
    fun `createAceWindowsFlagPreferencesRepository は保存と取得が正常に動作する`() =
        runTest {
            val tempDir = Files.createTempDirectory("kodriver_ace_flag_pref_factory_test").toFile()
            try {
                val repo = createAceWindowsFlagPreferencesRepository(tempDir.absolutePath)

                assertTrue(repo.observeFlagEnabledStates().first().isEmpty())

                repo.saveFlagEnabledState(AceWindowsReadoutItemKey.Flag.BlueFlag, true)
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(AceWindowsReadoutItemKey.Flag.BlueFlag to true),
                    repo.observeFlagEnabledStates().first(),
                )
            } finally {
                tempDir.deleteRecursively()
            }
        }
}
