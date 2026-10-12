package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository

class SaveReadoutHotkeyVirtualKeyCodeUseCase(
    private val repository: ReadoutHotkeyPreferencesRepository,
) {
    suspend operator fun invoke(virtualKeyCode: Int) = repository.saveVirtualKeyCode(virtualKeyCode)
}
