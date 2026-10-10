package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AceWindowsRemainingFuelPreferencesSerializerTest {
    @Test
    fun `デフォルト値は thresholdPercentage が 30`() {
        assertEquals(
            AceWindowsRemainingFuelPreferences(
                thresholdPercentage = 30,
                readoutText = ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
            ),
            AceWindowsRemainingFuelPreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = AceWindowsRemainingFuelPreferences(thresholdPercentage = 50, readoutText = "残り{percent}%")
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

    @Test
    fun `空欄文言は読み出しても既定値に戻らない`() =
        runTest {
            val original = AceWindowsRemainingFuelPreferences(readoutText = "")
            val output = ByteArrayOutputStream()
            AceWindowsRemainingFuelPreferencesSerializer.writeTo(original, output)

            assertEquals(
                original,
                AceWindowsRemainingFuelPreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }
}
