package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.util.matchManualReadoutItemKey

/** 音声認識の確定結果の候補から、Gt7Ps5 の手動読み上げ項目のキーを選ぶ。照合規則は [matchManualReadoutItemKey] を参照。 */
fun matchGt7Ps5ManualReadoutItemKey(candidates: List<String>): String? =
    matchManualReadoutItemKey(candidates, gt7Ps5ManualReadoutItemPhrases)
