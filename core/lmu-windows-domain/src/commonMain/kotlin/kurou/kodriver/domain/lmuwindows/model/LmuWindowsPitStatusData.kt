package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

@Serializable
public data class LmuWindowsPitStatusData(
    /** ピットレーン走行中か（リモート車両は不正確な場合あり）。 */
    val inPits: Boolean,
    val pitState: LmuWindowsPitState,
    /** 正しいガレージストール内にいるか。 */
    val inGarageStall: Boolean,
)

// LMU 共有メモリの rF2VehicleScoring.mPitState に対応する値。
// docs/lmu-windows-telemetry.md の mPitState 説明に基づく。
@Serializable
public enum class LmuWindowsPitState(
    public val rawValue: Int,
) {
    NONE(0),
    REQUESTED(1),
    ENTERING(2),
    STOPPED(3),
    EXITING(4),
    UNKNOWN(Int.MIN_VALUE),
    ;

    public companion object {
        public fun fromRaw(value: Int): LmuWindowsPitState = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}
