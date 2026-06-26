package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.Gt7Ps5RemainingFuelLapsPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Gt7Ps5RemainingFuelLapsPreferencesSerializerTest {

    @Test
    fun `デフォルト値は3周`() {
        assertEquals(
            Gt7Ps5RemainingFuelLapsPreferences(remainingFuelLaps = 3),
            Gt7Ps5RemainingFuelLapsPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() = runTest {
        val original = Gt7Ps5RemainingFuelLapsPreferences(remainingFuelLaps = 5)
        val output = ByteArrayOutputStream()
        Gt7Ps5RemainingFuelLapsPreferencesSerializer.writeTo(original, output)

        val restored = Gt7Ps5RemainingFuelLapsPreferencesSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() = runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            Gt7Ps5RemainingFuelLapsPreferencesSerializer.readFrom(corrupt)
        }
    }
}
