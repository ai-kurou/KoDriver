package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kurou.kodriver.domain.model.Simulator

/** 読み上げセットはシミュレーターごとに別ファイルへ保存する。 */
internal fun createManualReadoutSetPreferencesDataStore(
    directory: String,
    simulator: Simulator,
): DataStore<ManualReadoutSetPreferences> =
    preferencesDataStore(
        directory = directory,
        fileName = "manual_readout_set_preferences_${simulator.id}.pb",
        serializer = ManualReadoutSetPreferencesSerializer,
    )
