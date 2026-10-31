package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class ThemePreferencesSerializerTest {
    @Test
    fun `readFromでThemePreferencesを復元できる`() =
        runTest {
            val original = ThemePreferences(mode = "dark")
            val bytes = ProtoBuf.encodeToByteArray(ThemePreferences.serializer(), original)

            val result = ThemePreferencesSerializer.readFrom(ByteArrayInputStream(bytes))

            assertEquals(original, result)
        }

    @Test
    fun `writeToでThemePreferencesを書き込める`() =
        runTest {
            val original = ThemePreferences(mode = "light")
            val output = ByteArrayOutputStream()

            ThemePreferencesSerializer.writeTo(original, output)
            val result = ThemePreferencesSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(original, result)
        }

    @Test
    fun `デフォルト値はsystem`() {
        assertEquals(THEME_MODE_DEFAULT.id, ThemePreferencesSerializer.defaultValue.mode)
    }
}
