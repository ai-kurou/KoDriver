package kurou.kodriver.domain.model

const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_HYPERCAR_DEFAULT = 20
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P2_DEFAULT = 20
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P2_ELMS_DEFAULT = 20
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P3_DEFAULT = 20
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GTE_DEFAULT = 20
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GT3_DEFAULT = 20
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_UNKNOWN_DEFAULT = 20

/**
 * 車両クラスごとの残量警告閾値のデフォルト値（%）。現時点では全クラス共通の20%を既定値とし、
 * クラスごとの個別最適値はユーザーが `feature:lmu-windows-readout-brake-wear-detail` から調整する。
 */
fun lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(vehicleClass: LmuWindowsVehicleClassData): Int =
    when (vehicleClass) {
        LmuWindowsVehicleClassData.Hypercar -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_HYPERCAR_DEFAULT
        }

        LmuWindowsVehicleClassData.P2 -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P2_DEFAULT
        }

        LmuWindowsVehicleClassData.P2Elms -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P2_ELMS_DEFAULT
        }

        LmuWindowsVehicleClassData.P3 -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_P3_DEFAULT
        }

        LmuWindowsVehicleClassData.Gte -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GTE_DEFAULT
        }

        LmuWindowsVehicleClassData.Gt3 -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_GT3_DEFAULT
        }

        is LmuWindowsVehicleClassData.Unknown -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_UNKNOWN_DEFAULT
        }
    }

/**
 * 現在走行中の車両クラスに対応する残量警告閾値を、クラス別閾値マップから解決する。
 * [LmuWindowsVehicleClassData.Unknown] は raw 値によらず代表キーの1件を共有するため、
 * マップの直接参照ではなく代表キーへ正規化してから参照する。
 */
fun resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercent(
    thresholdsByVehicleClass: Map<LmuWindowsVehicleClassData, Int>,
    vehicleClass: LmuWindowsVehicleClassData,
): Int {
    val key =
        if (vehicleClass is LmuWindowsVehicleClassData.Unknown) {
            LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY)
        } else {
            vehicleClass
        }
    return thresholdsByVehicleClass[key]
        ?: lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(vehicleClass)
}

/**
 * 対象クラスチップのデフォルト選択値。
 */
val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_WEAR_SELECTED_DEFAULT: LmuWindowsVehicleClassData =
    LmuWindowsVehicleClassData.Hypercar
