package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsFlagEnabledStatesUseCaseTest {
    val repository: AceWindowsFlagPreferencesRepository = mockk()

    private lateinit var useCase: ObserveAceWindowsFlagEnabledStatesUseCase

    @BeforeTest
    fun setUp() {
        useCase = ObserveAceWindowsFlagEnabledStatesUseCase(repository)
    }

    @Test
    fun `永続化された値がない場合は全フラグがデフォルトで有効になる`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap<ReadoutItemKey, Boolean>())

            val result: Map<ReadoutItemKey, Boolean> = useCase().first()

            val expected: Map<ReadoutItemKey, Boolean> =
                mapOf(
                    AceWindowsReadoutItemKey.Flag.WhiteFlag to true,
                    AceWindowsReadoutItemKey.Flag.GreenFlag to true,
                    AceWindowsReadoutItemKey.Flag.RedFlag to true,
                    AceWindowsReadoutItemKey.Flag.BlueFlag to true,
                    AceWindowsReadoutItemKey.Flag.YellowFlag to true,
                    AceWindowsReadoutItemKey.Flag.BlackFlag to true,
                    AceWindowsReadoutItemKey.Flag.BlackWhiteFlag to true,
                    AceWindowsReadoutItemKey.Flag.CheckeredFlag to true,
                    AceWindowsReadoutItemKey.Flag.OrangeCircleFlag to true,
                    AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag to true,
                )
            assertEquals(expected, result)
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            confirmVerified(repository)
        }

    @Test
    fun `永続化された値がデフォルトより優先される`() =
        runTest {
            every { repository.observeFlagEnabledStates() } returns
                flowOf(
                    mapOf(AceWindowsReadoutItemKey.Flag.BlueFlag to false),
                )

            val result = useCase().first()

            assertEquals(false, result.getValue(AceWindowsReadoutItemKey.Flag.BlueFlag))
            assertEquals(true, result.getValue(AceWindowsReadoutItemKey.Flag.WhiteFlag))
            verify(exactly = 1) { repository.observeFlagEnabledStates() }
            confirmVerified(repository)
        }
}
