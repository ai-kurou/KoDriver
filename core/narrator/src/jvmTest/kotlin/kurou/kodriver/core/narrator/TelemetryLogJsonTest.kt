package kurou.kodriver.core.narrator

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Serializable
private data class Sample(
    val value: Int = 0,
    val nullable: String? = null,
)

@Serializable
private data class DoubleHolder(
    val value: Double,
)

class TelemetryLogJsonTest {
    @Test
    fun `encodeDefaultsとexplicitNullsが有効になっている`() {
        val json = TelemetryLogJson.encodeToString(Sample())

        assertEquals("""{"value":0,"nullable":null}""", json)
    }

    @Test
    fun `NaNなどの特殊な浮動小数点値はデフォルトでは許可しない`() {
        assertFailsWith<IllegalArgumentException> {
            TelemetryLogJson.encodeToString(DoubleHolder(Double.NaN))
        }
    }

    @Test
    fun `バックスラッシュ・ダブルクォート・改行等をエスケープする`() {
        val literal = "a\\b\"c\nd\re\tf".toJsonStringLiteral()

        assertEquals("\"a\\\\b\\\"c\\nd\\re\\tf\"", literal)
    }

    @Test
    fun `特殊文字を含まない文字列はそのまま引用符で囲む`() {
        val literal = "plain".toJsonStringLiteral()

        assertEquals("\"plain\"", literal)
    }

    @Test
    fun `個別エスケープを持たない制御文字はuXXXX形式でエスケープする`() {
        val literal = "abc".toJsonStringLiteral()

        assertEquals("\"a\\u0008b\\u000cc\"", literal)
    }
}
