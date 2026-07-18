package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.LmuWindowsRemainingVirtualEnergyLapsPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializerTest {

    @Test
    fun `デフォルト値は3周`() {
        assertEquals(
            LmuWindowsRemainingVirtualEnergyLapsPreferences(remainingVirtualEnergyLaps = 3),
            LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = LmuWindowsRemainingVirtualEnergyLapsPreferences(remainingVirtualEnergyLaps = 5)
        val output = ByteArrayOutputStream()
        LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializer.writeTo(original, output)

        val restored = LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializer.readFrom(corrupt)
        }
    }
}
