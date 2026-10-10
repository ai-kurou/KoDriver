package kurou.kodriver.domain.model

/** 車両クラスごとのブレーキ破損厚さ（単位: meters）。この厚さを下回ると摩耗の限界とみなす。 */
const val LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_HYPERCAR_DEFAULT = 0.025f
const val LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_P2_DEFAULT = 0.025f
const val LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_P2_ELMS_DEFAULT = 0.025f
const val LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_P3_DEFAULT = 0.025f
const val LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_GTE_DEFAULT = 0.030f
const val LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_GT3_DEFAULT = 0.030f
const val LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_UNKNOWN_DEFAULT = 0.025f

/**
 * 車両クラスごとのブレーキ破損厚さのデフォルト値。残量%は「新品時の厚さ」と「破損厚さ」の間での位置で計算する。
 * LMU の REST API は破損厚さを返さないため、クラス別の固定値を使う。
 */
fun lmuWindowsVehicleClassBrakeFailureThicknessDefault(vehicleClass: LmuWindowsVehicleClassData): BrakeThicknessMeters =
    BrakeThicknessMeters(
        when (vehicleClass) {
            LmuWindowsVehicleClassData.Hypercar -> LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_HYPERCAR_DEFAULT
            LmuWindowsVehicleClassData.P2 -> LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_P2_DEFAULT
            LmuWindowsVehicleClassData.P2Elms -> LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_P2_ELMS_DEFAULT
            LmuWindowsVehicleClassData.P3 -> LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_P3_DEFAULT
            LmuWindowsVehicleClassData.Gte -> LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_GTE_DEFAULT
            LmuWindowsVehicleClassData.Gt3 -> LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_GT3_DEFAULT
            is LmuWindowsVehicleClassData.Unknown -> LMU_WINDOWS_BRAKE_FAILURE_THICKNESS_METERS_UNKNOWN_DEFAULT
        },
    )
