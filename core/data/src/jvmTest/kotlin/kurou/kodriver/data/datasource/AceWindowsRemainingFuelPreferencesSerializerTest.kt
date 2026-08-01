@file:Suppress("FunctionNaming")

package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.AceWindowsRemainingFuelPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AceWindowsRemainingFuelPreferencesSerializerTest {

    @Test
    fun `デフォルト値は thresholdPercentage が 30`() {
        assertEquals(
            AceWindowsRemainingFuelPreferences(thresholdPercentage = 30),
            AceWindowsRemainingFuelPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
        val original = AceWindowsRemainingFuelPreferences(thresholdPercentage = 50)
        val output = ByteArrayOutputStream()
        AceWindowsRemainingFuelPreferencesSerializer.writeTo(original, output)

        val restored =
            AceWindowsRemainingFuelPreferencesSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            AceWindowsRemainingFuelPreferencesSerializer.readFrom(corrupt)
        }
    }
}
