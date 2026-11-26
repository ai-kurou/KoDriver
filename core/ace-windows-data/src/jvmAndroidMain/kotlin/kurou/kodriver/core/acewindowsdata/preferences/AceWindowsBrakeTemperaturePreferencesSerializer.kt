package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.Serializer

internal val AceWindowsBrakeTemperaturePreferencesSerializer: Serializer<AceWindowsBrakeTemperaturePreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsBrakeTemperaturePreferences(),
        kSerializer = AceWindowsBrakeTemperaturePreferences.serializer(),
    )
