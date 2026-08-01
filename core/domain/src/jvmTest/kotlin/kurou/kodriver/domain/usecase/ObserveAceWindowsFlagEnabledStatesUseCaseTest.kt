package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsFlagEnabledStatesUseCaseTest {

    @MockK
    lateinit var repository: AceWindowsFlagPreferencesRepository

    private lateinit var useCase: ObserveAceWindowsFlagEnabledStatesUseCase

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ObserveAceWindowsFlagEnabledStatesUseCase(repository)
    }

    @Test
    fun `永続化された値がない場合は全フラグがデフォルトで有効になる`() =
        runBlocking {
        every { repository.observeFlagEnabledStates() } returns flowOf(emptyMap<ReadoutItemKey, Boolean>())

        val result: Map<ReadoutItemKey, Boolean> = useCase().first()

        val expected: Map<ReadoutItemKey, Boolean> =
            mapOf(
            ReadoutItemKey.AceWindows.Flag.WhiteFlag to true,
            ReadoutItemKey.AceWindows.Flag.GreenFlag to true,
            ReadoutItemKey.AceWindows.Flag.RedFlag to true,
            ReadoutItemKey.AceWindows.Flag.BlueFlag to true,
            ReadoutItemKey.AceWindows.Flag.YellowFlag to true,
            ReadoutItemKey.AceWindows.Flag.BlackFlag to true,
            ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag to true,
            ReadoutItemKey.AceWindows.Flag.CheckeredFlag to true,
            ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag to true,
            ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag to true,
        )
        assertEquals(expected, result)
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        confirmVerified(repository)
    }

    @Test
    fun `永続化された値がデフォルトより優先される`() =
        runBlocking {
        every { repository.observeFlagEnabledStates() } returns
            flowOf(
            mapOf(ReadoutItemKey.AceWindows.Flag.BlueFlag to false),
        )

        val result = useCase().first()

        assertEquals(false, result.getValue(ReadoutItemKey.AceWindows.Flag.BlueFlag))
        assertEquals(true, result.getValue(ReadoutItemKey.AceWindows.Flag.WhiteFlag))
        verify(exactly = 1) { repository.observeFlagEnabledStates() }
        confirmVerified(repository)
    }
}
