package kurou.kodriver.domain.acewindows.usecase

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
import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createAceWindowsBrakeTemperaturePreferencesRepository(
    repository: AceWindowsBrakeTemperaturePreferencesRepository,
    initialHighThreshold: Celsius = Celsius(800),
): AceWindowsBrakeTemperaturePreferencesRepository {
    val highThreshold = MutableStateFlow(initialHighThreshold)
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeHighThresholdCelsius() } returns highThreshold
    coEvery { repository.saveHighThresholdCelsius(Celsius(900)) } answers { highThreshold.update { Celsius(900) } }
    every { repository.observeEnabledStates() } returns enabledStates
    listOf(
        AceWindowsReadoutItemKey.BrakeTemperature.OverheatWarning,
        AceWindowsReadoutItemKey.BrakeTemperature.Root,
    ).forEach { key ->
        listOf(true, false).forEach { enabled ->
            coEvery { repository.saveEnabledState(key, enabled) } answers {
                enabledStates.update { it + (key to enabled) }
            }
        }
    }
    return repository
}

class ObserveAceWindowsBrakeTemperatureEnabledStatesUseCaseTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerAceWindowsReadoutItemKeys()
    }

    private val repository: AceWindowsBrakeTemperaturePreferencesRepository = mockk()

    @Test
    fun `初期値はOverheatWarningのデフォルトtrueを返す`() =
        runTest {
            val repo = createAceWindowsBrakeTemperaturePreferencesRepository(repository)
            val useCase = ObserveAceWindowsBrakeTemperatureEnabledStatesUseCase(repo)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    AceWindowsReadoutItemKey.BrakeTemperature.OverheatWarning to true,
                )
            assertEquals(expected, useCase().first())
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val repo = createAceWindowsBrakeTemperaturePreferencesRepository(repository)
            val useCase = ObserveAceWindowsBrakeTemperatureEnabledStatesUseCase(repo)

            repo.saveEnabledState(AceWindowsReadoutItemKey.BrakeTemperature.OverheatWarning, false)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    AceWindowsReadoutItemKey.BrakeTemperature.OverheatWarning to false,
                )
            assertEquals(expected, useCase().first())
            coVerify(exactly = 1) {
                repo.saveEnabledState(AceWindowsReadoutItemKey.BrakeTemperature.OverheatWarning, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() =
        runTest {
            val repo = createAceWindowsBrakeTemperaturePreferencesRepository(repository)
            val useCase = ObserveAceWindowsBrakeTemperatureEnabledStatesUseCase(repo)

            repo.saveEnabledState(AceWindowsReadoutItemKey.BrakeTemperature.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    AceWindowsReadoutItemKey.BrakeTemperature.OverheatWarning to true,
                    AceWindowsReadoutItemKey.BrakeTemperature.Root to false,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repo.saveEnabledState(AceWindowsReadoutItemKey.BrakeTemperature.Root, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }
}
