package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemKeys
import org.junit.Test
import kotlin.test.assertEquals

class ManualReadoutPreviewTest {
    @Test
    fun `エナジー周回数はサンプル周回数に置換し空欄を除いて保存順に連結する`() {
        val key = LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("エナジーは残り約3周", manualReadoutPreview(state))
        assertEquals(
            "あと3 {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "あと{laps} {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "エナジーは残り約3周、タイヤ温度90℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, LmuWindowsReadoutItemKey.TyreTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `タイヤ摩耗周回数はサンプル周回数に置換し空欄を除いて保存順に連結する`() {
        val key = LmuWindowsReadoutItemKey.PitTiming.TyreWear.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("タイヤは残り約3周", manualReadoutPreview(state))
        assertEquals(
            "あと3 {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "あと{laps} {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "タイヤは残り約3周、タイヤ温度90℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, LmuWindowsReadoutItemKey.TyreTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `自己ベストはサンプルラップタイムに置換し空欄を除いて保存順に連結する`() {
        val key = LmuWindowsReadoutItemKey.MyBestLap.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("自己ベストラップ 1分23秒456", manualReadoutPreview(state))
        assertEquals(
            "記録1分23秒456 {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "記録{laptime} {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "自己ベストラップ 1分23秒456、タイヤ温度90℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, LmuWindowsReadoutItemKey.TyreTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `GT7の自己ベストはサンプルラップタイムに置換し空欄を除いて保存順に連結する`() {
        val key = Gt7Ps5ReadoutItemKey.MyBestLap.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("自己ベストラップ 1分23秒456", manualReadoutPreview(state))
        assertEquals(
            "記録1分23秒456 {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "記録{laptime} {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "自己ベストラップ 1分23秒456、タイヤ温度90℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, LmuWindowsReadoutItemKey.TyreTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `GT7の残り燃料周回数は3周でプレビューし空欄は読まない`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("燃料は残り約3周", manualReadoutPreview(state))
        assertEquals("あと3周", manualReadoutPreview(state.copy(itemTexts = mapOf(key to "あと{laps}周"))))
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `ACEの残り燃料の既定文言と編集文言はサンプル値に置換する`() {
        val key = AceWindowsReadoutItemKey.RemainingFuel.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("燃料は残り50パーセント", manualReadoutPreview(state))
        assertEquals(
            "残り50% {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}% {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `GT7の残り燃料の既定文言と編集文言はサンプル値に置換する`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("燃料は残り50パーセント", manualReadoutPreview(state))
        assertEquals(
            "残り50% {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}% {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `GT7のタイヤ温度の既定文言と編集文言はサンプル値に置換する`() {
        val key = Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("タイヤ温度90℃", manualReadoutPreview(state))
        assertEquals(
            "温度90℃ {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "温度{celsius}℃ {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "タイヤ温度90℃、ブレーキ温度500℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, LmuWindowsReadoutItemKey.BrakeTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `タイヤ温度の既定文言と編集文言はサンプル値に置換する`() {
        val key = LmuWindowsReadoutItemKey.TyreTemperature.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("タイヤ温度90℃", manualReadoutPreview(state))
        assertEquals(
            "温度90℃ {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "温度{celsius}℃ {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "タイヤ温度90℃、ブレーキ温度500℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, LmuWindowsReadoutItemKey.BrakeTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `ブレーキ温度の既定文言と編集文言はサンプル値に置換する`() {
        val key = LmuWindowsReadoutItemKey.BrakeTemperature.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("ブレーキ温度500℃", manualReadoutPreview(state))
        assertEquals(
            "温度500℃ {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "温度{celsius}℃ {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `ブレーキ残量の既定文言と編集文言はサンプル値に置換する`() {
        val key = LmuWindowsReadoutItemKey.BrakeWear.Root.value
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("ブレーキ残量50%", manualReadoutPreview(state))
        assertEquals(
            "残り50% {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}% {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `タイヤ摩耗の既定文言と編集文言はサンプル値に置換する`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id)[1]
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("タイヤ摩耗残り50%", manualReadoutPreview(state))
        assertEquals("残り50%", manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}%"))))
    }

    @Test
    fun `未選択と空欄はプレビューに含めない`() {
        assertEquals("", manualReadoutPreview(ManualReadoutSetDetailUiState()))
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).first()
        assertEquals(
            "",
            manualReadoutPreview(
                ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key), itemTexts = mapOf(key to "  ")),
            ),
        )
    }

    @Test
    fun `既定文言と編集文言の残量はサンプル値に置換し未知トークンは保持する`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).first()
        val state = ManualReadoutSetDetailUiState(selectedItemKeys = listOf(key))
        assertEquals("バーチャルエナジー残量は50パーセント", manualReadoutPreview(state))
        assertEquals(
            "残り50% {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}% {unknown}"))),
        )
    }

    @Test
    fun `保存済みの選択順に空欄以外を連結する`() {
        assertEquals(
            "後、先",
            manualReadoutPreview(
                ManualReadoutSetDetailUiState(
                    selectedItemKeys = listOf("second", "blank", "first", "missing"),
                    itemTexts = mapOf("first" to "先", "second" to "後", "blank" to ""),
                ),
            ),
        )
    }
}
