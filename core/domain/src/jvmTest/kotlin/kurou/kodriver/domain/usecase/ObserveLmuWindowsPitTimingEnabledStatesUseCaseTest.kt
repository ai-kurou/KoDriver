@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsPitTimingPreferencesRepository(
    repository: LmuWindowsPitTimingPreferencesRepository,
): LmuWindowsPitTimingPreferencesRepository {
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns enabledStates
    listOf(
        ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy,
        ReadoutItemKey.LmuWindows.PitTiming.TyreWear,
        ReadoutItemKey.LmuWindows.PitTiming.Root,
    ).forEach { key ->
        listOf(true, false).forEach { enabled ->
            coEvery { repository.saveEnabledState(key, enabled) } answers {
                enabledStates.update { it + (key to enabled) }
            }
        }
    }
    return repository
}

class ObserveLmuWindowsPitTimingEnabledStatesUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk()

    @Test
    fun `初期値はVirtualEnergyとTyreWearのデフォルトtrueを返す`() =
        runTest {
            val repo = createLmuWindowsPitTimingPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsPitTimingEnabledStatesUseCase(repo)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy to true,
                    ReadoutItemKey.LmuWindows.PitTiming.TyreWear to true,
                )
            assertEquals(expected, useCase().first())
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val repo = createLmuWindowsPitTimingPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsPitTimingEnabledStatesUseCase(repo)

            repo.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, false)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy to false,
                    ReadoutItemKey.LmuWindows.PitTiming.TyreWear to true,
                )
            assertEquals(expected, useCase().first())
            coVerify(exactly = 1) {
                repo.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() =
        runTest {
            val repo = createLmuWindowsPitTimingPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsPitTimingEnabledStatesUseCase(repo)

            repo.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy to true,
                    ReadoutItemKey.LmuWindows.PitTiming.TyreWear to true,
                    ReadoutItemKey.LmuWindows.PitTiming.Root to false,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repo.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.Root, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }
}
