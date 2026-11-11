package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey

/**
 * 音声認識の発話から手動読み上げ項目を選ぶための語句。キーは [aceWindowsManualReadoutItemDefinitions] と同じ項目キー。
 * 認識結果は表記が揺れるため、ひらがな・カタカナ・漢字の言い方を併記する。
 * 照合する側は部分一致した語句のうち最も長いものを持つ項目を選ぶこと。同一の語句を複数項目に持たせてはならない。
 */
val aceWindowsManualReadoutItemPhrases: Map<String, List<String>> =
    mapOf(
        AceWindowsReadoutItemKey.RemainingFuel.Root.value to
            listOf("ガソリン", "燃料残量", "燃料の残量", "ガソリン残量"),
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value to
            listOf("燃料周回", "燃料残り周回", "燃料あと何周", "燃料ラップ", "燃料何周", "燃料はあと何周", "燃料は何周"),
        AceWindowsReadoutItemKey.MyBestLap.Root.value to
            listOf("自己ベスト", "ベストラップ", "ベストタイム", "自己ベストラップ"),
        AceWindowsReadoutItemKey.TyreTemperature.Root.value to
            listOf("タイヤ温度", "タイヤの温度", "タイヤ熱"),
    )

fun aceWindowsManualReadoutItemPhrases(itemKey: String): List<String> =
    aceWindowsManualReadoutItemPhrases[itemKey].orEmpty()
