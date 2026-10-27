package kurou.kodriver.data

import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository

internal class CoreAutoReadoutSettingsResetter(
    private val autoReadoutPreferencesRepository: AutoReadoutPreferencesRepository,
    private val queuePreferencesRepository: QueuePreferencesRepository,
    private val readoutStartSoundEnabledPreferencesRepository: ReadoutStartSoundEnabledPreferencesRepository,
) : AutoReadoutSettingsResetter {
    override suspend fun resetToDefault() {
        autoReadoutPreferencesRepository.resetToDefault()
        queuePreferencesRepository.resetToDefault()
        readoutStartSoundEnabledPreferencesRepository.resetToDefault()
    }
}
