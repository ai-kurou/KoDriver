package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.SOUND_VOLUME_MAX
import kurou.kodriver.domain.model.SOUND_VOLUME_MIN
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository

class SaveSoundVolumeUseCase(private val repository: SoundVolumePreferencesRepository) {
    suspend operator fun invoke(volume: Int) {
        require(volume in SOUND_VOLUME_MIN..SOUND_VOLUME_MAX) { "volume must be between 0 and 100" }
        repository.saveVolume(volume)
    }
}
