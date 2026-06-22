package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.MyBestLapPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MyBestLapPreferencesSerializerTest {

    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            MyBestLapPreferences(voiceType = "formal"),
            MyBestLapPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = MyBestLapPreferences(voiceType = "casual")
        val output = ByteArrayOutputStream()
        MyBestLapPreferencesSerializer.writeTo(original, output)

        val restored = MyBestLapPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            MyBestLapPreferencesSerializer.readFrom(corrupt)
        }
    }
}
