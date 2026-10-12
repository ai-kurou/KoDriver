package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.HotkeyRepository

class SaveReadoutHotkeyVirtualKeyCodeUseCase(
    private val repository: HotkeyRepository,
) {
    suspend operator fun invoke(virtualKeyCode: Int) = repository.saveVirtualKeyCode(virtualKeyCode)
}
