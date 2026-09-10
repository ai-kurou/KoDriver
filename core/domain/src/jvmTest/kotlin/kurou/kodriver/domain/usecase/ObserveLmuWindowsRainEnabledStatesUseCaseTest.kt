@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsRainPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsRainPreferencesRepository(
    repository: LmuWindowsRainPreferencesRepository,
): LmuWindowsRainPreferencesRepository {
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeRainEnabledStates() } returns states
    listOf(true, false).forEach { enabled ->
        coEvery { repository.saveRainEnabledState(ReadoutItemKey.LmuWindows.Rain.Start, enabled) } answers {
            states.update { it + (ReadoutItemKey.LmuWindows.Rain.Start to enabled) }
        }
    }
    return repository
}

class ObserveLmuWindowsRainEnabledStatesUseCaseTest {
    @MockK
    private lateinit var repository: LmuWindowsRainPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値は降り始めの読み上げのデフォルトtrueを返す`() =
        runTest {
            val repo = createLmuWindowsRainPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsRainEnabledStatesUseCase(repo)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Rain.Start to true),
                useCase().first(),
            )
            verify(exactly = 1) { repo.observeRainEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val repo = createLmuWindowsRainPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsRainEnabledStatesUseCase(repo)

            repo.saveRainEnabledState(ReadoutItemKey.LmuWindows.Rain.Start, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Rain.Start to false),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repo.saveRainEnabledState(ReadoutItemKey.LmuWindows.Rain.Start, false)
            }
            verify(exactly = 1) { repo.observeRainEnabledStates() }
            confirmVerified(repo)
        }
}
