package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlagReadoutTextPreferencesSerializerTest {
    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            FlagReadoutTextPreferences(sectorYellowFlagText = LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT),
            FlagReadoutTextPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                FlagReadoutTextPreferences(
                    sectorYellowFlagText = "イエロー、注意",
                )
            val output = ByteArrayOutputStream()
            FlagReadoutTextPreferencesSerializer.writeTo(original, output)

            val restored = FlagReadoutTextPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, restored)
        }

    @Test
    fun `旧収録音声選択フィールドを含む設定でも文言と空文字を保持する`() =
        runTest {
            // field 1: 空文字、field 2: "blue"、廃止したfield 5〜8: true。
            val legacy = byteArrayOf(0x0a, 0, 0x12, 4, 98, 108, 117, 101, 0x28, 1, 0x30, 1, 0x38, 1, 0x40, 1)
            val restored = FlagReadoutTextPreferencesSerializer.readFrom(ByteArrayInputStream(legacy))
            assertEquals("", restored.sectorYellowFlagText)
            assertEquals("blue", restored.blueFlagText)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                FlagReadoutTextPreferencesSerializer.readFrom(corrupt)
            }
        }
}
