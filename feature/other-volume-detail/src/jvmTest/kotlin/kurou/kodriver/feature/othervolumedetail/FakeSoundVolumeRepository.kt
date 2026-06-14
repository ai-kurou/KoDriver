package kurou.kodriver.feature.othervolumedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.SoundVolumeRepository

internal class FakeSoundVolumeRepository(initialVolume: Int = 100) : SoundVolumeRepository {
    private val volume = MutableStateFlow(initialVolume)

    override fun volume(): Flow<Int> = volume

    override suspend fun saveVolume(volume: Int) {
        this.volume.update { volume }
    }
}
