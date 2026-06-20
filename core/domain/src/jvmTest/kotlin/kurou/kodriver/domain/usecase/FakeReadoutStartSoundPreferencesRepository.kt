package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository

internal class FakeReadoutStartSoundPreferencesRepository(
    initialType: ReadoutStartSoundType = ReadoutStartSoundType.ELECTRONIC_NOISE,
) : ReadoutStartSoundPreferencesRepository {
    private val state = MutableStateFlow(initialType)

    override fun observeType(): Flow<ReadoutStartSoundType> = state

    override suspend fun saveType(type: ReadoutStartSoundType) {
        state.update { type }
    }
}
