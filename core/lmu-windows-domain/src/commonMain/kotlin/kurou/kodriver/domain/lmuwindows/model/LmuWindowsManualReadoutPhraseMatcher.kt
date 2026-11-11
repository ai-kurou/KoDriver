package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.util.ManualReadoutPhraseMatch
import kurou.kodriver.domain.util.matchManualReadoutItemKey
import kurou.kodriver.domain.util.matchManualReadoutPhrase

/** 音声認識の確定結果の候補から、LMU の手動読み上げ項目のキーを選ぶ。照合規則は [matchManualReadoutItemKey] を参照。 */
fun matchLmuWindowsManualReadoutItemKey(candidates: List<String>): String? =
    matchManualReadoutItemKey(candidates, lmuWindowsManualReadoutItemPhrases)

/** [matchLmuWindowsManualReadoutItemKey] と同じ規則で照合し、項目のキーと決め手になった語句を返す。 */
fun matchLmuWindowsManualReadoutPhrase(candidates: List<String>): ManualReadoutPhraseMatch? =
    matchManualReadoutPhrase(candidates, lmuWindowsManualReadoutItemPhrases)
