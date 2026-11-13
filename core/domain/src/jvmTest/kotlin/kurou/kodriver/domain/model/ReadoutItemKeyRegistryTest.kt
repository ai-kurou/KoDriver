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
    fun `基準のspecを登録する`() {
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

    @Test
    fun `手動読み上げ対応のspecだけをSimulator順に返し案内表示の有無を引ける`() {
        val definition =
            ManualReadoutItemDefinition(
                "key",
                ManualReadoutItemCategory.FUEL,
                "名",
                "既定",
                "{p}",
                { emptyList() },
                { it },
                ChildKey("key"),
            )
        val lmu =
            SimulatorReadoutSpec(
                Simulator.LmuWindows,
                emptyList(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyList(),
                ManualReadoutSimulatorSpec(mapOf("key" to definition)),
            )
        val gt7 =
            SimulatorReadoutSpec(
                Simulator.Gt7Ps5,
                emptyList(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyList(),
                ManualReadoutSimulatorSpec(mapOf("key" to definition), showSimHubGuideLink = false),
            )
        ReadoutItemKeyRegistry.register(gt7)
        ReadoutItemKeyRegistry.register(lmu)

        assertEquals(
            listOf(Simulator.LmuWindows.id, Simulator.Gt7Ps5.id),
            ReadoutItemKeyRegistry.manualReadoutSpecs.map { it.first },
        )
        assertEquals(
            mapOf("key" to definition),
            ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.Gt7Ps5.id)?.itemDefinitions,
        )
        assertNull(ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.AceWindows.id))
        assertNull(ReadoutItemKeyRegistry.manualReadoutSpecOf("unknown"))
        assertEquals(true, ReadoutItemKeyRegistry.showsManualReadoutSimHubGuideLink(Simulator.LmuWindows.id))
        assertEquals(false, ReadoutItemKeyRegistry.showsManualReadoutSimHubGuideLink(Simulator.Gt7Ps5.id))
        assertEquals(true, ReadoutItemKeyRegistry.showsManualReadoutSimHubGuideLink("unknown"))
    }

    @Test
    fun `登録後に元の項目定義マップを変更してもレジストリには反映されない`() {
        val definition =
            ManualReadoutItemDefinition(
                "key",
                ManualReadoutItemCategory.FUEL,
                "名",
                "既定",
                "{p}",
                { emptyList() },
                { it },
                ChildKey("key"),
            )
        val definitions = mutableMapOf("key" to definition)
        ReadoutItemKeyRegistry.register(
            SimulatorReadoutSpec(
                Simulator.LmuWindows,
                emptyList(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyList(),
                ManualReadoutSimulatorSpec(definitions),
            ),
        )

        definitions.clear()

        assertEquals(
            mapOf("key" to definition),
            ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.LmuWindows.id)?.itemDefinitions,
        )
    }

    @Test
    fun `手動読み上げの語句は登録でき省略時は空で登録後の変更は反映されない`() {
        val definition =
            ManualReadoutItemDefinition(
                "key",
                ManualReadoutItemCategory.FUEL,
                "名",
                "既定",
                "{p}",
                { emptyList() },
                { it },
                ChildKey("key"),
            )
        val phrases = mutableListOf("燃料")

        fun lmu(manual: ManualReadoutSimulatorSpec) =
            SimulatorReadoutSpec(
                Simulator.LmuWindows,
                emptyList(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyList(),
                manual,
            )

        ReadoutItemKeyRegistry.register(lmu(ManualReadoutSimulatorSpec(mapOf("key" to definition))))
        assertEquals(
            emptyMap(),
            ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.LmuWindows.id)?.itemPhrases,
        )

        ReadoutItemKeyRegistry.register(
            lmu(ManualReadoutSimulatorSpec(mapOf("key" to definition), itemPhrases = mapOf("key" to phrases))),
        )
        phrases.add("ガソリン")

        assertEquals(
            mapOf("key" to listOf("燃料")),
            ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.LmuWindows.id)?.itemPhrases,
        )
    }

    @Test
    fun `手動読み上げspecの旧セット移行処理を登録後も保持し未指定なら何もしない`() {
        val definition =
            ManualReadoutItemDefinition(
                "key",
                ManualReadoutItemCategory.FUEL,
                "名",
                "既定",
                "{p}",
                { emptyList() },
                { it },
                ChildKey("key"),
            )

        fun lmu(manual: ManualReadoutSimulatorSpec) =
            SimulatorReadoutSpec(
                Simulator.LmuWindows,
                emptyList(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyList(),
                manual,
            )

        val set = ManualReadoutSet("s", listOf("old"))
        ReadoutItemKeyRegistry.register(lmu(ManualReadoutSimulatorSpec(mapOf("key" to definition))))
        assertEquals(
            set,
            ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.LmuWindows.id)?.migrateLegacySet?.invoke(set),
        )

        ReadoutItemKeyRegistry.register(
            lmu(
                ManualReadoutSimulatorSpec(
                    mapOf("key" to definition),
                    migrateLegacySet = { it.copy(itemKeys = listOf("new")) },
                ),
            ),
        )
        assertEquals(
            listOf("new"),
            ReadoutItemKeyRegistry
                .manualReadoutSpecOf(
                    Simulator.LmuWindows.id,
                )?.migrateLegacySet
                ?.invoke(set)
                ?.itemKeys,
        )
    }
}
