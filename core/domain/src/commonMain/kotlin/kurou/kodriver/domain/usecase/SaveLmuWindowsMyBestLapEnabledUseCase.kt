package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository

class SaveLmuWindowsMyBestLapEnabledUseCase(
    private val repository: LmuWindowsMyBestLapEnabledRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.saveEnabled(enabled)
}
