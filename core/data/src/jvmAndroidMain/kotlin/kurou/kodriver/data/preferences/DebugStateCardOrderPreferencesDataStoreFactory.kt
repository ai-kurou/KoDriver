package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kurou.kodriver.domain.model.Simulator

/** カード順序はシミュレーターごとに別ファイルへ保存する。同じカードキーを複数のシミュレーターが使うため、共有すると互いに上書きする。 */
internal fun createDebugStateCardOrderPreferencesDataStore(
    directory: String,
    simulator: Simulator,
): DataStore<DebugStateCardOrderPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "debug_state_card_order_preferences_${simulator.id}.pb",
        serializer = DebugStateCardOrderPreferencesSerializer,
    )
