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
class LmuWindowsFlagReadoutTextPreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_flag_readout_text_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = FlagReadoutTextPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsFlagReadoutTextPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `sectorYellowFlagText の初期値はイエローフラッグ`() =
        runTest {
            assertEquals("イエローフラッグ", repository.observeSectorYellowFlagText().first())
        }

    @Test
    fun `saveSectorYellowFlagText で保存した値を observeSectorYellowFlagText で取得できる`() =
        runTest {
            repository.saveSectorYellowFlagText("イエロー、注意")

            assertEquals("イエロー、注意", repository.observeSectorYellowFlagText().first())
        }

    @Test
    fun `空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveSectorYellowFlagText("イエロー、注意")
            repository.saveSectorYellowFlagText("")

            assertEquals("", repository.observeSectorYellowFlagText().first())
        }

    @Test
    fun `blueFlagText の初期値はブルーフラッグ`() =
        runTest {
            assertEquals("ブルーフラッグ", repository.observeBlueFlagText().first())
        }

    @Test
    fun `saveBlueFlagText で保存した値を observeBlueFlagText で取得できる`() =
        runTest {
            repository.saveBlueFlagText("ブルー、道を譲れ")

            assertEquals("ブルー、道を譲れ", repository.observeBlueFlagText().first())
        }

    @Test
    fun `fullCourseYellowFlagText の初期値はフルコースイエロー`() =
        runTest {
            assertEquals("フルコースイエロー", repository.observeFullCourseYellowFlagText().first())
        }

    @Test
    fun `saveFullCourseYellowFlagText で保存した値を observeFullCourseYellowFlagText で取得できる`() =
        runTest {
            repository.saveFullCourseYellowFlagText("フルコースイエロー")

            assertEquals("フルコースイエロー", repository.observeFullCourseYellowFlagText().first())
        }

    @Test
    fun `フルコースイエローの空文字は保存後も空文字のまま取得できる`() =
        runTest {
            repository.saveFullCourseYellowFlagText("")

            assertEquals("", repository.observeFullCourseYellowFlagText().first())
        }

    @Test
    fun `redFlagText の初期値はレッドフラッグ`() =
        runTest {
            assertEquals("レッドフラッグ", repository.observeRedFlagText().first())
        }

    @Test
    fun `saveRedFlagText で保存した値を observeRedFlagText で取得できる`() =
        runTest {
            repository.saveRedFlagText("レッドフラッグ、セッション中断")

            assertEquals("レッドフラッグ、セッション中断", repository.observeRedFlagText().first())
        }
}
