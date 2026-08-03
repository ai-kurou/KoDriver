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
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsFlagPreferencesRepository(
    repository: LmuWindowsFlagPreferencesRepository,
): LmuWindowsFlagPreferencesRepository {
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeFlagEnabledStates() } returns states
    listOf(
        ReadoutItemKey.LmuWindows.Flag.BlueFlag,
        ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag,
        ReadoutItemKey.LmuWindows.Flag.RedFlag,
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
    @MockK
    private lateinit var repository: LmuWindowsFlagPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値はフラグ4種のデフォルトtrueを返す`() =
        runTest {
            val repo = createLmuWindowsFlagPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsFlagEnabledStatesUseCase(repo)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                    ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
                    ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
                    ReadoutItemKey.LmuWindows.Flag.RedFlag to true,
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

            repo.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.Flag.BlueFlag to true,
                    ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag to true,
                    ReadoutItemKey.LmuWindows.Flag.FullCourseYellow to true,
                    ReadoutItemKey.LmuWindows.Flag.RedFlag to false,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repo.saveFlagEnabledState(ReadoutItemKey.LmuWindows.Flag.RedFlag, false)
            }
            verify(exactly = 1) { repo.observeFlagEnabledStates() }
            confirmVerified(repo)
        }
}
