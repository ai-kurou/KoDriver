package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository

public class SaveReadoutHotkeyVirtualKeyCodeUseCase(
    private val repository: ReadoutHotkeyPreferencesRepository,
) {
    public suspend operator fun invoke(virtualKeyCode: Int): Unit = repository.saveVirtualKeyCode(virtualKeyCode)
}
