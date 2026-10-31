package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

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
                                hotkeyVirtualKeyCode = 0x41,
                                hotkeyModifiers = 3,
                                name = "レース用",
                                number = 7,
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
            assertEquals("", restored.sets.single().name)
            assertEquals(0, restored.sets.single().number)
            assertEquals(emptyMap(), restored.sets.single().itemTexts)
            assertEquals(null, restored.sets.single().hotkeyVirtualKeyCode)
            assertEquals(null, restored.sets.single().hotkeyModifiers)
        }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `ホットキーフィールドのない文言保存済み旧データも読み出せる`() =
        runTest {
            val legacy =
                LegacyManualReadoutSetPreferences(
                    listOf(LegacyManualReadoutSetPreference("target", listOf("ENERGY"), mapOf("ENERGY" to "文言"))),
                )
            val bytes = ProtoBuf.encodeToByteArray(LegacyManualReadoutSetPreferences.serializer(), legacy)
            val restored = ManualReadoutSetPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))
            assertEquals(
                listOf(ManualReadoutSetPreference("target", listOf("ENERGY"), mapOf("ENERGY" to "文言"))),
                restored.sets,
            )
            assertEquals(null, restored.sets.single().hotkeyVirtualKeyCode)
            assertEquals(null, restored.sets.single().hotkeyModifiers)
        }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `修飾キーフィールドのないホットキー保存済み旧データも読み出せる`() =
        runTest {
            val legacy =
                LegacyManualReadoutSetPreferences(
                    listOf(LegacyManualReadoutSetPreference("target", emptyList(), emptyMap(), 0x41)),
                )
            val bytes = ProtoBuf.encodeToByteArray(LegacyManualReadoutSetPreferences.serializer(), legacy)
            val restored = ManualReadoutSetPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))
            assertEquals(listOf(ManualReadoutSetPreference("target", hotkeyVirtualKeyCode = 0x41)), restored.sets)
            assertEquals(null, restored.sets.single().hotkeyModifiers)
        }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private data class LegacyManualReadoutSetPreferences(
    @ProtoNumber(1) val sets: List<LegacyManualReadoutSetPreference>,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
private data class LegacyManualReadoutSetPreference(
    @ProtoNumber(1) val id: String,
    @ProtoNumber(2) val itemKeys: List<String>,
    @ProtoNumber(3) val itemTexts: Map<String, String>,
    @ProtoNumber(4) val hotkeyVirtualKeyCode: Int? = null,
)
