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
                            ManualReadoutSetPreference(
                                "set-1",
                                listOf("SESSION", "SIMULATOR", "BRAKE_WEAR"),
                                mapOf("SESSION" to "残量{percent}%", "UNKNOWN" to ""),
                            ),
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

    @Test
    fun `文言フィールドのない旧データは空Mapとして読み出せる`() =
        runTest {
            // sets[0] = { id: "set-1", itemKeys: ["ENERGY"] }（フィールド1と2のみ）。
            val legacy =
                byteArrayOf(
                    0x0A,
                    0x0F,
                    0x0A,
                    0x05,
                    0x73,
                    0x65,
                    0x74,
                    0x2D,
                    0x31,
                    0x12,
                    0x06,
                    0x45,
                    0x4E,
                    0x45,
                    0x52,
                    0x47,
                    0x59,
                )
            val restored = ManualReadoutSetPreferencesSerializer.readFrom(ByteArrayInputStream(legacy))
            assertEquals(listOf(ManualReadoutSetPreference("set-1", listOf("ENERGY"))), restored.sets)
            assertEquals(emptyMap(), restored.sets.single().itemTexts)
        }
}
