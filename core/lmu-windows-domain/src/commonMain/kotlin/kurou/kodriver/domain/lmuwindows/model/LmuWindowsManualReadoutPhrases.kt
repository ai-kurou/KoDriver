package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey

/**
 * 音声認識の発話から手動読み上げ項目を選ぶための語句。キーは [lmuWindowsManualReadoutItemDefinitions] と同じ項目キー。
 * 認識結果は表記が揺れるため、ひらがな・カタカナ・漢字の言い方を併記する。
 * 「燃料」と「燃料周回」のように、ある語句が別項目の語句の一部になっている場合がある。
 * 照合する側は部分一致した語句のうち最も長いものを持つ項目を選ぶこと。同一の語句を複数項目に持たせてはならない。
 */
val lmuWindowsManualReadoutItemPhrases: Map<String, List<String>> =
    mapOf(
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to
            listOf("バーチャルエネルギー", "ばーちゃるえねるぎー", "VE", "ブイイー", "エネルギー残量", "エネルギー"),
        LmuWindowsReadoutItemKey.RemainingFuel.Root.value to
            listOf("燃料", "ねんりょう", "ガソリン", "フューエル", "燃料残量", "燃料は"),
        LmuWindowsReadoutItemKey.TyreWear.Root.value to
            listOf(
                "タイヤ摩耗",
                "タイヤ磨耗",
                "タイヤの摩耗",
                "タイヤウェア",
                "タイヤの減り",
                "タイヤ残量",
                // 「摩耗」は音声認識で拾われにくいため、残り・減り系の言い方も受け付ける。
                "タイヤ残り",
                "タイヤの残り",
                "タイヤの残量",
                "タイヤ減り",
                "タイヤのへり",
                "タイヤすり減り",
                "タイヤの減り具合",
                "タイヤのこり",
                "タイヤのこりりょう",
                "タイヤへり",
            ),
        LmuWindowsReadoutItemKey.BrakeWear.Root.value to
            listOf(
                "ブレーキ残量",
                "ブレーキ摩耗",
                "ブレーキ磨耗",
                "ブレーキの摩耗",
                "ブレーキウェア",
                "ブレーキの減り",
                // 「摩耗」は音声認識で拾われにくいため、残り・減り系の言い方も受け付ける。
                "ブレーキ残り",
                "ブレーキの残り",
                "ブレーキの残量",
                "ブレーキ減り",
                "ブレーキのへり",
                "ブレーキすり減り",
                "ブレーキの減り具合",
                "ブレーキのこり",
                "ブレーキのこりりょう",
                "ブレーキへり",
            ),
        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value to
            listOf("ブレーキ温度", "ブレーキの温度", "ブレーキ熱", "ブレーキテンプ"),
        LmuWindowsReadoutItemKey.TyreTemperature.Root.value to
            listOf("タイヤ温度", "タイヤの温度", "タイヤ熱", "タイヤテンプ"),
        LmuWindowsReadoutItemKey.MyBestLap.Root.value to
            listOf("自己ベスト", "じこべ", "ベストラップ", "ベストタイム", "自己ベストラップ"),
        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value to
            listOf("バーチャルエネルギー周回", "エネルギー周回", "エネルギー残り周回", "VE周回", "VEラップ"),
        LmuWindowsReadoutItemKey.PitTiming.TyreWear.value to
            listOf(
                "タイヤ周回",
                "タイヤ残り周回",
                "タイヤあと何周",
                "タイヤ摩耗周回",
                "タイヤラップ",
                // タイヤ摩耗の「残り」系の語句を含む言い方は、より長いこちらの語句で周回数項目に解決する。
                "タイヤの残り周回",
                "タイヤのこり周回",
                "タイヤ残りあと何周",
                "タイヤの残りあと何周",
                "タイヤのこりあと何周",
                "タイヤ残り何周",
                "タイヤの残り何周",
                "タイヤ残りは何周",
                "タイヤの残りは何周",
                "タイヤ残りラップ",
                "タイヤの残りラップ",
            ),
        LmuWindowsReadoutItemKey.RemainingFuel.Laps.value to
            listOf("燃料周回", "燃料残り周回", "燃料あと何周", "燃料ラップ", "燃料何周", "燃料はあと何周", "燃料は何周"),
        LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value to
            listOf("前の車", "前方", "前とのギャップ", "前のギャップ", "前の差", "前の車との差"),
        LmuWindowsReadoutItemKey.ClassCarGap.Behind.value to
            listOf("後ろの車", "後方", "後ろとのギャップ", "後ろのギャップ", "後ろの差", "後ろの車との差"),
    )

fun lmuWindowsManualReadoutItemPhrases(itemKey: String): List<String> =
    lmuWindowsManualReadoutItemPhrases[itemKey].orEmpty()
