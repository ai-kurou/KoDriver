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
    fun `特殊な浮動小数点値を許可する設定でNaNをencodeできる`() {
        val json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(DoubleHolder(Double.NaN))

        assertEquals("""{"value":NaN}""", json)
    }

    @Test
    fun `特殊な浮動小数点値を許可する設定でもencodeDefaultsとexplicitNullsを維持する`() {
        val json = TelemetryLogJsonAllowingSpecialFloatingPoint.encodeToString(Sample())

        assertEquals("""{"value":0,"nullable":null}""", json)
    }

    @Test
    fun `オブジェクトのtoStringをrawフィールドに格納する`() {
        val json = Sample(value = 42).toRawTelemetryLogJson()

        assertEquals("""{"raw":"Sample(value=42, nullable=null)"}""", json)
    }

    @Test
    fun `rawフィールドの特殊文字をエスケープする`() {
        val json = "a\\b\"c\nd\re\tf".toRawTelemetryLogJson()

        assertEquals("""{"raw":"a\\b\"c\nd\re\tf"}""", json)
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

    @Test
    fun `state_previous_current_settings_observedAtMs_finalStateの順でJSONオブジェクトを組み立てる`() {
        val json =
            buildTelemetryLogJson(
                stateJson = """{"phase":"GREEN"}""",
                previous = TelemetryLogJsonPreviousField(name = "previousFlag", json = """{"value":1}"""),
                current = TelemetryLogJsonCurrentField(name = "flag", json = """{"value":2}"""),
                settingsJson = """{"enabled":true}""",
                observedAtMs = 12345L,
                finalStateJson = """{"phase":"YELLOW"}""",
            )

        assertEquals(
            """{"state":{"phase":"GREEN"},"previousFlag":{"value":1},"flag":{"value":2},""" +
                """"settings":{"enabled":true},"observedAtMs":12345,"finalState":{"phase":"YELLOW"}}""",
            json,
        )
    }

    @Test
    fun `previousのjsonがnullの場合はnullリテラルを埋め込む`() {
        val json =
            buildTelemetryLogJson(
                stateJson = """{"phase":"GREEN"}""",
                previous = TelemetryLogJsonPreviousField(name = "previousFlag", json = null),
                current = TelemetryLogJsonCurrentField(name = "flag", json = """{"value":2}"""),
                settingsJson = """{"enabled":true}""",
                observedAtMs = 12345L,
                finalStateJson = """{"phase":"YELLOW"}""",
            )

        assertEquals(
            """{"state":{"phase":"GREEN"},"previousFlag":null,"flag":{"value":2},""" +
                """"settings":{"enabled":true},"observedAtMs":12345,"finalState":{"phase":"YELLOW"}}""",
            json,
        )
    }
}
