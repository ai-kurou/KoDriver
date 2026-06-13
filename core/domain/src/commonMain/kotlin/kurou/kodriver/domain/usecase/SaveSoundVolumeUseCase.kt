package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.SoundVolumeRepository

class SaveSoundVolumeUseCase(private val repository: SoundVolumeRepository) {
    suspend operator fun invoke(volume: Int) {
        require(volume in 0..100) { "volume must be between 0 and 100" }
        repository.saveVolume(volume)
    }
}
