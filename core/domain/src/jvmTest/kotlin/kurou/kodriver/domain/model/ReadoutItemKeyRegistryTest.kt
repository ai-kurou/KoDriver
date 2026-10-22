package kurou.kodriver.domain.model

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ReadoutItemKeyRegistryTest {
    private data class TestKey(
        override val value: String,
        override val supportsQueue: Boolean,
    ) : ReadoutItemKey.TopLevel

    private data class ChildKey(
        override val value: String,
    ) : ReadoutItemKey

    @BeforeTest
    fun registerBaselineSpecs() {
        Simulator.entries.forEachIndexed { index, simulator ->
            ReadoutItemKeyRegistry.register(spec(simulator, "baseline_$index"))
        }
    }

    private fun spec(
        simulator: Simulator,
        prefix: String,
        enabled: Boolean = true,
    ): SimulatorReadoutSpec {
        val root = TestKey("${prefix}_root", supportsQueue = true)
        val child = ChildKey("${prefix}_child")
        return SimulatorReadoutSpec(
            simulator = simulator,
            keys = listOf(root, child),
            autoReadoutEnabledStateDefault = mapOf(root to enabled),
            startSoundEnabledStateDefault = mapOf(root to true),
            queueEnabledStateDefault = mapOf(root to false),
            defaultAutoReadoutOrder = listOf(root),
        )
    }

    @Test
    fun `registerは同じSimulatorのspecを冪等に上書きしentriesをSimulator順に返す`() {
        val lmu = spec(Simulator.LmuWindows, "lmu")
        val gt7 = spec(Simulator.Gt7Ps5, "gt7")
        val ace = spec(Simulator.AceWindows, "ace")
        ReadoutItemKeyRegistry.register(ace)
        ReadoutItemKeyRegistry.register(gt7)
        ReadoutItemKeyRegistry.register(lmu)
        ReadoutItemKeyRegistry.register(lmu)

        assertEquals(lmu.keys + gt7.keys + ace.keys, ReadoutItemKey.entries)
        assertEquals(lmu.keys + gt7.keys + ace.keys, ReadoutItemKeyRegistry.entries)
        assertEquals(lmu.keys.first(), ReadoutItemKey.fromValue("lmu_root"))
        assertNull(ReadoutItemKey.fromValue("unknown"))
        assertEquals(listOf(lmu.keys.first()), ReadoutItemKey.topLevelEntriesOf(Simulator.LmuWindows))
    }

    @Test
    fun `registerで同じSimulatorのspecを上書きする`() {
        ReadoutItemKeyRegistry.register(spec(Simulator.Gt7Ps5, "first"))
        val replacement = spec(Simulator.Gt7Ps5, "second", enabled = false)
        ReadoutItemKeyRegistry.register(replacement)

        assertEquals(replacement.keys, ReadoutItemKeyRegistry.specOf(Simulator.Gt7Ps5).keys)
        assertEquals(false, READOUT_ENABLED_STATE_DEFAULT.getValue(Simulator.Gt7Ps5).values.first())
    }

    @Test
    fun `未登録Simulatorの参照は明示的に失敗する`() {
        val registry = ReadoutItemKeyRegistryState()

        val exception = assertFailsWith<IllegalStateException> { registry.specOf(Simulator.LmuWindows) }
        assertEquals("ReadoutItemKeyRegistry: lmu_windows is not registered", exception.message)
        assertFailsWith<IllegalStateException> { registry.topLevelEntriesOf(Simulator.LmuWindows) }
    }

    @Test
    fun `登録済みspecから集約DefaultsとreadoutEnabledを合成する`() {
        val registered = spec(Simulator.LmuWindows, "lmu", enabled = false)
        ReadoutItemKeyRegistry.register(registered)

        assertEquals(
            false,
            READOUT_ENABLED_STATE_DEFAULT.getValue(Simulator.LmuWindows).getValue(registered.keys.first()),
        )
        assertEquals(true, READOUT_ENABLED_STATE_DEFAULT.getValue(Simulator.Gt7Ps5).values.first())
        assertEquals(false, QUEUE_ENABLED_STATE_DEFAULT.getValue(registered.keys.first()))
        assertEquals(true, READOUT_START_SOUND_ENABLED_STATE_DEFAULT.getValue(registered.keys.first()))
        assertEquals(listOf(registered.keys.first()), defaultAutoReadoutOrder(Simulator.LmuWindows))
        assertEquals(false, emptyMap<ReadoutItemKey, Boolean>().readoutEnabled(registered.keys.first()))
        assertEquals(true, emptyMap<ReadoutItemKey, Boolean>().readoutEnabled(registered.keys.last()))
    }

    @Test
    fun `registeredSpecsはSimulator順で登録済みのspecだけを返す`() {
        assertEquals(Simulator.entries, ReadoutItemKeyRegistry.registeredSpecs.map { it.simulator })
    }
}
