package kurou.kodriver.data.preferences

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kurou.kodriver.domain.captureError

/**
 * Preferences 形式の DataStore（`Context.preferencesDataStore`）の破損時ハンドラー。
 *
 * 破損（`CorruptionException`）を [captureError] で記録したうえで、ファイルを空の Preferences（＝既定値）で置き換える。
 * Preferences 形式は独自 Serializer を持たないため、Serializer 側の記録（`protoBufPreferencesSerializer`）に相当する記録をここで行う。
 */
internal val androidPreferencesCorruptionHandler: ReplaceFileCorruptionHandler<Preferences> =
    ReplaceFileCorruptionHandler { e ->
        captureError(e)
        emptyPreferences()
    }
