package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.util.ManualReadoutPhraseMatch
import kurou.kodriver.domain.util.matchManualReadoutItemKey
import kurou.kodriver.domain.util.matchManualReadoutPhrase

/** 音声認識の確定結果の候補から、ACE の手動読み上げ項目のキーを選ぶ。照合規則は [matchManualReadoutItemKey] を参照。 */
fun matchAceWindowsManualReadoutItemKey(candidates: List<String>): String? =
    matchManualReadoutItemKey(candidates, aceWindowsManualReadoutItemPhrases)

/** [matchAceWindowsManualReadoutItemKey] と同じ規則で照合し、項目のキーと決め手になった語句を返す。 */
fun matchAceWindowsManualReadoutPhrase(candidates: List<String>): ManualReadoutPhraseMatch? =
    matchManualReadoutPhrase(candidates, aceWindowsManualReadoutItemPhrases)
