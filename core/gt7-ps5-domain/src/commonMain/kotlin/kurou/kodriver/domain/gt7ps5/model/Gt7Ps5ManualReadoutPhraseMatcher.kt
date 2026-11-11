package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.util.ManualReadoutPhraseMatch
import kurou.kodriver.domain.util.matchManualReadoutItemKey
import kurou.kodriver.domain.util.matchManualReadoutPhrase

/** 音声認識の確定結果の候補から、Gt7Ps5 の手動読み上げ項目のキーを選ぶ。照合規則は [matchManualReadoutItemKey] を参照。 */
fun matchGt7Ps5ManualReadoutItemKey(candidates: List<String>): String? =
    matchManualReadoutItemKey(candidates, gt7Ps5ManualReadoutItemPhrases)

/** [matchGt7Ps5ManualReadoutItemKey] と同じ規則で照合し、項目のキーと決め手になった語句を返す。 */
fun matchGt7Ps5ManualReadoutPhrase(candidates: List<String>): ManualReadoutPhraseMatch? =
    matchManualReadoutPhrase(candidates, gt7Ps5ManualReadoutItemPhrases)
