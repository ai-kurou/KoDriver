package kurou.kodriver.domain.model

data class RaceFlagsData(
    // セッション全体の進行状態。例: ガレージ、グリッド、走行中、終了後。
    val gamePhase: Int,

    // セッション全体の黄旗状態。黄旗なし/区間黄旗/フルコースイエロー等を表す。
    val yellowFlagState: Int,

    // 各セクターの旗状態。0番目=セクター1、1番目=セクター2、2番目=セクター3。
    val sectorFlags: List<Int>,

    // スタートライトの現在状態。消灯/点灯パターンの判定に使う。
    val startLight: Int,

    // 現在点灯している赤ランプの数。スタート手順の進行把握に使う。
    val numRedLights: Int,

    // プレイヤー車両個別に出ている旗指示。追い越し指示やペナルティ指示など。
    val playerFlag: Int,

    // プレイヤー車両が黄旗制御下にあるかどうか。
    val playerUnderYellow: Boolean,

    // 今周回を正式ラップとして数えるかどうかを示すフラグ。
    val playerCountLapFlag: Int,
)
