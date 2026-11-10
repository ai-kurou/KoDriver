package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MainWindowBounds

interface MainWindowBoundsPreferencesRepository {
    fun observeMainWindowBounds(): Flow<MainWindowBounds>

    suspend fun saveMainWindowBounds(bounds: MainWindowBounds)
}
