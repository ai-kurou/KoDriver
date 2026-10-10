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
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsFlagPreferencesRepository(
    repository: LmuWindowsFlagPreferencesRepository,
): LmuWindowsFlagPreferencesRepository {
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeFlagEnabledStates() } returns states
    listOf(
        LmuWindowsReadoutItemKey.Flag.BlueFlag,
        LmuWindowsReadoutItemKey.Flag.SectorYellowFlag,
        LmuWindowsReadoutItemKey.Flag.RedFlag,
    ).forEach { key ->
        listOf(true, false).forEach { enabled ->
            coEvery { repository.saveFlagEnabledState(key, enabled) } answers {
                states.update { it + (key to enabled) }
            }
        }
    }
    return repository
}

class ObserveLmuWindowsFlagEnabledStatesUseCaseTest {
    private val repository: LmuWindowsFlagPreferencesRepository = mockk()

    @Test
    fun `初期値はフラグ4種のデフォルトtrueを返す`() =
        runTest {
            val repo = createLmuWindowsFlagPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsFlagEnabledStatesUseCase(repo)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.Flag.BlueFlag to true,
                    LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to true,
                    LmuWindowsReadoutItemKey.Flag.FullCourseYellow to true,
                    LmuWindowsReadoutItemKey.Flag.RedFlag to true,
                ),
                useCase().first(),
            )
            verify(exactly = 1) { repo.observeFlagEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val repo = createLmuWindowsFlagPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsFlagEnabledStatesUseCase(repo)

            repo.saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.RedFlag, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.Flag.BlueFlag to true,
                    LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to true,
                    LmuWindowsReadoutItemKey.Flag.FullCourseYellow to true,
                    LmuWindowsReadoutItemKey.Flag.RedFlag to false,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repo.saveFlagEnabledState(LmuWindowsReadoutItemKey.Flag.RedFlag, false)
            }
            verify(exactly = 1) { repo.observeFlagEnabledStates() }
            confirmVerified(repo)
        }
}
