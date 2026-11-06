package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.util.matchManualReadoutItemKey

/** 音声認識の確定結果の候補から、ACE の手動読み上げ項目のキーを選ぶ。照合規則は [matchManualReadoutItemKey] を参照。 */
fun matchAceWindowsManualReadoutItemKey(candidates: List<String>): String? =
    matchManualReadoutItemKey(candidates, aceWindowsManualReadoutItemPhrases)
