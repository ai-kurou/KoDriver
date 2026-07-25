package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.LmuWindowsPitTimingPreferences
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsPitTimingPreferencesSerializerTest {

    @Test
    fun `デフォルト値は両方とも3周`() {
        assertEquals(
            LmuWindowsPitTimingPreferences(
                virtualEnergyLaps = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                tyreWearLaps = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT,
            ),
            LmuWindowsPitTimingPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = LmuWindowsPitTimingPreferences(virtualEnergyLaps = 5, tyreWearLaps = 1)
        val output = ByteArrayOutputStream()
        LmuWindowsPitTimingPreferencesSerializer.writeTo(original, output)

        val restored = LmuWindowsPitTimingPreferencesSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            LmuWindowsPitTimingPreferencesSerializer.readFrom(corrupt)
        }
    }
}
