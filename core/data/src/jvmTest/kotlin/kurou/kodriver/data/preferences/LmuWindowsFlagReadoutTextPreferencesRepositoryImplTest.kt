package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun `fullCourseYellowFlagText の初期値は空文字`() =
        runTest {
            assertEquals("", repository.observeFullCourseYellowFlagText().first())
        }

    @Test
    fun `saveFullCourseYellowFlagText で保存した値を observeFullCourseYellowFlagText で取得できる`() =
        runTest {
            repository.saveFullCourseYellowFlagText("フルコースイエロー")

            assertEquals("フルコースイエロー", repository.observeFullCourseYellowFlagText().first())
        }

    @Test
    fun `redFlagText の初期値は空文字`() =
        runTest {
            assertEquals("", repository.observeRedFlagText().first())
        }

    @Test
    fun `saveRedFlagText で保存した値を observeRedFlagText で取得できる`() =
        runTest {
            repository.saveRedFlagText("レッドフラッグ、セッション中断")

            assertEquals("レッドフラッグ、セッション中断", repository.observeRedFlagText().first())
        }

    @Test
    fun `収録音声選択の初期値は全フラッグで未選択`() =
        runTest {
            LmuWindowsFlagReadoutTarget.entries.forEach { target ->
                assertFalse(repository.observeRecordedVoiceSelected(target).first())
            }
        }

    @Test
    fun `収録音声選択はフラッグごとに独立して保存され文言は変わらない`() =
        runTest {
            repository.saveBlueFlagText("ブルー、道を譲れ")

            LmuWindowsFlagReadoutTarget.entries.forEach { target ->
                repository.saveRecordedVoiceSelected(target, true)

                assertTrue(repository.observeRecordedVoiceSelected(target).first())
                LmuWindowsFlagReadoutTarget.entries.filter { it.ordinal > target.ordinal }.forEach { other ->
                    assertFalse(repository.observeRecordedVoiceSelected(other).first())
                }
            }
            assertEquals("ブルー、道を譲れ", repository.observeBlueFlagText().first())

            LmuWindowsFlagReadoutTarget.entries.forEach { target ->
                repository.saveRecordedVoiceSelected(target, false)
                assertFalse(repository.observeRecordedVoiceSelected(target).first())
            }
        }

    @Test
    fun `saveTextAndRecordedVoiceSelected は指定フラッグの文言と選択状態を同時に保存し他のフラッグは変更しない`() =
        runTest {
            LmuWindowsFlagReadoutTarget.entries.forEach { target ->
                repository.saveRecordedVoiceSelected(target, true)
                repository.saveTextAndRecordedVoiceSelected(target, "文言-${target.name}", false)
            }

            assertEquals("文言-SECTOR_YELLOW_FLAG", repository.observeSectorYellowFlagText().first())
            assertEquals("文言-BLUE_FLAG", repository.observeBlueFlagText().first())
            assertEquals("文言-FULL_COURSE_YELLOW", repository.observeFullCourseYellowFlagText().first())
            assertEquals("文言-RED_FLAG", repository.observeRedFlagText().first())
            LmuWindowsFlagReadoutTarget.entries.forEach { target ->
                assertFalse(repository.observeRecordedVoiceSelected(target).first())
            }
        }
}
