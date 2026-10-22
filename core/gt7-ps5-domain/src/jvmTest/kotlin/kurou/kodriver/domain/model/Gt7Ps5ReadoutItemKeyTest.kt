package kurou.kodriver.domain.model

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_AUTO_READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5ReadoutItemKeyTest {
    @BeforeTest
    fun registerReadoutKeys() {
        registerGt7Ps5ReadoutItemKeys()
    }

    @Test
    fun `GT7 keys retain legacy order and every key round trips`() {
        assertEquals(8, GT7_PS5_READOUT_ITEM_KEYS.size)
        assertEquals(Gt7Ps5ReadoutItemKey.MyBestLap.Root, GT7_PS5_READOUT_ITEM_KEYS.first())
        assertEquals(Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning, GT7_PS5_READOUT_ITEM_KEYS.last())
        assertEquals(
            GT7_PS5_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>(),
            ReadoutItemKey.topLevelEntriesOf(Simulator.Gt7Ps5),
        )
        GT7_PS5_READOUT_ITEM_KEYS.forEach { key -> assertEquals(key, ReadoutItemKey.fromValue(key.value)) }
    }

    @Test
    fun `GT7 defaults cover all TopLevel keys and queueable keys`() {
        val topLevel = GT7_PS5_READOUT_ITEM_KEYS.filterIsInstance<ReadoutItemKey.TopLevel>()
        assertEquals(topLevel.toSet(), GT7_PS5_AUTO_READOUT_ENABLED_STATE_DEFAULT.keys)
        assertEquals(topLevel.toSet(), GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT.keys)
        assertEquals(topLevel.filter { it.supportsQueue }.toSet(), GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT.keys)
        assertEquals(Simulator.Gt7Ps5, gt7Ps5ReadoutSpec.simulator)
        assertEquals(
            listOf(
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root,
            ),
            gt7Ps5ReadoutSpec.defaultAutoReadoutOrder,
        )
        assertEquals(GT7_PS5_READOUT_ITEM_KEYS, ReadoutItemKeyRegistry.specOf(Simulator.Gt7Ps5).keys)
    }
}
