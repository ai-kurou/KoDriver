@file:Suppress("TooManyFunctions")

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
class AceWindowsFlagReadoutTextPreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_ace_windows_flag_readout_text_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = AceWindowsFlagReadoutTextPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = AceWindowsFlagReadoutTextPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `checkeredFlagText の初期値はチェッカーフラッグ`() =
        runTest {
            assertEquals("チェッカーフラッグ", repository.observeCheckeredFlagText().first())
        }

    @Test
    fun `saveCheckeredFlagText で保存した値を observeCheckeredFlagText で取得できる`() =
        runTest {
            repository.saveCheckeredFlagText("チェッカー、完走")

            assertEquals("チェッカー、完走", repository.observeCheckeredFlagText().first())
        }

    @Test
    fun `Checkeredの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveCheckeredFlagText("チェッカー、完走")
            repository.saveCheckeredFlagText("")

            assertEquals("", repository.observeCheckeredFlagText().first())
        }

    @Test
    fun `whiteFlagText の初期値はホワイトフラッグ`() =
        runTest {
            assertEquals("ホワイトフラッグ", repository.observeWhiteFlagText().first())
        }

    @Test
    fun `saveWhiteFlagText で保存した値を observeWhiteFlagText で取得できる`() =
        runTest {
            repository.saveWhiteFlagText("ホワイトフラッグ、注意")

            assertEquals("ホワイトフラッグ、注意", repository.observeWhiteFlagText().first())
        }

    @Test
    fun `Whiteの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveWhiteFlagText("ホワイトフラッグ、注意")
            repository.saveWhiteFlagText("")

            assertEquals("", repository.observeWhiteFlagText().first())
        }

    @Test
    fun `greenFlagText の初期値はグリーンフラッグ`() =
        runTest {
            assertEquals("グリーンフラッグ", repository.observeGreenFlagText().first())
        }

    @Test
    fun `saveGreenFlagText で保存した値を observeGreenFlagText で取得できる`() =
        runTest {
            repository.saveGreenFlagText("グリーンフラッグ、注意")

            assertEquals("グリーンフラッグ、注意", repository.observeGreenFlagText().first())
        }

    @Test
    fun `Greenの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveGreenFlagText("グリーンフラッグ、注意")
            repository.saveGreenFlagText("")

            assertEquals("", repository.observeGreenFlagText().first())
        }

    @Test
    fun `redFlagText の初期値はレッドフラッグ`() =
        runTest {
            assertEquals("レッドフラッグ", repository.observeRedFlagText().first())
        }

    @Test
    fun `saveRedFlagText で保存した値を observeRedFlagText で取得できる`() =
        runTest {
            repository.saveRedFlagText("レッドフラッグ、注意")

            assertEquals("レッドフラッグ、注意", repository.observeRedFlagText().first())
        }

    @Test
    fun `Redの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveRedFlagText("レッドフラッグ、注意")
            repository.saveRedFlagText("")

            assertEquals("", repository.observeRedFlagText().first())
        }

    @Test
    fun `blueFlagText の初期値はブルーフラッグ`() =
        runTest {
            assertEquals("ブルーフラッグ", repository.observeBlueFlagText().first())
        }

    @Test
    fun `saveBlueFlagText で保存した値を observeBlueFlagText で取得できる`() =
        runTest {
            repository.saveBlueFlagText("ブルーフラッグ、注意")

            assertEquals("ブルーフラッグ、注意", repository.observeBlueFlagText().first())
        }

    @Test
    fun `Blueの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveBlueFlagText("ブルーフラッグ、注意")
            repository.saveBlueFlagText("")

            assertEquals("", repository.observeBlueFlagText().first())
        }

    @Test
    fun `yellowFlagText の初期値はイエローフラッグ`() =
        runTest {
            assertEquals("イエローフラッグ", repository.observeYellowFlagText().first())
        }

    @Test
    fun `saveYellowFlagText で保存した値を observeYellowFlagText で取得できる`() =
        runTest {
            repository.saveYellowFlagText("イエローフラッグ、注意")

            assertEquals("イエローフラッグ、注意", repository.observeYellowFlagText().first())
        }

    @Test
    fun `Yellowの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveYellowFlagText("イエローフラッグ、注意")
            repository.saveYellowFlagText("")

            assertEquals("", repository.observeYellowFlagText().first())
        }

    @Test
    fun `blackFlagText の初期値はブラックフラッグ`() =
        runTest {
            assertEquals("ブラックフラッグ", repository.observeBlackFlagText().first())
        }

    @Test
    fun `saveBlackFlagText で保存した値を observeBlackFlagText で取得できる`() =
        runTest {
            repository.saveBlackFlagText("ブラックフラッグ、注意")

            assertEquals("ブラックフラッグ、注意", repository.observeBlackFlagText().first())
        }

    @Test
    fun `Blackの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveBlackFlagText("ブラックフラッグ、注意")
            repository.saveBlackFlagText("")

            assertEquals("", repository.observeBlackFlagText().first())
        }

    @Test
    fun `blackWhiteFlagText の初期値はブラック・ホワイトフラッグ`() =
        runTest {
            assertEquals("ブラック・ホワイトフラッグ", repository.observeBlackWhiteFlagText().first())
        }

    @Test
    fun `saveBlackWhiteFlagText で保存した値を observeBlackWhiteFlagText で取得できる`() =
        runTest {
            repository.saveBlackWhiteFlagText("ブラック・ホワイトフラッグ、注意")

            assertEquals("ブラック・ホワイトフラッグ、注意", repository.observeBlackWhiteFlagText().first())
        }

    @Test
    fun `BlackWhiteの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveBlackWhiteFlagText("ブラック・ホワイトフラッグ、注意")
            repository.saveBlackWhiteFlagText("")

            assertEquals("", repository.observeBlackWhiteFlagText().first())
        }

    @Test
    fun `各フラッグの文言保存は他の保存済み文言を変更しない`() =
        runTest {
            repository.saveCheckeredFlagText("保存文言Checkered")
            repository.saveWhiteFlagText("保存文言White")
            repository.saveGreenFlagText("保存文言Green")
            repository.saveRedFlagText("保存文言Red")
            repository.saveBlueFlagText("保存文言Blue")
            repository.saveYellowFlagText("保存文言Yellow")
            repository.saveBlackFlagText("保存文言Black")
            repository.saveBlackWhiteFlagText("保存文言BlackWhite")

            assertEquals("保存文言Checkered", repository.observeCheckeredFlagText().first())
            assertEquals("保存文言White", repository.observeWhiteFlagText().first())
            assertEquals("保存文言Green", repository.observeGreenFlagText().first())
            assertEquals("保存文言Red", repository.observeRedFlagText().first())
            assertEquals("保存文言Blue", repository.observeBlueFlagText().first())
            assertEquals("保存文言Yellow", repository.observeYellowFlagText().first())
            assertEquals("保存文言Black", repository.observeBlackFlagText().first())
            assertEquals("保存文言BlackWhite", repository.observeBlackWhiteFlagText().first())
        }
}
