package kurou.kodriver.domain.lmuwindows.model

/**
 * Hypercar と LMGT3（GT3）のみが Virtual Energy を使い、それ以外は燃料を使う。
 * 実機・ゲーム仕様で確認済みの対応関係であり、ゲーム更新でクラスが増えた場合は手動更新が必要。
 */
fun lmuWindowsVehicleClassEnergySource(vehicleClass: LmuWindowsVehicleClassData): LmuWindowsEnergySource =
    when (vehicleClass) {
        LmuWindowsVehicleClassData.Hypercar -> LmuWindowsEnergySource.VirtualEnergy
        LmuWindowsVehicleClassData.P2 -> LmuWindowsEnergySource.Fuel
        LmuWindowsVehicleClassData.P2Elms -> LmuWindowsEnergySource.Fuel
        LmuWindowsVehicleClassData.P3 -> LmuWindowsEnergySource.Fuel
        LmuWindowsVehicleClassData.Gte -> LmuWindowsEnergySource.Fuel
        LmuWindowsVehicleClassData.Gt3 -> LmuWindowsEnergySource.VirtualEnergy
        is LmuWindowsVehicleClassData.Unknown -> LmuWindowsEnergySource.Fuel
    }
