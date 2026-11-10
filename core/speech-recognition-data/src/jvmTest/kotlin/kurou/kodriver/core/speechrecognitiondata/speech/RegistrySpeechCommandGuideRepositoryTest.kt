package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.ManualReadoutSimulatorSpec
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SimulatorReadoutSpec
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import kotlin.test.Test
import kotlin.test.assertEquals

class RegistrySpeechCommandGuideRepositoryTest {
    private data class TestKey(
        override val value: String,
    ) : ReadoutItemKey

    private fun definition(
        key: String,
        category: ManualReadoutItemCategory,
        name: String,
    ) = ManualReadoutItemDefinition(key, category, name, "既定", "{p}", { emptyList() }, { it }, TestKey(key))

    private fun register(
        simulator: Simulator,
        manualReadout: ManualReadoutSimulatorSpec?,
    ) = ReadoutItemKeyRegistry.register(
        SimulatorReadoutSpec(
            simulator,
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap(),
            emptyList(),
            manualReadout,
        ),
    )

    @Test
    fun `項目をカテゴリ順にまとめ語句を持たない項目は含めない`() {
        val definitions =
            listOf(
                definition("lap", ManualReadoutItemCategory.LAP, "ベストラップ"),
                definition("fuel", ManualReadoutItemCategory.FUEL, "燃料残量"),
                definition("fuel_laps", ManualReadoutItemCategory.FUEL, "燃料周回数"),
                definition("silent", ManualReadoutItemCategory.TYRE, "語句なし"),
            )
        register(
            Simulator.LmuWindows,
            ManualReadoutSimulatorSpec(
                definitions.associateBy { it.key },
                itemPhrases =
                    mapOf(
                        "lap" to listOf("自己ベスト"),
                        "fuel" to listOf("燃料", "ガソリン"),
                        "fuel_laps" to listOf("燃料周回"),
                        "silent" to emptyList(),
                    ),
            ),
        )

        assertEquals(
            listOf(
                SpeechCommandGuideGroup(
                    ManualReadoutItemCategory.FUEL,
                    listOf(
                        SpeechCommandGuideItem("fuel", "燃料残量", listOf("燃料", "ガソリン")),
                        SpeechCommandGuideItem("fuel_laps", "燃料周回数", listOf("燃料周回")),
                    ),
                ),
                SpeechCommandGuideGroup(
                    ManualReadoutItemCategory.LAP,
                    listOf(SpeechCommandGuideItem("lap", "ベストラップ", listOf("自己ベスト"))),
                ),
            ),
            RegistrySpeechCommandGuideRepository().guideGroups(Simulator.LmuWindows),
        )
    }

    @Test
    fun `手動読み上げに対応しないシミュレーターは空を返す`() {
        register(Simulator.Gt7Ps5, null)

        assertEquals(emptyList(), RegistrySpeechCommandGuideRepository().guideGroups(Simulator.Gt7Ps5))
    }

    @Test
    fun `登録されていないシミュレーターは空を返す`() {
        assertEquals(emptyList(), RegistrySpeechCommandGuideRepository().guideGroups(Simulator.AceWindows))
    }
}
