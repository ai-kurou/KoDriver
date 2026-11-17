package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

/** LMU のピットタイミングの読み上げ文言を永続化する。 */
@Suppress("TooManyFunctions")
public interface LmuWindowsPitTimingReadoutTextPreferencesRepository {
    public fun observeVirtualEnergyReadoutText(): Flow<String>

    public suspend fun saveVirtualEnergyReadoutText(text: String)

    public fun observeVirtualEnergyImminentReadoutText(): Flow<String>

    public suspend fun saveVirtualEnergyImminentReadoutText(text: String)

    public fun observeTyreWearReadoutText(): Flow<String>

    public fun observeFuelReadoutText(): Flow<String>

    public suspend fun saveTyreWearReadoutText(text: String)

    public suspend fun saveFuelReadoutText(text: String)

    public fun observeTyreWearImminentReadoutText(): Flow<String>

    public fun observeFuelImminentReadoutText(): Flow<String>

    public suspend fun saveTyreWearImminentReadoutText(text: String)

    public suspend fun saveFuelImminentReadoutText(text: String)
}
