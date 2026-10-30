package kurou.kodriver.data

import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutSettingsResetter

internal class CoreManualReadoutSettingsResetter(
    private val lmuWindowsManualReadoutSetPreferencesRepository: ManualReadoutSetPreferencesRepository,
    private val aceWindowsManualReadoutSetPreferencesRepository: ManualReadoutSetPreferencesRepository,
    private val gt7Ps5ManualReadoutSetPreferencesRepository: ManualReadoutSetPreferencesRepository,
    private val manualReadoutPlayButtonLayoutEnabledPreferencesRepository:
        ManualReadoutPlayButtonLayoutEnabledPreferencesRepository,
) : ManualReadoutSettingsResetter {
    override suspend fun resetToDefault() {
        lmuWindowsManualReadoutSetPreferencesRepository.resetToDefault()
        aceWindowsManualReadoutSetPreferencesRepository.resetToDefault()
        gt7Ps5ManualReadoutSetPreferencesRepository.resetToDefault()
        manualReadoutPlayButtonLayoutEnabledPreferencesRepository.resetToDefault()
    }
}
