package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
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
