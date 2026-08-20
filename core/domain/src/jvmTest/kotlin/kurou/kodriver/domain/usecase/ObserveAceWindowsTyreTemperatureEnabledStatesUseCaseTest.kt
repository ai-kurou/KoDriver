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
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createAceWindowsTyreTemperaturePreferencesRepository(
    repository: AceWindowsTyreTemperaturePreferencesRepository,
    initialHighThreshold: Celsius = Celsius(90),
): AceWindowsTyreTemperaturePreferencesRepository {
    val highThreshold = MutableStateFlow(initialHighThreshold)
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeHighThresholdCelsius() } returns highThreshold
    coEvery { repository.saveHighThresholdCelsius(Celsius(100)) } answers { highThreshold.update { Celsius(100) } }
    every { repository.observeEnabledStates() } returns enabledStates
    listOf(
        ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning,
        ReadoutItemKey.AceWindows.TyreTemperature.Root,
    ).forEach { key ->
        listOf(true, false).forEach { enabled ->
            coEvery { repository.saveEnabledState(key, enabled) } answers {
                enabledStates.update { it + (key to enabled) }
            }
        }
    }
    return repository
}

class ObserveAceWindowsTyreTemperatureEnabledStatesUseCaseTest {
    @MockK
    private lateinit var repository: AceWindowsTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値はOverheatWarningのデフォルトtrueを返す`() =
        runTest {
            val repo = createAceWindowsTyreTemperaturePreferencesRepository(repository)
            val useCase = ObserveAceWindowsTyreTemperatureEnabledStatesUseCase(repo)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to true,
                )
            assertEquals(expected, useCase().first())
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val repo = createAceWindowsTyreTemperaturePreferencesRepository(repository)
            val useCase = ObserveAceWindowsTyreTemperatureEnabledStatesUseCase(repo)

            repo.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to false,
                )
            assertEquals(expected, useCase().first())
            coVerify(exactly = 1) {
                repo.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() =
        runTest {
            val repo = createAceWindowsTyreTemperaturePreferencesRepository(repository)
            val useCase = ObserveAceWindowsTyreTemperatureEnabledStatesUseCase(repo)

            repo.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to true,
                    ReadoutItemKey.AceWindows.TyreTemperature.Root to false,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repo.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.Root, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }
}
