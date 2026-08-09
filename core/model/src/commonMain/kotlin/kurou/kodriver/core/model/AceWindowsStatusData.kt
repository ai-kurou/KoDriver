package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AceWindowsStatusData(
    val status: AceWindowsStatusType,
    val carLocation: AceWindowsCarLocation = AceWindowsCarLocation.UNASSIGNED,
)

// ACE shared memory の graphics.status に対応する ACEVO_STATUS の値。
// docs/ace-windows-telemetry.md の ACEVO_STATUS 一覧に基づく。
@Serializable
enum class AceWindowsStatusType(
    val rawValue: Int,
) {
    OFF(0),
    REPLAY(1),
    LIVE(2),
    PAUSE(3),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): AceWindowsStatusType = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}

// ACE shared memory の graphics.car_location に対応する ACEVO_CAR_LOCATION の値。
// status(LIVE)だけではガレージ・ピット・コース上の区別ができないため、これと組み合わせて
// 「実際にコース上を走行中か」を判定する用途を想定している。
// docs/ace-windows-telemetry.md の ACEVO_CAR_LOCATION 一覧に基づく。
@Serializable
enum class AceWindowsCarLocation(
    val rawValue: Int,
) {
    UNASSIGNED(0),
    PITLANE(1),
    PITENTRY(2),
    PITEXIT(3),
    TRACK(4),
    UNKNOWN(Int.MIN_VALUE),
    ;

    companion object {
        fun fromRaw(value: Int): AceWindowsCarLocation = entries.firstOrNull { it.rawValue == value } ?: UNKNOWN
    }
}
