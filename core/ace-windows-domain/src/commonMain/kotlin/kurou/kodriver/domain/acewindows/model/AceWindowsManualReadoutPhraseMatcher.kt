package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.util.ManualReadoutPhraseMatch
import kurou.kodriver.domain.util.matchManualReadoutPhrase

/** 音声認識の確定結果の候補から、ACE の手動読み上げ項目のキーと決め手になった語句を選ぶ。照合規則は [matchManualReadoutPhrase] を参照。 */
fun matchAceWindowsManualReadoutPhrase(candidates: List<String>): ManualReadoutPhraseMatch? =
    matchManualReadoutPhrase(candidates, aceWindowsManualReadoutItemPhrases)
