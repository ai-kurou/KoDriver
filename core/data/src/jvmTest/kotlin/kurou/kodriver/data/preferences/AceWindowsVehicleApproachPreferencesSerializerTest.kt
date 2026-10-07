package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AceWindowsVehicleApproachPreferencesSerializerTest {
    @Test
    fun `デフォルト値は各Defaults定数と一致する`() {
        assertEquals(
            AceWindowsVehicleApproachPreferences(
                thresholdMeters = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
            ),
            AceWindowsVehicleApproachPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                AceWindowsVehicleApproachPreferences(
                    thresholdMeters = 7.0,
                    readoutText = "周囲に注意",
                    enabledStates = mapOf("ace_windows_vehicle_approach_start_readout" to false),
                )
            val output = ByteArrayOutputStream()
            AceWindowsVehicleApproachPreferencesSerializer.writeTo(original, output)

            val restored =
                AceWindowsVehicleApproachPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                AceWindowsVehicleApproachPreferencesSerializer.readFrom(corrupt)
            }
        }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `フィールド5のない旧データは閾値とスイッチを保持して既定文言になる`() =
        runTest {
            val old = LegacyPreferences(7.0, mapOf("ace_windows_vehicle_approach_start_readout" to false))
            val bytes = ProtoBuf.encodeToByteArray(LegacyPreferences.serializer(), old)
            val restored = AceWindowsVehicleApproachPreferencesSerializer.readFrom(ByteArrayInputStream(bytes))
            assertEquals(7.0, restored.thresholdMeters)
            assertEquals(old.enabledStates, restored.enabledStates)
            assertEquals(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT, restored.readoutText)
        }

    @Test
    fun `空文言もそのまま往復できる`() =
        runTest {
            val original = AceWindowsVehicleApproachPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            AceWindowsVehicleApproachPreferencesSerializer.writeTo(original, output)
            assertEquals(
                original,
                AceWindowsVehicleApproachPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    private data class LegacyPreferences(
        @ProtoNumber(1) val thresholdMeters: Double,
        @ProtoNumber(4) val enabledStates: Map<String, Boolean>,
    )
}
