package kurou.kodriver.data

import kurou.kodriver.data.datasource.createProximityThresholdsDataStore
import kurou.kodriver.data.repository.ProximityThresholdsRepositoryImpl
import kurou.kodriver.domain.repository.ProximityThresholdsRepository

internal fun createProximityThresholdsRepository(directory: String): ProximityThresholdsRepository =
    ProximityThresholdsRepositoryImpl(createProximityThresholdsDataStore(directory))
