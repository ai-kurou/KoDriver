package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFailsWith

class ReadoutItemDisplayNameTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `既知のreadoutItemIdを日本語名に変換する`() {
        val expectedDisplayNames =
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
            )

        composeRule.setContent {
            expectedDisplayNames.forEach { (readoutItemId, _) ->
                Text(readoutItemDisplayName(readoutItemId))
            }
        }

        expectedDisplayNames.groupingBy { it.second }.eachCount().forEach { (displayName, count) ->
            composeRule.onAllNodesWithText(displayName).assertCountEquals(count)
        }
    }

    @Test
    fun `未対応のreadoutItemIdを渡すと表示名の取得で例外が発生する`() {
        assertFailsWith<IllegalStateException> {
            composeRule.setContent {
                readoutItemDisplayName("unknown_readout_item")
            }
        }
    }
}
