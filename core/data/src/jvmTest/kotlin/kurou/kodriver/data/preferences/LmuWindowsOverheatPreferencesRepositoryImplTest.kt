package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverheatVoiceType
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsOverheatPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_overheat_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = OverheatPreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsOverheatPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `voiceType の初期値は GP2_GP2`() =
        testScope.runTest {
            assertEquals(OverheatVoiceType.GP2_GP2, repository.observeVoiceType().first())
        }

    @Test
    fun `saveVoiceType で保存した値を observeVoiceType で取得できる`() =
        testScope.runTest {
            repository.saveVoiceType(OverheatVoiceType.STANDARD)
            assertEquals(OverheatVoiceType.STANDARD, repository.observeVoiceType().first())
        }

    @Test
    fun `saveVoiceType を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveVoiceType(OverheatVoiceType.STANDARD)
            repository.saveVoiceType(OverheatVoiceType.GP2_GP2)
            assertEquals(OverheatVoiceType.GP2_GP2, repository.observeVoiceType().first())
        }

    @Test
    fun `voiceType が未知の ID のとき GP2_GP2 を返す`() =
        testScope.runTest {
            dataStore.updateData { it.copy(voiceType = "unknown") }
            assertEquals(OverheatVoiceType.GP2_GP2, repository.observeVoiceType().first())
        }
}
