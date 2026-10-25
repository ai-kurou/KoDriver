package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsManualReadoutItem
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutTemperatureItem
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsManualReadoutItemsTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `定義はキー順に名前と既定文言を持ち未知キーは空文字`() {
        val definitions = lmuWindowsManualReadoutItemDefinitions
        assertEquals(definitions.keys.toList(), lmuWindowsManualReadoutItemKeys())
        assertEquals(10, definitions.size)
        assertEquals(
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
            ),
            definitions.keys.take(2),
        )
        val energyLaps = definitions.getValue(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value)
        assertEquals("バーチャルエネルギーの残り周回数", energyLaps.name)
        assertEquals("バーチャルエネルギーは残り約{laps}周", energyLaps.textDefault)
        val laps = definitions.getValue(LmuWindowsReadoutItemKey.RemainingFuel.Laps.value)
        assertEquals("燃料の残り周回数", laps.name)
        assertEquals(LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT, laps.textDefault)
        assertEquals(laps.key, definitions.keys.last())
        val fuel = definitions.getValue(LmuWindowsReadoutItemKey.RemainingFuel.Root.value)
        assertEquals("燃料残量", fuel.name)
        assertEquals("燃料は残り{percent}パーセント", fuel.textDefault)
        assertEquals(LMU_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT, fuel.textDefault)
        definitions.values.forEach { definition ->
            assertEquals(definition.name, lmuWindowsManualReadoutItemName(definition.key))
            assertEquals(definition.textDefault, lmuWindowsManualReadoutItemTextDefault(definition.key))
            assertTrue(definition.name.isNotBlank())
            assertTrue(definition.textDefault.isNotBlank())
        }
        assertEquals("", lmuWindowsManualReadoutItemName("unknown"))
        assertEquals("", lmuWindowsManualReadoutItemTextDefault("unknown"))
    }

    @Test
    fun `手動読み上げ値と温度判定を保持する`() {
        val item = LmuWindowsManualReadoutItem("key", "text", 0.5, 90f, 123L, 4)
        assertEquals(0.5, item.ratio)
        assertEquals(90f, item.celsius)
        assertEquals(123L, item.lapTimeMs)
        assertEquals(4, item.remainingLaps)
        assertTrue(isLmuWindowsManualReadoutTemperatureItem(LmuWindowsReadoutItemKey.TyreTemperature.Root.value))
        assertEquals(false, isLmuWindowsManualReadoutTemperatureItem("unknown"))
    }
}
