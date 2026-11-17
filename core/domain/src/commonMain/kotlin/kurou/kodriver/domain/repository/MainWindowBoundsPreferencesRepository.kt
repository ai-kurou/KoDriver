package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.MainWindowBounds

public interface MainWindowBoundsPreferencesRepository {
    public fun observeMainWindowBounds(): Flow<MainWindowBounds>

    public suspend fun saveMainWindowBounds(bounds: MainWindowBounds)
}
