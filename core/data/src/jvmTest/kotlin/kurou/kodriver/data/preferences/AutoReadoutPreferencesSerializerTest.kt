package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalSerializationApi::class)
class AutoReadoutPreferencesSerializerTest {
    @Test
    fun `正常なバイト列をデシリアライズできる`() =
        runTest {
            val original =
                AutoReadoutPreferences(
                    simulatorStates =
                        mapOf(
                            "lmu_windows" to
                                SimulatorAutoReadoutState(enabledStates = mapOf("vehicle_approach" to true)),
                        ),
                )
            val bytes = ProtoBuf.encodeToByteArray(AutoReadoutPreferences.serializer(), original)

            val result = AutoReadoutPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `不正なバイト列はCorruptionExceptionをスローする`() =
        runTest {
            val invalidBytes = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x01)

            assertFailsWith<CorruptionException> {
                AutoReadoutPreferencesSerializer.readFrom(ByteArrayInputStream(invalidBytes))
            }
        }

    @Test
    fun `writeToしたバイト列をreadFromで復元できる`() =
        runTest {
            val original =
                AutoReadoutPreferences(
                    simulatorStates =
                        mapOf(
                            "lmu_windows" to
                                SimulatorAutoReadoutState(
                                    enabledStates =
                                        mapOf(
                                            "vehicle_approach" to true,
                                            "flag" to false,
                                            "vehicle_damage" to true,
                                        ),
                                ),
                        ),
                )
            val output = ByteArrayOutputStream()
            AutoReadoutPreferencesSerializer.writeTo(original, output)

            val result = AutoReadoutPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }
}
