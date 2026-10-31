package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleDamagePreferencesSerializerTest {
    @Test
    fun `デフォルト値は enabledStates が空Map`() {
        assertEquals(LmuWindowsVehicleDamagePreferences(), LmuWindowsVehicleDamagePreferencesSerializer.defaultValue)
    }

    @Test
    fun `保存した空欄は復元後も既定文言へ戻らない`() =
        runTest {
            val original =
                LmuWindowsVehicleDamagePreferences(
                    overheatReadoutText = "",
                    partDetachedReadoutText = "",
                    tyreDetachedReadoutText = "",
                )
            val output = ByteArrayOutputStream()
            LmuWindowsVehicleDamagePreferencesSerializer.writeTo(original, output)
            assertEquals(
                original,
                LmuWindowsVehicleDamagePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
