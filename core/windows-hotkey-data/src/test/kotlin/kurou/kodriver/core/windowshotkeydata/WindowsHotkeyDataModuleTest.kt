package kurou.kodriver.core.windowshotkeydata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertSame

class WindowsHotkeyDataModuleTest {
    @Test
    fun `ウェイクワードホットキーのRepositoryを単一インスタンスで解決できる`() {
        val preferences =
            object : WakeWordHotkeyPreferencesRepository {
                override fun observeWakeWordHotkey(): Flow<WakeWordHotkey> = flowOf(WakeWordHotkey())

                override suspend fun saveWakeWordHotkey(hotkey: WakeWordHotkey) = Unit
            }
        val application =
            koinApplication {
                modules(windowsHotkeyDataModule, module { single<WakeWordHotkeyPreferencesRepository> { preferences } })
            }
        try {
            val repository = application.koin.get<WakeWordHotkeyRepository>()

            assertSame(repository, application.koin.get<WakeWordHotkeyRepository>())
        } finally {
            application.close()
        }
    }
}
