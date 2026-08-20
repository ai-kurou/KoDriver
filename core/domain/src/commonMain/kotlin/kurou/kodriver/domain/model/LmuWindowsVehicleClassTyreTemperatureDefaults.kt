package kurou.kodriver.domain.model

val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_HYPERCAR_DEFAULT = Celsius(100)
val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_DEFAULT = Celsius(90)
val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P2_ELMS_DEFAULT = Celsius(90)
val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_P3_DEFAULT = Celsius(90)
val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GTE_DEFAULT = Celsius(100)
val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_GT3_DEFAULT = Celsius(90)
val LMU_WINDOWS_VEHICLE_CLASS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_UNKNOWN_DEFAULT = Celsius(90)

/**
 * [LmuWindowsVehicleClassData.Unknown] を「未知クラス全体で共有する1件」として扱うための代表キー。
 * 実際の raw 値は無視される。
 */
const val LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY = "Unknown"

/**
 * 車両クラスごとの高温警告しきい値のデフォルト値（摂氏）。Hypercar と GTE は 100℃、
 * それ以外のクラスは 90℃ を既定値とする。
 */
fun lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
    vehicleClass: LmuWindowsVehicleClassData,
): Celsius =
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
 * 現在走行中の車両クラスに対応する高温警告しきい値を、クラス別しきい値マップから解決する。
 * [LmuWindowsVehicleClassData.Unknown] は raw 値によらず代表キーの1件を共有するため、
 * マップの直接参照ではなく代表キーへ正規化してから参照する。
 */
fun resolveLmuWindowsVehicleClassTyreTemperatureHighThresholdCelsius(
    thresholdsByVehicleClass: Map<LmuWindowsVehicleClassData, Celsius>,
    vehicleClass: LmuWindowsVehicleClassData,
): Celsius {
    val key =
        if (vehicleClass is LmuWindowsVehicleClassData.Unknown) {
            LmuWindowsVehicleClassData.Unknown(LMU_WINDOWS_VEHICLE_CLASS_UNKNOWN_KEY)
        } else {
            vehicleClass
        }
    return thresholdsByVehicleClass[key]
        ?: lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(vehicleClass)
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
