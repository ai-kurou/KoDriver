package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry

/** 登録済みの全シミュレーターの手動読み上げ項目定義から、キーでカテゴリを引く。 */
internal fun manualReadoutItemCategory(key: String): ManualReadoutItemCategory? =
    ReadoutItemKeyRegistry.manualReadoutSpecs.firstNotNullOfOrNull { (_, spec) -> spec.itemDefinitions[key]?.category }
