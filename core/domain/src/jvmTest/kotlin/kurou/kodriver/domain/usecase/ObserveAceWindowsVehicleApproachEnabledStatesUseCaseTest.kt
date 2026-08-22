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
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createAceWindowsVehicleApproachPreferencesRepository(
    repository: AceWindowsVehicleApproachPreferencesRepository,
): AceWindowsVehicleApproachPreferencesRepository {
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns enabledStates
    listOf(
        ReadoutItemKey.AceWindows.VehicleApproach.StartReadout,
        ReadoutItemKey.AceWindows.VehicleApproach.Root,
    ).forEach { key ->
        listOf(true, false).forEach { enabled ->
            coEvery { repository.saveEnabledState(key, enabled) } answers {
                enabledStates.update { it + (key to enabled) }
            }
        }
    }
    return repository
}

class ObserveAceWindowsVehicleApproachEnabledStatesUseCaseTest {
    @MockK
    private lateinit var repository: AceWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値はStartReadoutのデフォルトtrueを返す`() =
        runTest {
            val repo = createAceWindowsVehicleApproachPreferencesRepository(repository)
            val useCase = ObserveAceWindowsVehicleApproachEnabledStatesUseCase(repo)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to true,
                )
            assertEquals(expected, useCase().first())
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val repo = createAceWindowsVehicleApproachPreferencesRepository(repository)
            val useCase = ObserveAceWindowsVehicleApproachEnabledStatesUseCase(repo)

            repo.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to false,
                )
            assertEquals(expected, useCase().first())
            coVerify(exactly = 1) {
                repo.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.StartReadout, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() =
        runTest {
            val repo = createAceWindowsVehicleApproachPreferencesRepository(repository)
            val useCase = ObserveAceWindowsVehicleApproachEnabledStatesUseCase(repo)

            repo.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.VehicleApproach.StartReadout to true,
                    ReadoutItemKey.AceWindows.VehicleApproach.Root to false,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repo.saveEnabledState(ReadoutItemKey.AceWindows.VehicleApproach.Root, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }
}
