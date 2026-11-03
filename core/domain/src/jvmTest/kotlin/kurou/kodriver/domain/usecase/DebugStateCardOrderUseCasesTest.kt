package kurou.kodriver.domain.usecase

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
import kotlin.test.Test
import kotlin.test.assertEquals

class DebugStateCardOrderUseCasesTest {
    private enum class TestCardKey { FIRST, SECOND, THIRD }

    private val repository: DebugStateCardOrderPreferencesRepository = mockk()
    private val useCases =
        DebugStateCardOrderUseCases(
            TestCardKey.entries,
            ObserveDebugStateCardOrderUseCase(repository),
            ResolveDebugStateCardOrderUseCase(),
            SaveDebugStateCardOrderUseCase(repository),
        )

    @Test
    fun `保存名をカードキーへ変換し未知名を除去して順序を維持する`() =
        runTest {
            every { repository.observeCardOrder() } returns flowOf(listOf("THIRD", "REMOVED_KEY", "FIRST"))

            assertEquals(listOf(TestCardKey.THIRD, TestCardKey.FIRST), useCases.observeKeys().first())
            verify(exactly = 1) { repository.observeCardOrder() }
            confirmVerified(repository)
        }

    @Test
    fun `カードキーを列挙名で保存する`() =
        runTest {
            coEvery { repository.saveCardOrder(listOf("SECOND", "FIRST")) } returns Unit

            useCases.saveKeys(listOf(TestCardKey.SECOND, TestCardKey.FIRST))

            coVerify(exactly = 1) { repository.saveCardOrder(listOf("SECOND", "FIRST")) }
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
