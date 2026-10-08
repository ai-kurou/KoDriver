@file:Suppress("TooManyFunctions")

package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsFlagReadoutTextKey
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
        dataStoreScope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `checkeredFlagText の初期値はチェッカーフラッグ`() =
        runTest {
            assertEquals("チェッカーフラッグ", repository.observeText(AceWindowsFlagReadoutTextKey.CHECKERED).first())
        }

    @Test
    fun `saveCheckeredFlagText で保存した値を observeCheckeredFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "チェッカー、完走")

            assertEquals("チェッカー、完走", repository.observeText(AceWindowsFlagReadoutTextKey.CHECKERED).first())
        }

    @Test
    fun `Checkeredの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "チェッカー、完走")
            repository.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.CHECKERED).first())
        }

    @Test
    fun `whiteFlagText の初期値はホワイトフラッグ`() =
        runTest {
            assertEquals("ホワイトフラッグ", repository.observeText(AceWindowsFlagReadoutTextKey.WHITE).first())
        }

    @Test
    fun `saveWhiteFlagText で保存した値を observeWhiteFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.WHITE, "ホワイトフラッグ、注意")

            assertEquals("ホワイトフラッグ、注意", repository.observeText(AceWindowsFlagReadoutTextKey.WHITE).first())
        }

    @Test
    fun `Whiteの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.WHITE, "ホワイトフラッグ、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.WHITE, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.WHITE).first())
        }

    @Test
    fun `greenFlagText の初期値はグリーンフラッグ`() =
        runTest {
            assertEquals("グリーンフラッグ", repository.observeText(AceWindowsFlagReadoutTextKey.GREEN).first())
        }

    @Test
    fun `saveGreenFlagText で保存した値を observeGreenFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.GREEN, "グリーンフラッグ、注意")

            assertEquals("グリーンフラッグ、注意", repository.observeText(AceWindowsFlagReadoutTextKey.GREEN).first())
        }

    @Test
    fun `Greenの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.GREEN, "グリーンフラッグ、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.GREEN, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.GREEN).first())
        }

    @Test
    fun `redFlagText の初期値はレッドフラッグ`() =
        runTest {
            assertEquals("レッドフラッグ", repository.observeText(AceWindowsFlagReadoutTextKey.RED).first())
        }

    @Test
    fun `saveRedFlagText で保存した値を observeRedFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.RED, "レッドフラッグ、注意")

            assertEquals("レッドフラッグ、注意", repository.observeText(AceWindowsFlagReadoutTextKey.RED).first())
        }

    @Test
    fun `Redの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.RED, "レッドフラッグ、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.RED, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.RED).first())
        }

    @Test
    fun `blueFlagText の初期値はブルーフラッグ`() =
        runTest {
            assertEquals("ブルーフラッグ", repository.observeText(AceWindowsFlagReadoutTextKey.BLUE).first())
        }

    @Test
    fun `saveBlueFlagText で保存した値を observeBlueFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.BLUE, "ブルーフラッグ、注意")

            assertEquals("ブルーフラッグ、注意", repository.observeText(AceWindowsFlagReadoutTextKey.BLUE).first())
        }

    @Test
    fun `Blueの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.BLUE, "ブルーフラッグ、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.BLUE, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.BLUE).first())
        }

    @Test
    fun `yellowFlagText の初期値はイエローフラッグ`() =
        runTest {
            assertEquals("イエローフラッグ", repository.observeText(AceWindowsFlagReadoutTextKey.YELLOW).first())
        }

    @Test
    fun `saveYellowFlagText で保存した値を observeYellowFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.YELLOW, "イエローフラッグ、注意")

            assertEquals("イエローフラッグ、注意", repository.observeText(AceWindowsFlagReadoutTextKey.YELLOW).first())
        }

    @Test
    fun `Yellowの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.YELLOW, "イエローフラッグ、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.YELLOW, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.YELLOW).first())
        }

    @Test
    fun `blackFlagText の初期値はブラックフラッグ`() =
        runTest {
            assertEquals("ブラックフラッグ", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK).first())
        }

    @Test
    fun `saveBlackFlagText で保存した値を observeBlackFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.BLACK, "ブラックフラッグ、注意")

            assertEquals("ブラックフラッグ、注意", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK).first())
        }

    @Test
    fun `Blackの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.BLACK, "ブラックフラッグ、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.BLACK, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK).first())
        }

    @Test
    fun `blackWhiteFlagText の初期値はブラック・ホワイトフラッグ`() =
        runTest {
            assertEquals("ブラック・ホワイトフラッグ", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE).first())
        }

    @Test
    fun `saveBlackWhiteFlagText で保存した値を observeBlackWhiteFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, "ブラック・ホワイトフラッグ、注意")

            assertEquals("ブラック・ホワイトフラッグ、注意", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE).first())
        }

    @Test
    fun `BlackWhiteの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, "ブラック・ホワイトフラッグ、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE).first())
        }

    @Test
    fun `各フラッグの文言保存は他の保存済み文言を変更しない`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.CHECKERED, "保存文言Checkered")
            repository.saveText(AceWindowsFlagReadoutTextKey.WHITE, "保存文言White")
            repository.saveText(AceWindowsFlagReadoutTextKey.GREEN, "保存文言Green")
            repository.saveText(AceWindowsFlagReadoutTextKey.RED, "保存文言Red")
            repository.saveText(AceWindowsFlagReadoutTextKey.BLUE, "保存文言Blue")
            repository.saveText(AceWindowsFlagReadoutTextKey.YELLOW, "保存文言Yellow")
            repository.saveText(AceWindowsFlagReadoutTextKey.BLACK, "保存文言Black")
            repository.saveText(AceWindowsFlagReadoutTextKey.BLACK_WHITE, "保存文言BlackWhite")
            repository.saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, "保存文言OrangeCircle")
            repository.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "保存文言RedYellowStripes")

            assertEquals("保存文言Checkered", repository.observeText(AceWindowsFlagReadoutTextKey.CHECKERED).first())
            assertEquals("保存文言White", repository.observeText(AceWindowsFlagReadoutTextKey.WHITE).first())
            assertEquals("保存文言Green", repository.observeText(AceWindowsFlagReadoutTextKey.GREEN).first())
            assertEquals("保存文言Red", repository.observeText(AceWindowsFlagReadoutTextKey.RED).first())
            assertEquals("保存文言Blue", repository.observeText(AceWindowsFlagReadoutTextKey.BLUE).first())
            assertEquals("保存文言Yellow", repository.observeText(AceWindowsFlagReadoutTextKey.YELLOW).first())
            assertEquals("保存文言Black", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK).first())
            assertEquals("保存文言BlackWhite", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE).first())
            assertEquals("保存文言OrangeCircle", repository.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE).first())
            assertEquals(
                "保存文言RedYellowStripes",
                repository.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES).first(),
            )
        }

    @Test
    fun `orangeCircleFlagText の初期値はオレンジボールフラッグ、車両に不具合があります`() =
        runTest {
            assertEquals(
                "オレンジボールフラッグ、車両に不具合があります",
                repository.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE).first(),
            )
        }

    @Test
    fun `saveOrangeCircleFlagText で保存した値を observeOrangeCircleFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, "オレンジボールフラッグ、車両に不具合があります、注意")

            assertEquals(
                "オレンジボールフラッグ、車両に不具合があります、注意",
                repository.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE).first(),
            )
        }

    @Test
    fun `OrangeCircleの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, "オレンジボールフラッグ、車両に不具合があります、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE).first())
        }

    @Test
    fun `redYellowStripesFlagText の初期値はレッド・イエローストライプフラッグ、路面が滑りやすいです`() =
        runTest {
            assertEquals(
                "レッド・イエローストライプフラッグ、路面が滑りやすいです",
                repository.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES).first(),
            )
        }

    @Test
    fun `saveRedYellowStripesFlagText で保存した値を observeRedYellowStripesFlagText で取得できる`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "レッド・イエローストライプフラッグ、路面が滑りやすいです、注意")

            assertEquals(
                "レッド・イエローストライプフラッグ、路面が滑りやすいです、注意",
                repository.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES).first(),
            )
        }

    @Test
    fun `RedYellowStripesの空文字を保存すると未設定に戻る`() =
        runTest {
            repository.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "レッド・イエローストライプフラッグ、路面が滑りやすいです、注意")
            repository.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "")

            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES).first())
        }

    @Test
    fun `既存データの文言と空文字を保持し追加2種は既定値で監視できる`() =
        runTest {
            tempDir.resolve("test.pb").writeBytes(byteArrayOf(0x0a, 1, 65, 0x42, 0))

            assertEquals("A", repository.observeText(AceWindowsFlagReadoutTextKey.CHECKERED).first())
            assertEquals("", repository.observeText(AceWindowsFlagReadoutTextKey.BLACK_WHITE).first())
            assertEquals(
                "オレンジボールフラッグ、車両に不具合があります",
                repository.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE).first(),
            )
            assertEquals(
                "レッド・イエローストライプフラッグ、路面が滑りやすいです",
                repository.observeText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES).first(),
            )
        }

    @Test
    fun `全種類で空文字を保存できる`() =
        runTest {
            AceWindowsFlagReadoutTextKey.entries.forEach { key ->
                repository.saveText(key, "保存文言$key")
            }
            AceWindowsFlagReadoutTextKey.entries.forEach { key ->
                repository.saveText(key, "")
                assertEquals("", repository.observeText(key).first())
            }
        }

    @Test
    fun `既存形式の全フィールドを対応するキーで取得できる`() =
        runTest {
            val original =
                AceWindowsFlagReadoutTextPreferences(
                    checkeredFlagText = "チェッカー保存値",
                    whiteFlagText = "ホワイト保存値",
                    greenFlagText = "グリーン保存値",
                    redFlagText = "レッド保存値",
                    blueFlagText = "ブルー保存値",
                    yellowFlagText = "イエロー保存値",
                    blackFlagText = "ブラック保存値",
                    blackWhiteFlagText = "ブラックホワイト保存値",
                    orangeCircleFlagText = "オレンジ保存値",
                    redYellowStripesFlagText = "ストライプ保存値",
                )
            tempDir.resolve("test.pb").outputStream().use {
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, it)
            }
            val expected =
                listOf(
                    "チェッカー保存値",
                    "ホワイト保存値",
                    "グリーン保存値",
                    "レッド保存値",
                    "ブルー保存値",
                    "イエロー保存値",
                    "ブラック保存値",
                    "ブラックホワイト保存値",
                    "オレンジ保存値",
                    "ストライプ保存値",
                )
            assertEquals(expected, AceWindowsFlagReadoutTextKey.entries.map { repository.observeText(it).first() })
        }
}
