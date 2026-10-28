package kurou.kodriver.feature.acewindowsdebugstatedetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kurou.kodriver.domain.usecase.SaveDebugStateCardOrderUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

class DebugStateCardOrderUseCasesTest {
    private val repository: DebugStateCardOrderPreferencesRepository = mockk()
    private val useCases =
        DebugStateCardOrderUseCases(
            ObserveDebugStateCardOrderUseCase(repository),
            ResolveDebugStateCardOrderUseCase(),
            SaveDebugStateCardOrderUseCase(repository),
        )
    private val savedNames =
        listOf(
            "SIMULATOR",
            "VEHICLE_LOCATION",
            "FLAG_INFO",
            "SIDE_BY_SIDE_VEHICLES",
            "BEST_LAP",
            "TYRE_CARCASS_TEMPERATURE",
            "FUEL_CONSUMPTION",
            "CAR_GAP",
        )

    @Test
    fun `旧保存名を全て読み出し未知名を除去して順序を維持する`() =
        runTest {
            val order = savedNames.reversed()
            every { repository.observeCardOrder() } returns
                flowOf(listOf("REMOVED_KEY", "GAME_PHASE", order.first()) + order.drop(1))

            assertEquals(AceWindowsDebugStateCardKey.entries.reversed(), useCases.observeKeys().first())
            verify(exactly = 1) { repository.observeCardOrder() }
            confirmVerified(repository)
        }

    @Test
    fun `全キーを旧保存名で保存する`() =
        runTest {
            coEvery { repository.saveCardOrder(savedNames) } returns Unit

            useCases.saveKeys(AceWindowsDebugStateCardKey.entries)

            coVerify(exactly = 1) { repository.saveCardOrder(savedNames) }
            confirmVerified(repository)
        }

    @Test
    fun `空の順序を読み書きできる`() =
        runTest {
            every { repository.observeCardOrder() } returns flowOf(emptyList())
            coEvery { repository.saveCardOrder(emptyList()) } returns Unit

            assertEquals(emptyList(), useCases.observeKeys().first())
            useCases.saveKeys(emptyList())

            verify(exactly = 1) { repository.observeCardOrder() }
            coVerify(exactly = 1) { repository.saveCardOrder(emptyList()) }
            confirmVerified(repository)
        }
}
