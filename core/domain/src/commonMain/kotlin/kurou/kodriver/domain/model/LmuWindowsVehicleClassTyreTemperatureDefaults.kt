package kurou.kodriver.domain.model

const val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_HYPERCAR_DEFAULT = 95
const val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_DEFAULT = 95
const val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_ELMS_DEFAULT = 95
const val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P3_DEFAULT = 95
const val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT = 95
const val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GT3_DEFAULT = 95
const val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT = 95

/**
 * [LmuWindowsVehicleClassData.Unknown] を「未知クラス全体で共有する1件」として扱うための代表キー。
 * 実際の raw 値は無視される。
 */
const val LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY = "Unknown"

/**
 * 車両クラスごとの高温警告しきい値のデフォルト値（摂氏）。実測に基づくクラス別の適正値が
 * 判明するまでは、いずれも既存の全クラス共通しきい値と同じ値を暫定的に使用する。
 */
fun lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass: LmuWindowsVehicleClassData): Int =
    when (vehicleClass) {
        LmuWindowsVehicleClassData.Hypercar -> {
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_HYPERCAR_DEFAULT
        }

        LmuWindowsVehicleClassData.P2 -> {
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_DEFAULT
        }

        LmuWindowsVehicleClassData.P2Elms -> {
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_ELMS_DEFAULT
        }

        LmuWindowsVehicleClassData.P3 -> {
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P3_DEFAULT
        }

        LmuWindowsVehicleClassData.Gte -> {
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT
        }

        LmuWindowsVehicleClassData.Gt3 -> {
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GT3_DEFAULT
        }

        is LmuWindowsVehicleClassData.Unknown -> {
            LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT
        }
    }

/**
 * 対象クラスチップのデフォルト選択値。
 */
val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_SELECTED_DEFAULT: LmuWindowsVehicleClassData =
    LmuWindowsVehicleClassData.Hypercar

/**
 * 高温警告しきい値をクラスごとに保存・列挙する際に対象とする全車両クラス
 * （Unknown は代表インスタンスを1件のみ含む）。
 */
val lmuWindowsAllVehicleClasses: List<LmuWindowsVehicleClassData> =
    listOf(
        LmuWindowsVehicleClassData.Hypercar,
        LmuWindowsVehicleClassData.P2,
        LmuWindowsVehicleClassData.P2Elms,
        LmuWindowsVehicleClassData.P3,
        LmuWindowsVehicleClassData.Gte,
        LmuWindowsVehicleClassData.Gt3,
        LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY),
    )
