package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.DebugStateCardOrderPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DebugStateCardOrderPreferencesSerializerTest {

    @Test
    fun `デフォルト値は空リスト`() {
        assertEquals(
            DebugStateCardOrderPreferences(cardOrder = emptyList()),
            DebugStateCardOrderPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
        val original = DebugStateCardOrderPreferences(cardOrder = listOf("SESSION", "SIMULATOR"))
        val output = ByteArrayOutputStream()
        DebugStateCardOrderPreferencesSerializer.writeTo(original, output)

        val restored = DebugStateCardOrderPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            DebugStateCardOrderPreferencesSerializer.readFrom(corrupt)
        }
    }
}
