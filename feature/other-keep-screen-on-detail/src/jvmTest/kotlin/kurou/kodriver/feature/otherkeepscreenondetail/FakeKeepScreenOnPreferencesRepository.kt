package kurou.kodriver.feature.otherkeepscreenondetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository

class FakeKeepScreenOnPreferencesRepository : KeepScreenOnPreferencesRepository {
    private val state = MutableStateFlow(true)

    override fun keepScreenOn(): Flow<Boolean> = state

    override suspend fun saveKeepScreenOn(enabled: Boolean) {
        state.value = enabled
    }
}
