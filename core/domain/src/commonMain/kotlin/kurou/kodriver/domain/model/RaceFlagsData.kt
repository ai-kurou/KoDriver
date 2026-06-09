package kurou.kodriver.domain.model

data class RaceFlagsData(
    // セッション全体の進行状態。例: ガレージ、グリッド、走行中、終了後。
    val gamePhase: SessionPhase,

    // セッション全体の黄旗状態。黄旗なし/FCY準備中/ピットクローズ等を表す。
    val yellowFlagState: SessionYellowFlagState,

    // 各セクターの旗状態。0番目=セクター1、1番目=セクター2、2番目=セクター3。
    val sectorFlags: List<SectorFlagState>,

    // スタートライトの現在状態。消灯/点灯パターンの判定に使う。
    val startLight: Int,

    // 現在点灯している赤ランプの数。スタート手順の進行把握に使う。
    val numRedLights: Int,

    // プレイヤー車両個別に出ている旗指示。追い越し指示やペナルティ指示など。
    val playerFlag: PrimaryFlag,

    // プレイヤー車両が黄旗制御下にあるかどうか。
    val playerUnderYellow: Boolean,

    // 今周回を正式ラップとして数えるかどうかを示すフラグ。
    val playerCountLapFlag: CountLapFlag,
)

// rFactor/rF2 系 shared memory の GamePhase をベースにしたセッション状態。
enum class SessionPhase(val rawValue: Int) {
    GARAGE(0),
    WARM_UP(1),
    GRID_WALK(2),
    FORMATION(3),
    COUNTDOWN(4),
    GREEN_FLAG(5),
    FULL_COURSE_YELLOW(6),
    SESSION_STOPPED(7),
    SESSION_OVER(8),
    PAUSED_OR_HEARTBEAT(9),
    UNDER_YELLOW_FLAG(10),
    UNDER_BLUE_FLAG(11),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): SessionPhase = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}

// rFactor/rF2 系 shared memory の YellowFlagState をベースにしたセッション黄旗状態。
enum class SessionYellowFlagState(val rawValue: Int) {
    INVALID(-1),
    NONE(0),
    PENDING(1),
    PIT_CLOSED(2),
    PIT_LEAD_LAP(3),
    PIT_OPEN(4),
    LAST_LAP(5),
    RESUME(6),
    RACE_HALT(7),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): SessionYellowFlagState = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}

// セクター単位の旗状態。通常はクリアか黄旗の二値で扱う。
enum class SectorFlagState(val rawValue: Int) {
    CLEAR(0),
    YELLOW(1),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): SectorFlagState = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}

// 車両個別の旗指示。mFlag の代表的な意味をコード上で読めるようにしたもの。
enum class PrimaryFlag(val rawValue: Int) {
    NONE(0),
    BLUE(1),
    YELLOW(2),
    BLACK(3),
    WHITE(4),
    GREEN(5),
    CHECKERED(6),
    PENALTY(7),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): PrimaryFlag = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}

// その周回を正式ラップとして数えるかどうかに関するフラグ。
enum class CountLapFlag(val rawValue: Int) {
    DO_NOT_COUNT(0),
    COUNT(1),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): CountLapFlag = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}
