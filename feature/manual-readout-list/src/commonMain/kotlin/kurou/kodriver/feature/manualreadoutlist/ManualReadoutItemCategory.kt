package kurou.kodriver.feature.manualreadoutlist

import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemDefinitions
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemDefinitions
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemDefinitions
import kurou.kodriver.domain.model.ManualReadoutItemCategory

/** 各シミュレーターの手動読み上げ項目定義のカテゴリを、キーで引けるようにまとめる。 */
private val manualReadoutItemCategories: Map<String, ManualReadoutItemCategory> =
    (
        lmuWindowsManualReadoutItemDefinitions +
            aceWindowsManualReadoutItemDefinitions +
            gt7Ps5ManualReadoutItemDefinitions
    ).mapValues { it.value.category }

internal fun manualReadoutItemCategory(key: String): ManualReadoutItemCategory? = manualReadoutItemCategories[key]
