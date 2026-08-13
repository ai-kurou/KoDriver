@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsVehicleDamagePreferencesSerializerTest {
    @Test
    fun `デフォルト値は enabledStates が空Map`() {
        assertEquals(LmuWindowsVehicleDamagePreferences(), LmuWindowsVehicleDamagePreferencesSerializer.defaultValue)
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = LmuWindowsVehicleDamagePreferences(enabledStates = mapOf("overheat" to true))
            val output = ByteArrayOutputStream()
            LmuWindowsVehicleDamagePreferencesSerializer.writeTo(original, output)

            val restored =
                LmuWindowsVehicleDamagePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                LmuWindowsVehicleDamagePreferencesSerializer.readFrom(corrupt)
            }
        }
}
