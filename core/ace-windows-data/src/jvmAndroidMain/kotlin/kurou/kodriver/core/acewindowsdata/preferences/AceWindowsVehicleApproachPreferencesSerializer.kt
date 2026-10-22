package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsVehicleApproachPreferencesSerializer: Serializer<AceWindowsVehicleApproachPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsVehicleApproachPreferences(),
        kSerializer = AceWindowsVehicleApproachPreferences.serializer(),
    )
