package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsManualReadoutItem
import kurou.kodriver.domain.model.LmuWindowsManualReadoutValue
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutClassCarGapItem
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutTemperatureItem
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsManualReadoutItemsTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `全項目はプレースホルダーを持ち既定文言に含まれプレビューで置換される`() {
        lmuWindowsManualReadoutItemDefinitions.values.forEach { definition ->
            assertTrue(definition.placeholder.isNotEmpty())
            assertEquals(emptyList(), definition.findUnknownPlaceholders(definition.placeholder))
            assertTrue(definition.textDefault.contains(definition.placeholder))
            assertFalse(definition.previewFormatter(definition.textDefault).contains(definition.placeholder))
        }
    }

    @Test
    fun `定義はキー順に名前と既定文言を持ち未知キーは空文字`() {
        val definitions = lmuWindowsManualReadoutItemDefinitions
        assertEquals(definitions.keys.toList(), lmuWindowsManualReadoutItemKeys())
        assertEquals(11, definitions.size)
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
        assertEquals("燃料残り周回数", laps.name)
        assertEquals(LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT, laps.textDefault)
        val gap = definitions.getValue(LmuWindowsReadoutItemKey.ClassCarGap.Root.value)
        assertEquals("クラス内前後車とのギャップ", gap.name)
        assertEquals("{ahead}、{behind}", gap.textDefault)
        assertEquals("{ahead}", gap.placeholder)
        assertEquals(listOf(laps.key, gap.key), definitions.keys.toList().takeLast(2))
        val fuel = definitions.getValue(LmuWindowsReadoutItemKey.RemainingFuel.Root.value)
        assertEquals("燃料残量", fuel.name)
        assertEquals("燃料は残り{percent}%", fuel.textDefault)
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
        val item = LmuWindowsManualReadoutItem("key", "text", LmuWindowsManualReadoutValue.Ratio(0.5))
        assertEquals(LmuWindowsManualReadoutValue.Ratio(0.5), item.value)
        assertTrue(isLmuWindowsManualReadoutTemperatureItem(LmuWindowsReadoutItemKey.TyreTemperature.Root.value))
        assertEquals(false, isLmuWindowsManualReadoutTemperatureItem("unknown"))
    }

    @Test
    fun `クラス内ギャップ項目だけをクラス内ギャップ項目と判定する`() {
        assertTrue(isLmuWindowsManualReadoutClassCarGapItem(LmuWindowsReadoutItemKey.ClassCarGap.Root.value))
        assertEquals(false, isLmuWindowsManualReadoutClassCarGapItem(LmuWindowsReadoutItemKey.MyBestLap.Root.value))
    }

    @Test
    fun `全項目キーは期待する開始音キーに解決される`() {
        val expected =
            mapOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to
                    LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to LmuWindowsReadoutItemKey.PitTiming.Root,
                LmuWindowsReadoutItemKey.PitTiming.TyreWear to LmuWindowsReadoutItemKey.PitTiming.Root,
                LmuWindowsReadoutItemKey.RemainingFuel.Laps to LmuWindowsReadoutItemKey.RemainingFuel.Root,
                LmuWindowsReadoutItemKey.RemainingFuel.Root to LmuWindowsReadoutItemKey.RemainingFuel.Root,
                LmuWindowsReadoutItemKey.ClassCarGap.Root to LmuWindowsReadoutItemKey.VehicleApproach.Root,
                LmuWindowsReadoutItemKey.TyreWear.Root to LmuWindowsReadoutItemKey.TyreWear.Root,
                LmuWindowsReadoutItemKey.BrakeWear.Root to LmuWindowsReadoutItemKey.BrakeWear.Root,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root to LmuWindowsReadoutItemKey.BrakeTemperature.Root,
                LmuWindowsReadoutItemKey.MyBestLap.Root to LmuWindowsReadoutItemKey.MyBestLap.Root,
                LmuWindowsReadoutItemKey.TyreTemperature.Root to LmuWindowsReadoutItemKey.TyreTemperature.Root,
            ).mapKeys { it.key.value }
        assertEquals(expected.keys, lmuWindowsManualReadoutItemKeys().toSet())
        expected.forEach { (itemKey, eventKey) ->
            assertEquals(eventKey, lmuWindowsManualReadoutItemDefinitions.getValue(itemKey).eventKey)
        }
    }
}
