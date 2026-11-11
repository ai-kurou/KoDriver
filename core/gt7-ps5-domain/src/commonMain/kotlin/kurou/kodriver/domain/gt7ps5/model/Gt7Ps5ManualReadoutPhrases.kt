package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey

/**
 * 音声認識の発話から手動読み上げ項目を選ぶための語句。キーは [gt7Ps5ManualReadoutItemDefinitions] と同じ項目キー。
 * 認識結果は表記が揺れるため、ひらがな・カタカナ・漢字の言い方を併記する。
 * 照合する側は部分一致した語句のうち最も長いものを持つ項目を選ぶこと。同一の語句を複数項目に持たせてはならない。
 */
val gt7Ps5ManualReadoutItemPhrases: Map<String, List<String>> =
    mapOf(
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value to
            listOf("ガソリン", "燃料残量", "燃料の残量", "ガソリン残量"),
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value to
            listOf("燃料周回", "燃料残り周回", "燃料あと何周", "燃料ラップ", "燃料何周", "燃料はあと何周", "燃料は何周"),
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value to
            listOf("タイヤ温度", "タイヤの温度"),
        Gt7Ps5ReadoutItemKey.MyBestLap.Root.value to
            listOf("自己ベスト", "ベストラップ", "ベストタイム", "自己ベストラップ"),
    )

fun gt7Ps5ManualReadoutItemPhrases(itemKey: String): List<String> = gt7Ps5ManualReadoutItemPhrases[itemKey].orEmpty()
