package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

internal class FakeLmuWindowsTyreTemperaturePreferencesRepository(
    initialHighThreshold: Int = 90,
) : LmuWindowsTyreTemperaturePreferencesRepository {
    private val _highThreshold = MutableStateFlow(initialHighThreshold)

    override fun observeHighThresholdCelsius(): Flow<Int> = _highThreshold.asStateFlow()

    override suspend fun saveHighThresholdCelsius(celsius: Int) {
        _highThreshold.update { celsius }
    }
}
