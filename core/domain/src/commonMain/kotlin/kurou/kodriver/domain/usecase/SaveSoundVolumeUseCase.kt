package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository

class SaveSoundVolumeUseCase(private val repository: SoundVolumePreferencesRepository) {
    suspend operator fun invoke(volume: Int) {
        require(volume in 0..100) { "volume must be between 0 and 100" }
        repository.saveVolume(volume)
    }
}
