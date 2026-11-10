package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import kurou.kodriver.domain.repository.SpeechCommandGuideRepository

/**
 * [ReadoutItemKeyRegistry] に登録された手動読み上げの項目定義と語句から、音声タブの案内を組み立てる。
 * 語句は音声認識の照合（`match*ManualReadoutItemKey`）と同じ定義なので、一覧に出る言い方と実際に反応する言い方は一致する。
 */
class RegistrySpeechCommandGuideRepository : SpeechCommandGuideRepository {
    override fun guideGroups(simulator: Simulator): List<SpeechCommandGuideGroup> {
        val spec = ReadoutItemKeyRegistry.manualReadoutSpecOf(simulator.id) ?: return emptyList()
        val items =
            spec.itemDefinitions.values.mapNotNull { definition ->
                val phrases = spec.itemPhrases[definition.key].orEmpty()
                if (phrases.isEmpty()) return@mapNotNull null
                definition.category to SpeechCommandGuideItem(definition.key, definition.name, phrases)
            }
        return ManualReadoutItemCategory.entries.mapNotNull { category ->
            val categoryItems = items.filter { it.first == category }.map { it.second }
            if (categoryItems.isEmpty()) null else SpeechCommandGuideGroup(category, categoryItems)
        }
    }
}
