package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType

// 車両接近の設定を同じDataStoreで扱うため、左右の文言もこのRepositoryにまとめる。
@Suppress("TooManyFunctions")
interface LmuWindowsVehicleApproachPreferencesRepository {
    fun observeSkipFirstLap(): Flow<Boolean>

    suspend fun saveSkipFirstLap(skip: Boolean)

    fun observeStartLeftReadoutText(): Flow<String>

    suspend fun saveStartLeftReadoutText(text: String)

    fun observeStartRightReadoutText(): Flow<String>

    suspend fun saveStartRightReadoutText(text: String)

    fun observeSustainedReadoutType(): Flow<VehicleApproachSustainedReadoutType>

    suspend fun saveSustainedReadoutType(type: VehicleApproachSustainedReadoutType)

    fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>>

    suspend fun saveEnabledState(
        key: ReadoutItemKey,
        enabled: Boolean,
    )
}
