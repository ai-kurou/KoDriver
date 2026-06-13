package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.SoundVolumeRepository

internal class FakeSoundVolumeRepository(initial: Int = 100) : SoundVolumeRepository {
    private val flow = MutableStateFlow(initial)

    override fun volume(): Flow<Int> = flow
    override suspend fun saveVolume(volume: Int) { flow.value = volume }
}
