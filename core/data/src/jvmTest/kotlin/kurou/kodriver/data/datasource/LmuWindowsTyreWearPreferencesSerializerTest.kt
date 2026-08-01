@file:Suppress("FunctionNaming")

package kurou.kodriver.data.datasource

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.LmuWindowsTyreWearPreferences
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LmuWindowsTyreWearPreferencesSerializerTest {

    @Test
    fun `デフォルト値は thresholdPercentage が 50`() {
        assertEquals(
            LmuWindowsTyreWearPreferences(thresholdPercentage = 50),
            LmuWindowsTyreWearPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
        val original = LmuWindowsTyreWearPreferences(thresholdPercentage = 30)
        val output = ByteArrayOutputStream()
        LmuWindowsTyreWearPreferencesSerializer.writeTo(original, output)

        val restored =
            LmuWindowsTyreWearPreferencesSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
        val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

        assertFailsWith<CorruptionException> {
            LmuWindowsTyreWearPreferencesSerializer.readFrom(corrupt)
        }
    }
}
