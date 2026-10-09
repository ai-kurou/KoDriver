package kurou.kodriver.data.preferences

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Gt7Ps5TyreTemperaturePreferencesSerializerTest {
    @Test
    fun `デフォルト値は100度`() {
        assertEquals(
            Gt7Ps5TyreTemperaturePreferences(
                highThresholdCelsius = 100,
            ),
            Gt7Ps5TyreTemperaturePreferencesSerializer.defaultValue,
        )
    }

    @Test
    fun `書き込んだ値を読み出せる`() =
        runTest {
            val original = Gt7Ps5TyreTemperaturePreferences(highThresholdCelsius = 95)
            val output = ByteArrayOutputStream()
            Gt7Ps5TyreTemperaturePreferencesSerializer.writeTo(original, output)

            val restored =
                Gt7Ps5TyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(output.toByteArray()),
                )

            assertEquals(original, restored)
        }

    @Test
    fun `閾値が省略された旧データは95度を維持する`() =
        runTest {
            // 旧デフォルト値のみの空データと、フィールド3に文言だけを保存した旧データ。
            for ((bytes, text) in listOf(
                byteArrayOf() to GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                byteArrayOf(0x1A, 0x04, 0x74, 0x65, 0x73, 0x74) to "test",
            )) {
                val restored = Gt7Ps5TyreTemperaturePreferencesSerializer.readFrom(ByteArrayInputStream(bytes))
                assertEquals(95, restored.highThresholdCelsius)
                assertEquals(text, restored.overheatReadoutText)
            }
        }

    @Test
    fun `新規設定の100度は書き込み後も維持する`() =
        runTest {
            val original = Gt7Ps5TyreTemperaturePreferencesSerializer.defaultValue
            val output = ByteArrayOutputStream()
            Gt7Ps5TyreTemperaturePreferencesSerializer.writeTo(original, output)
            assertEquals(
                original,
                Gt7Ps5TyreTemperaturePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
            )
        }

    @Test
    fun `不正なバイト列で CorruptionException が発生する`() =
        runTest {
            val corrupt = ByteArrayInputStream(byteArrayOf(0x00, 0xFF.toByte(), 0x42))

            assertFailsWith<CorruptionException> {
                Gt7Ps5TyreTemperaturePreferencesSerializer.readFrom(corrupt)
            }
        }

    @Test
    fun `文言未保存の旧データは既定文言を読み出す`() =
        runTest {
            val restored =
                Gt7Ps5TyreTemperaturePreferencesSerializer.readFrom(
                    ByteArrayInputStream(byteArrayOf(0x08, 0x64)),
                )
            assertEquals(100, restored.highThresholdCelsius)
            assertEquals(GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT, restored.overheatReadoutText)
        }

    @Test
    fun `カスタム文言と空文言を他の設定とともに往復できる`() =
        runTest {
            listOf("注意{celsius}度", "", " ").forEach { text ->
                val original = Gt7Ps5TyreTemperaturePreferences(105, mapOf("overheat_warning" to false), text)
                val output = ByteArrayOutputStream()
                Gt7Ps5TyreTemperaturePreferencesSerializer.writeTo(original, output)
                assertEquals(
                    original,
                    Gt7Ps5TyreTemperaturePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray())),
                )
            }
        }
}
