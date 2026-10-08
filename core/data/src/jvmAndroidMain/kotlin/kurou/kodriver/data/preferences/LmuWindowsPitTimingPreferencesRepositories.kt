package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository

/** 同じ DataStore を共有する、ピットタイミングの設定 Repository と文言 Repository の組。 */
data class LmuWindowsPitTimingPreferencesRepositories(
    val preferences: LmuWindowsPitTimingPreferencesRepository,
    val readoutText: LmuWindowsPitTimingReadoutTextPreferencesRepository,
)
