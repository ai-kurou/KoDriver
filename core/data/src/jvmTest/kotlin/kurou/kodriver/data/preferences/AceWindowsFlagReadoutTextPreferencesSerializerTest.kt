package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT
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
            // field 2は将来追加するフィールド。field 1がない場合は既定文言を使用する。
            listOf(byteArrayOf(), byteArrayOf(0x12, 1, 65)).forEach { bytes ->
                assertEquals(
                    AceWindowsFlagReadoutTextPreferences(),
                    AceWindowsFlagReadoutTextPreferencesSerializer.readFrom(ByteArrayInputStream(bytes)),
                )
            }
        }

    @Test
    fun `未知フィールドは既存の空文字を上書きしない`() =
        runTest {
            val bytes = byteArrayOf(0x0a, 0, 0x12, 1, 65)
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
}
