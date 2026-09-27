package kurou.kodriver.domain.model

const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_HYPERCAR_DEFAULT = 700
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_DEFAULT = 700
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_ELMS_DEFAULT = 700
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P3_DEFAULT = 700
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT = 700
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GT3_DEFAULT = 700
const val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT = 700

/**
 * 車両クラスごとの過熱警告閾値のデフォルト値（摂氏）。現時点では全クラス共通の700℃を既定値とし、
 * クラスごとの個別最適値はユーザーが `feature:lmu-windows-readout-brake-temperature-detail` から調整する。
 */
fun lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(vehicleClass: LmuWindowsVehicleClassData): Int =
    when (vehicleClass) {
        LmuWindowsVehicleClassData.Hypercar -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_HYPERCAR_DEFAULT
        }

        LmuWindowsVehicleClassData.P2 -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_DEFAULT
        }

        LmuWindowsVehicleClassData.P2Elms -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_ELMS_DEFAULT
        }

        LmuWindowsVehicleClassData.P3 -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P3_DEFAULT
        }

        LmuWindowsVehicleClassData.Gte -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT
        }

        LmuWindowsVehicleClassData.Gt3 -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GT3_DEFAULT
        }

        is LmuWindowsVehicleClassData.Unknown -> {
            LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT
        }
    }

/**
 * 現在走行中の車両クラスに対応する過熱警告閾値を、クラス別閾値マップから解決する。
 * [LmuWindowsVehicleClassData.Unknown] は raw 値によらず代表キーの1件を共有するため、
 * マップの直接参照ではなく代表キーへ正規化してから参照する。
 */
fun resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsius(
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
        ?: lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(vehicleClass)
}

/**
 * 対象クラスチップのデフォルト選択値。
 */
val LMU_WINDOWS_VEHICLE_CLASS_BRAKE_TEMPERATURE_SELECTED_DEFAULT: LmuWindowsVehicleClassData =
    LmuWindowsVehicleClassData.Hypercar
