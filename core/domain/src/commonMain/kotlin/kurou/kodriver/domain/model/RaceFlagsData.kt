package kurou.kodriver.domain.model

import kotlinx.serialization.Serializable

@Serializable
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

// LMU shared memory の ScoringInfo.mGamePhase に対応する値。
// pyLMUSharedMemory lmu_enum.py の LMUGamePhase に基づく（値 0〜9）。
@Serializable
enum class SessionPhase(val rawValue: Int) {
    GARAGE(0),
    WARM_UP(1),
    GRID_WALK(2),
    FORMATION(3),
    COUNTDOWN(4),
    GREEN_FLAG(5),
    FULL_COURSE_YELLOW(6),
    RED_FLAG(7),
    SESSION_OVER(8),
    PAUSED_OR_HEARTBEAT(9),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): SessionPhase = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}

// rFactor/rF2 系 shared memory の ScoringInfo.mYellowFlagState に対応する値。
@Serializable
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

// shared memory の ScoringInfo.mSectorFlag[3] に対応する値。
// 通常は 0=クリア、1=黄旗として使われるため、その前提で enum 化している。
@Serializable
enum class SectorFlagState(val rawValue: Int) {
    CLEAR(0),
    YELLOW(1),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): SectorFlagState = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}

// LMU shared memory の VehicleScoringInfo.mFlag に対応する値。
// pyLMUSharedMemory lmu_enum.py の LMUPrimaryFlag に基づく（Green=0, Blue=6 の2値のみ）。
@Serializable
enum class PrimaryFlag(val rawValue: Int) {
    GREEN(0),
    BLUE(6),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): PrimaryFlag = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}

// LMU shared memory の VehicleScoringInfo.mCountLapFlag に対応する値。
// pyLMUSharedMemory lmu_enum.py の LMUCountLapFlag に基づく。
@Serializable
enum class CountLapFlag(val rawValue: Int) {
    DO_NOT_COUNT_LAP_OR_TIME(0),
    COUNT_LAP_BUT_NOT_TIME(1),
    COUNT_LAP_AND_TIME(2),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): CountLapFlag = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}
