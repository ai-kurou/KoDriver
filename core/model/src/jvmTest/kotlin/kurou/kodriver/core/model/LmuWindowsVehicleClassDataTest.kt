package kurou.kodriver.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LmuWindowsVehicleClassDataTest {
    @Test
    fun `fromRawValueは既知のクラス文字列を対応するインスタンスへ変換する`() {
        assertEquals(LmuWindowsVehicleClassData.Hypercar, LmuWindowsVehicleClassData.fromRawValue("Hyper"))
        assertEquals(LmuWindowsVehicleClassData.P2, LmuWindowsVehicleClassData.fromRawValue("LMP2"))
        assertEquals(LmuWindowsVehicleClassData.P2Elms, LmuWindowsVehicleClassData.fromRawValue("LMP2_ELMS"))
        assertEquals(LmuWindowsVehicleClassData.P3, LmuWindowsVehicleClassData.fromRawValue("LMP3"))
        assertEquals(LmuWindowsVehicleClassData.Gte, LmuWindowsVehicleClassData.fromRawValue("GTE"))
        assertEquals(LmuWindowsVehicleClassData.Gt3, LmuWindowsVehicleClassData.fromRawValue("GT3"))
    }

    @Test
    fun `fromRawValueは未知のクラス文字列をUnknownへ変換しnameに生文字列を保持する`() {
        val result = LmuWindowsVehicleClassData.fromRawValue("Formula2026")

        assertIs<LmuWindowsVehicleClassData.Unknown>(result)
        assertEquals("Formula2026", result.raw)
        assertEquals("Formula2026", result.name)
    }

    @Test
    fun `fromRawValueは空文字列をUnknownへ変換する`() {
        val result = LmuWindowsVehicleClassData.fromRawValue("")

        assertIs<LmuWindowsVehicleClassData.Unknown>(result)
        assertEquals("", result.name)
    }

    @Test
    fun `シリアライズ時はnameフィールドを持つJSONオブジェクトになる`() {
        val json = Json.encodeToString<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Gte)

        assertEquals("""{"name":"GTE"}""", json)
    }

    @Test
    fun `デシリアライズ時は既知の値を対応するインスタンスへ復元する`() {
        val result = Json.decodeFromString<LmuWindowsVehicleClassData>("""{"name":"LMP3"}""")

        assertEquals(LmuWindowsVehicleClassData.P3, result)
    }

    @Test
    fun `デシリアライズ時に未知の値はUnknownへ復元する`() {
        val result = Json.decodeFromString<LmuWindowsVehicleClassData>("""{"name":"Formula2026"}""")

        assertEquals(LmuWindowsVehicleClassData.Unknown("Formula2026"), result)
    }
}
