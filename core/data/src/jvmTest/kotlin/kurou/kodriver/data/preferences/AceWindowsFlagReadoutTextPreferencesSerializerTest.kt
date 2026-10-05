@file:Suppress("TooManyFunctions")

package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AceWindowsFlagReadoutTextPreferencesSerializerTest {
    @Test
    fun `デフォルト値はチェッカーフラッグ`() {
        assertEquals(
            ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.checkeredFlagText,
        )
    }

    @Test
    fun `カスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(checkeredFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `欠損フィールドと未知フィールドでは既定値を返す`() =
        runTest {
            // field 11は将来追加するフィールド。field 1がない場合は既定文言を使用する。
            listOf(byteArrayOf(), byteArrayOf(0x5a, 1, 65)).forEach { bytes ->
                assertEquals(
                    AceWindowsFlagReadoutTextPreferences(),
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(ByteArrayInputStream(bytes)),
                )
            }
        }

    @Test
    fun `未知フィールドは既存の空文字を上書きしない`() =
        runTest {
            val bytes = byteArrayOf(0x0a, 0, 0x5a, 1, 65)
            assertEquals(
                "",
                AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(ByteArrayInputStream(bytes)).checkeredFlagText,
            )
        }

    @Test
    fun `不正なバイト列でCorruptionExceptionが発生する`() =
        runTest {
            assertFailsWith<CorruptionException> {
                AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                    ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42)),
                )
            }
        }

    @Test
    fun `デフォルト値はホワイトフラッグ`() {
        assertEquals(
            ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.whiteFlagText,
        )
    }

    @Test
    fun `Whiteのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(whiteFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `デフォルト値はグリーンフラッグ`() {
        assertEquals(
            ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.greenFlagText,
        )
    }

    @Test
    fun `Greenのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(greenFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `デフォルト値はレッドフラッグ`() {
        assertEquals(
            ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.redFlagText,
        )
    }

    @Test
    fun `Redのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(redFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `デフォルト値はブルーフラッグ`() {
        assertEquals(
            ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.blueFlagText,
        )
    }

    @Test
    fun `Blueのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(blueFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `デフォルト値はイエローフラッグ`() {
        assertEquals(
            ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.yellowFlagText,
        )
    }

    @Test
    fun `Yellowのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(yellowFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `デフォルト値はブラックフラッグ`() {
        assertEquals(
            ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.blackFlagText,
        )
    }

    @Test
    fun `Blackのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(blackFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `デフォルト値はブラック・ホワイトフラッグ`() {
        assertEquals(
            ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.blackWhiteFlagText,
        )
    }

    @Test
    fun `BlackWhiteのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(blackWhiteFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `フィールド1のみの既存データでは追加9種は既定値になる`() =
        runTest {
            listOf(byteArrayOf(0x0a, 1, 65), byteArrayOf(0x0a, 0)).forEach { bytes ->
                val restored = AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))
                assertEquals(
                    AceWindowsFlagReadoutTextPreferences(checkeredFlagText = if (bytes.size == 2) "" else "A"),
                    restored,
                )
            }
        }

    @Test
    fun `デフォルト値はオレンジボールフラッグ、車両に不具合があります`() {
        assertEquals(
            ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.orangeCircleFlagText,
        )
    }

    @Test
    fun `OrangeCircleのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(orangeCircleFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `デフォルト値はレッド・イエローストライプフラッグ、路面が滑りやすいです`() {
        assertEquals(
            ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT,
            AceWindowsFlagReadoutTextPreferencesSerializer.defaultValue.redYellowStripesFlagText,
        )
    }

    @Test
    fun `RedYellowStripesのカスタム文言と空文字は往復後も保持する`() =
        runTest {
            listOf("チェッカー、完走", "").forEach { text ->
                val original = AceWindowsFlagReadoutTextPreferences(redYellowStripesFlagText = text)
                val output = ByteArrayOutputStream()
                AceWindowsFlagReadoutTextPreferencesSerializer.writeTo(original, output)
                val restored =
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(
                        ByteArrayInputStream(output.toByteArray()),
                    )
                assertEquals(original, restored)
            }
        }

    @Test
    fun `フィールド1から8の既存データを保持し追加2種は既定値になる`() =
        runTest {
            // 既存フィールドの保存文言と明示的な空文字を交互に含む。
            val bytes =
                byteArrayOf(
                    0x0a,
                    1,
                    65,
                    0x12,
                    0,
                    0x1a,
                    1,
                    66,
                    0x22,
                    0,
                    0x2a,
                    1,
                    67,
                    0x32,
                    0,
                    0x3a,
                    1,
                    68,
                    0x42,
                    0,
                )
            assertEquals(
                AceWindowsFlagReadoutTextPreferences(
                    checkeredFlagText = "A",
                    whiteFlagText = "",
                    greenFlagText = "B",
                    redFlagText = "",
                    blueFlagText = "C",
                    yellowFlagText = "",
                    blackFlagText = "D",
                    blackWhiteFlagText = "",
                ),
                AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(ByteArrayInputStream(bytes)),
            )
        }
}
