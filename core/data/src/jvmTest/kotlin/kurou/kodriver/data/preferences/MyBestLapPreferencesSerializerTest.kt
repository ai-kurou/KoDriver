package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MyBestLapPreferencesSerializerTest {
    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            MyBestLapPreferences(),
            MyBestLapPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                MyBestLapPreferences(
                    voiceType = "casual",
                    readoutText = "GT7更新{laptime}",
                    lmuWindowsReadoutText = "LMU更新{laptime}",
                    aceWindowsReadoutText = "ACE更新{laptime}",
                )
            val output = ByteArrayOutputStream()
            MyBestLapPreferencesSerializer.writeTo(original, output)

            val restored = MyBestLapPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, restored)
        }

    @Test
    fun `旧形式の口調だけを保存したデータは既定文言で復元される`() =
        runTest {
            // ProtoNumber(1) の文字列 casual のみを含む旧形式。
            val legacy = byteArrayOf(0x0A, 0x06) + "casual".encodeToByteArray()
            val restored = MyBestLapPreferencesSerializer.readFrom(ByteArrayInputStream(legacy))
            assertEquals("casual", restored.voiceType)
            assertEquals(GT7_PS5_MY_BEST_LAP_READOUT_TEXT_DEFAULT, restored.readoutText)
            assertEquals(LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, restored.lmuWindowsReadoutText)
            assertEquals(ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, restored.aceWindowsReadoutText)
        }

    @Test
    fun `空白文言もそのまま復元される`() =
        runTest {
            val original =
                MyBestLapPreferences(readoutText = " ", lmuWindowsReadoutText = " ", aceWindowsReadoutText = " ")
            val output = ByteArrayOutputStream()
            MyBestLapPreferencesSerializer.writeTo(original, output)
            assertEquals(original, MyBestLapPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())))
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                MyBestLapPreferencesSerializer.readFrom(corrupt)
            }
        }
}
