package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemKeys
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("TooManyFunctions")
class ManualReadoutPreviewTest {
    @Test
    fun `各シミュレーターの全項目にプレースホルダー挿入と未知検出定義があり未知キーは空になる`() {
        Simulator.entries.forEach { simulator ->
            manualReadoutItemKeys(simulator.id).forEach { key ->
                val definition = manualReadoutPlaceholderDefinitions.getValue(simulator.id to key)
                assertEquals(true, previewFormatters.containsKey(simulator.id to key))
                assertEquals(definition.insertText, insertPlaceholder(simulator.id, key))
                assertEquals(emptyList(), unknownPlaceholders(simulator.id, key, definition.insertText))
            }
        }
        assertEquals("", insertPlaceholder("unknown", "unknown"))
        assertEquals(emptyList(), unknownPlaceholders("unknown", "unknown", "{unknown}"))
        val aceKey = AceWindowsReadoutItemKey.MyBestLap.Root.value
        assertEquals("", insertPlaceholder("", aceKey))
        assertEquals(emptyList(), unknownPlaceholders("", aceKey, "{laptime}"))
    }

    @Test
    fun `同名項目キーの変換はシミュレーター別に分離される`() {
        val aceKey = AceWindowsReadoutItemKey.MyBestLap.Root.value
        assertEquals(ACE_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER, insertPlaceholder(Simulator.AceWindows.id, aceKey))
        assertEquals(emptyList(), unknownPlaceholders(Simulator.Gt7Ps5.id, aceKey, "{laptime}"))
        assertEquals(
            "記録{laptime}",
            manualReadoutPreview(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.Gt7Ps5.id,
                    selectedItemKeys = listOf(aceKey),
                    itemTexts = mapOf(aceKey to "記録{laptime}"),
                ),
            ),
        )
    }

    @Test
    fun `ACEタイヤ温度をサンプル温度に置換する`() {
        val key = AceWindowsReadoutItemKey.TyreTemperature.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.AceWindows.id, selectedItemKeys = listOf(key))
        assertEquals("タイヤ温度90℃", manualReadoutPreview(state))
        assertEquals("温度90 {other}", manualReadoutPreview(state.copy(itemTexts = mapOf(key to "温度{celsius} {other}"))))
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `ACE自己ベストラップをサンプルタイムに置換する`() {
        val key = AceWindowsReadoutItemKey.MyBestLap.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.AceWindows.id, selectedItemKeys = listOf(key))
        assertEquals("自己ベストラップ 1分23秒456", manualReadoutPreview(state))
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `エネルギー周回数はサンプル周回数に置換し空欄を除いて保存順に連結する`() {
        val key = LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
        assertEquals("バーチャルエネルギーは残り約3周", manualReadoutPreview(state))
        assertEquals(
            "エネルギーは残り約3周",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "エネルギーは残り約{laps}周"))),
        )
        assertEquals(
            "あと3 {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "あと{laps} {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "バーチャルエネルギーは残り約3周、タイヤ温度90℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, LmuWindowsReadoutItemKey.TyreTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `タイヤ摩耗周回数はサンプル周回数に置換し空欄を除いて保存順に連結する`() {
        val key = LmuWindowsReadoutItemKey.PitTiming.TyreWear.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
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
    fun `燃料周回数はサンプル周回数に置換し空欄を除いて保存順に連結する`() {
        val key = LmuWindowsReadoutItemKey.RemainingFuel.Laps.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
        assertEquals("燃料は残り約3周", manualReadoutPreview(state))
        assertEquals(
            "あと3 {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "あと{laps} {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "燃料は残り約3周、タイヤ温度90℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, LmuWindowsReadoutItemKey.TyreTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `自己ベストはサンプルラップタイムに置換し空欄を除いて保存順に連結する`() {
        val key = LmuWindowsReadoutItemKey.MyBestLap.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
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
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.Gt7Ps5.id, selectedItemKeys = listOf(key))
        assertEquals("自己ベストラップ 1分23秒456", manualReadoutPreview(state))
        assertEquals(
            "記録1分23秒456 {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "記録{laptime} {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "自己ベストラップ 1分23秒456、タイヤ温度90℃",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value)),
            ),
        )
    }

    @Test
    fun `GT7の残り燃料周回数は3周でプレビューし空欄は読まない`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.Gt7Ps5.id, selectedItemKeys = listOf(key))
        assertEquals("燃料は残り約3周", manualReadoutPreview(state))
        assertEquals("あと3周", manualReadoutPreview(state.copy(itemTexts = mapOf(key to "あと{laps}周"))))
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `LMUの燃料残量の既定文言と編集文言はサンプル値に置換する`() {
        val key = LmuWindowsReadoutItemKey.RemainingFuel.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
        assertEquals("燃料は残り50パーセント", manualReadoutPreview(state))
        assertEquals(
            "残り50% {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}% {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `ACEの残り燃料の既定文言と編集文言はサンプル値に置換する`() {
        val key = AceWindowsReadoutItemKey.RemainingFuel.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.AceWindows.id, selectedItemKeys = listOf(key))
        assertEquals("燃料は残り50パーセント", manualReadoutPreview(state))
        assertEquals(
            "残り50% {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}% {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `ACEの残り燃料の周回数の既定文言と編集文言はサンプル値に置換する`() {
        val key = AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.AceWindows.id, selectedItemKeys = listOf(key))
        assertEquals("燃料は残り約5周", manualReadoutPreview(state))
        assertEquals("あと5周", manualReadoutPreview(state.copy(itemTexts = mapOf(key to "あと{laps}周"))))
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `GT7の残り燃料の既定文言と編集文言はサンプル値に置換する`() {
        val key = Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.Gt7Ps5.id, selectedItemKeys = listOf(key))
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
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.Gt7Ps5.id, selectedItemKeys = listOf(key))
        assertEquals("タイヤ温度90℃", manualReadoutPreview(state))
        assertEquals(
            "温度90℃ {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "温度{celsius}℃ {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
        assertEquals(
            "タイヤ温度90℃、燃料は残り約3周",
            manualReadoutPreview(
                state.copy(selectedItemKeys = listOf(key, Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value)),
            ),
        )
    }

    @Test
    fun `タイヤ温度の既定文言と編集文言はサンプル値に置換する`() {
        val key = LmuWindowsReadoutItemKey.TyreTemperature.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
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
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
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
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
        assertEquals("ブレーキ残量50%", manualReadoutPreview(state))
        assertEquals(
            "残り50% {unknown}",
            manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}% {unknown}"))),
        )
        assertEquals("", manualReadoutPreview(state.copy(itemTexts = mapOf(key to ""))))
    }

    @Test
    fun `タイヤ摩耗の既定文言と編集文言はサンプル値に置換する`() {
        val key = LmuWindowsReadoutItemKey.TyreWear.Root.value
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
        assertEquals("タイヤ摩耗残り50%", manualReadoutPreview(state))
        assertEquals("残り50%", manualReadoutPreview(state.copy(itemTexts = mapOf(key to "残り{percent}%"))))
    }

    @Test
    fun `未選択と空欄はプレビューに含めない`() {
        assertEquals("", manualReadoutPreview(ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id)))
        val key = lmuWindowsManualReadoutItemKeys().first()
        assertEquals(
            "",
            manualReadoutPreview(
                ManualReadoutSetDetailUiState(
                    simulatorId = Simulator.LmuWindows.id,
                    selectedItemKeys = listOf(key),
                    itemTexts =
                        mapOf(key to "  "),
                ),
            ),
        )
    }

    @Test
    fun `既定文言と編集文言の残量はサンプル値に置換し未知トークンは保持する`() {
        val key = lmuWindowsManualReadoutItemKeys().first()
        val state = ManualReadoutSetDetailUiState(simulatorId = Simulator.LmuWindows.id, selectedItemKeys = listOf(key))
        assertEquals("バーチャルエネルギー残量は50パーセント", manualReadoutPreview(state))
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
                    simulatorId = Simulator.LmuWindows.id,
                    selectedItemKeys = listOf("second", "blank", "first", "missing"),
                    itemTexts = mapOf("first" to "先", "second" to "後", "blank" to ""),
                ),
            ),
        )
    }
}
