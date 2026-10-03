package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

/** 同じ DataStore を共有する、車両接近の設定 Repository と文言 Repository の組。 */
data class LmuWindowsVehicleApproachPreferencesRepositories(
    val preferences: LmuWindowsVehicleApproachPreferencesRepository,
    val readoutText: LmuWindowsVehicleApproachReadoutTextPreferencesRepository,
)
