package kurou.kodriver.data

import kurou.kodriver.data.datasource.createProximityThresholdsDataStore
import kurou.kodriver.data.repository.ProximityThresholdsRepositoryImpl
import kurou.kodriver.domain.repository.ProximityThresholdsRepository

fun createProximityThresholdsRepository(directory: String): ProximityThresholdsRepository =
    ProximityThresholdsRepositoryImpl(createProximityThresholdsDataStore(directory))
