package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ManualReadoutSetPreferencesSerializerTest {
    @Test
    fun `デフォルト値は空リスト`() {
        assertEquals(
            ManualReadoutSetPreferences(sets = emptyList()),
            ManualReadoutSetPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                ManualReadoutSetPreferences(
                    sets =
                        listOf(
                            ManualReadoutSetPreference("set-1", listOf("SESSION", "SIMULATOR", "BRAKE_WEAR")),
                            ManualReadoutSetPreference(),
                        ),
                )
            val output = ByteArrayOutputStream()
            ManualReadoutSetPreferencesSerializer.writeTo(original, output)

            val restored = ManualReadoutSetPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                ManualReadoutSetPreferencesSerializer.readFrom(corrupt)
            }
        }
}
