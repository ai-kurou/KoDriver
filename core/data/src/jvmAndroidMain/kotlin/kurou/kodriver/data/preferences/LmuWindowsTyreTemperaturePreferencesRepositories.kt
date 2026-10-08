package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository

/** 同じ DataStore を共有する、タイヤ温度の設定 Repository と文言 Repository の組。 */
data class LmuWindowsTyreTemperaturePreferencesRepositories(
    val preferences: LmuWindowsTyreTemperaturePreferencesRepository,
    val readoutText: LmuWindowsTyreTemperatureReadoutTextPreferencesRepository,
)
