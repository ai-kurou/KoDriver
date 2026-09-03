package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReadoutItemDisplayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val expectedDisplayNames =
        listOf(
            "lmu_windows_vehicle_approach" to "車両接近",
            "lmu_windows_vehicle_approach_sustained" to "接近継続",
            "lmu_windows_vehicle_approach_start_readout" to "接近開始時の読み上げ",
            "lmu_windows_flag" to "フラッグ",
            "lmu_windows_blue_flag" to "ブルーフラッグ",
            "lmu_windows_sector_yellow_flag" to "イエローフラッグ",
            "lmu_windows_full_course_yellow" to "フルコースイエロー",
            "lmu_windows_red_flag" to "レッドフラッグ",
            "lmu_windows_vehicle_damage" to "車両故障",
            "lmu_windows_overheat" to "オーバーヒート",
            "lmu_windows_part_detached" to "部品脱落",
            "lmu_windows_tyre_temperature" to "タイヤ温度",
            "lmu_windows_tyre_temperature_overheat_warning" to "過熱警告",
            "lmu_windows_tyre_temperature_low_warning" to "低温警告",
            "lmu_windows_pit_timing" to "ピットタイミング",
            "lmu_windows_remaining_virtual_energy" to "バーチャルエナジー残量",
            "lmu_windows_tyre_wear" to "タイヤ摩耗",
            "lmu_windows_my_best_lap" to "自己ベストラップ",
            "gt7_ps5_my_best_lap" to "自己ベストラップ",
            "gt7_ps5_remaining_fuel_laps" to "燃料残り周回数",
            "gt7_ps5_remaining_fuel" to "燃料残量",
            "gt7_ps5_tyre_temperature" to "タイヤ温度",
            "ace_windows_flag" to "フラッグ",
            "ace_windows_white_flag" to "ホワイトフラッグ",
            "ace_windows_green_flag" to "グリーンフラッグ",
            "ace_windows_red_flag" to "レッドフラッグ",
            "ace_windows_blue_flag" to "ブルーフラッグ",
            "ace_windows_yellow_flag" to "イエローフラッグ",
            "ace_windows_black_flag" to "ブラックフラッグ",
            "ace_windows_black_white_flag" to "ブラック・ホワイトフラッグ",
            "ace_windows_checkered_flag" to "チェッカーフラッグ",
            "ace_windows_orange_circle_flag" to "オレンジボールフラッグ",
            "ace_windows_red_yellow_stripes_flag" to "レッド・イエローストライプフラッグ",
            "ace_windows_remaining_fuel" to "燃料残量",
            "ace_windows_vehicle_approach" to "車両接近",
            "ace_windows_my_best_lap" to "自己ベストラップ",
        )

    @Test
    fun `全readoutItemKeyValueを日本語の表示名に変換する`() {
        var actualDisplayNames by mutableStateOf(emptyList<String>())

        composeRule.setContent {
            actualDisplayNames = expectedDisplayNames.map { (value, _) -> readoutItemDisplayName(value) }
        }

        assertEquals(expectedDisplayNames.map { it.second }, actualDisplayNames)
    }

    @Test
    fun `未対応のreadoutItemKeyValueを渡すと表示名の取得で例外が発生する`() {
        assertFailsWith<IllegalStateException> {
            composeRule.setContent {
                readoutItemDisplayName("unknown_readout_item")
            }
        }
    }
}
