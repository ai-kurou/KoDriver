package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutSet
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutSetPreferencesRepositoryImplTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_manual_readout_set_preferences_test")
            .toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = ManualReadoutSetPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = ManualReadoutSetPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `未知キーを含むセットをそのまま保存して読み出せる`() =
        runTest {
            val sets =
                listOf(
                    ManualReadoutSet("set-1", listOf("BRAKE_WEAR", "BEST_LAP", "REMOVED_KEY", "SIMULATOR")),
                    ManualReadoutSet("set-2"),
                )
            repository.saveSets(sets)

            assertEquals(sets, repository.observeSets().first())
        }

    @Test
    fun `初期値は空リスト・保存したセットを読み出せる・上書きで更新される`() =
        runTest {
            assertEquals(emptyList(), repository.observeSets().first())

            repository.saveSets(
                listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
            )
            assertEquals(
                listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
                repository.observeSets().first(),
            )

            repository.saveSets(
                listOf(ManualReadoutSet("set-1", listOf("SIMULATOR", "SESSION")), ManualReadoutSet("set-2")),
            )
            assertEquals(
                listOf(ManualReadoutSet("set-1", listOf("SIMULATOR", "SESSION")), ManualReadoutSet("set-2")),
                repository.observeSets().first(),
            )

            repository.saveSets(emptyList())
            assertEquals(emptyList(), repository.observeSets().first())
        }

    @Test
    fun `保存形式の未知キーもそのままドメインモデルへ変換する`() =
        runTest {
            dataStore.updateData {
                it.copy(
                    sets =
                        listOf(
                            ManualReadoutSetPreference("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR")),
                            ManualReadoutSetPreference("set-2"),
                        ),
                )
            }

            assertEquals(
                listOf(
                    ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR")),
                    ManualReadoutSet("set-2"),
                ),
                repository.observeSets().first(),
            )
        }
}
