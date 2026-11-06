package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey

/**
 * 音声認識の発話から手動読み上げ項目を選ぶための語句。キーは [aceWindowsManualReadoutItemDefinitions] と同じ項目キー。
 * 認識結果は表記が揺れるため、ひらがな・カタカナ・漢字の言い方を併記する。
 * 「燃料」と「燃料周回」のように、ある語句が別項目の語句の一部になっている場合がある。
 * 照合する側は部分一致した語句のうち最も長いものを持つ項目を選ぶこと。同一の語句を複数項目に持たせてはならない。
 */
val aceWindowsManualReadoutItemPhrases: Map<String, List<String>> =
    mapOf(
        AceWindowsReadoutItemKey.RemainingFuel.Root.value to
            listOf("燃料", "ねんりょう", "ガソリン", "フューエル", "燃料残量", "燃料は"),
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value to
            listOf("燃料周回", "燃料残り周回", "燃料あと何周", "燃料ラップ", "燃料何周"),
        AceWindowsReadoutItemKey.MyBestLap.Root.value to
            listOf("自己ベスト", "じこべ", "ベストラップ", "ベストタイム", "自己ベストラップ"),
        AceWindowsReadoutItemKey.TyreTemperature.Root.value to
            listOf("タイヤ温度", "タイヤの温度", "タイヤ熱", "タイヤテンプ"),
    )

fun aceWindowsManualReadoutItemPhrases(itemKey: String): List<String> =
    aceWindowsManualReadoutItemPhrases[itemKey].orEmpty()
