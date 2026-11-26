package kurou.kodriver.domain.speechrecognition.model

import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.ManualReadoutSimulatorSpec
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SpeechCommandGuideTest {
    private data class TestKey(
        override val value: String,
    ) : ReadoutItemKey

    private fun definition(
        key: String,
        category: ManualReadoutItemCategory,
        name: String,
    ) = ManualReadoutItemDefinition(key, category, name, "既定", "{p}", { emptyList() }, { it }, TestKey(key))

    @Test
    fun `項目をカテゴリ順にまとめ語句が空または未登録の項目は含めない`() {
        val definitions =
            listOf(
                definition("lap", ManualReadoutItemCategory.TIME, "ベストラップ"),
                definition("fuel", ManualReadoutItemCategory.FUEL, "燃料残量"),
                definition("fuel_laps", ManualReadoutItemCategory.FUEL, "燃料周回数"),
                definition("silent", ManualReadoutItemCategory.TYRE, "語句なし"),
                definition("unlisted", ManualReadoutItemCategory.BRAKE, "語句の登録なし"),
            )
        val spec =
            ManualReadoutSimulatorSpec(
                definitions.associateBy { it.key },
                itemPhrases =
                    mapOf(
                        "lap" to listOf("自己ベスト"),
                        "fuel" to listOf("燃料", "ガソリン"),
                        "fuel_laps" to listOf("燃料周回"),
                        "silent" to emptyList(),
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
                    ManualReadoutItemCategory.TIME,
                    listOf(SpeechCommandGuideItem("lap", "ベストラップ", listOf("自己ベスト"))),
                ),
            ),
            spec.toSpeechCommandGuideGroups(),
        )
    }

    @Test
    fun `項目定義が空なら空を返す`() {
        assertEquals(emptyList(), ManualReadoutSimulatorSpec(emptyMap()).toSpeechCommandGuideGroups())
    }
}
