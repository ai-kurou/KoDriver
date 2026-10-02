package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsVehicleApproachPreferencesSerializerTest {
    @Test
    fun `デフォルト値は初期設定を返す`() {
        assertEquals(
            LmuWindowsVehicleApproachPreferences(
                skipFirstLap = LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT,
            ),
            LmuWindowsVehicleApproachPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original =
                LmuWindowsVehicleApproachPreferences(
                    skipFirstLap = true,
                    startLeftReadoutText = "左注意",
                    startRightReadoutText = "",
                    enabledStates = mapOf("lmu_windows_vehicle_approach_sustained" to false),
                    sustainedReadoutType = "left_right_sustained",
                )
            val output = ByteArrayOutputStream()
            LmuWindowsVehicleApproachPreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsVehicleApproachPreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `廃止したProtoNumber 3を含む旧設定では開始文言が既定値になる`() =
        runTest {
            // field 1: false、廃止したfield 3: "left_right_approach"、field 5: "left_right_sustained"。
            val oldType = "left_right_approach".encodeToByteArray()
            val sustained = "left_right_sustained".encodeToByteArray()
            val legacy =
                byteArrayOf(0x08, 0, 0x1a, oldType.size.toByte()) + oldType +
                    byteArrayOf(0x2a, sustained.size.toByte()) + sustained

            val restored = LmuWindowsVehicleApproachPreferencesSerializer.readFrom(ByteArrayInputStream(legacy))

            assertEquals("カーレフト", restored.startLeftReadoutText)
            assertEquals("カーライト", restored.startRightReadoutText)
            assertEquals(false, restored.skipFirstLap)
            assertEquals("left_right_sustained", restored.sustainedReadoutType)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsVehicleApproachPreferencesSerializer.readFrom(corrupt)
            }
        }
}
