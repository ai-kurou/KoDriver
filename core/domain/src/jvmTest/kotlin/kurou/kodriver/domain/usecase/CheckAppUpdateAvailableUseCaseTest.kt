package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckAppUpdateAvailableUseCaseTest {
    @MockK
    private lateinit var repository: AppUpdateRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createUseCase(release: AppUpdate?): CheckAppUpdateAvailableUseCase {
        coEvery { repository.getLatestRelease() } returns release
        return CheckAppUpdateAvailableUseCase(repository)
    }

    @Test
    fun `最新リリースがnullのとき新バージョンなしと判定する`() =
        runTest {
            val useCase = createUseCase(null)

            assertFalse(useCase("1.0.0"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `最新バージョンが現在と同じとき新バージョンなしと判定する`() =
        runTest {
            val useCase = createUseCase(AppUpdate("v1.0.0"))

            assertFalse(useCase("1.0.0"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `最新バージョンがパッチだけ上のとき新バージョンありと判定する`() =
        runTest {
            val useCase = createUseCase(AppUpdate("v1.0.1"))

            assertTrue(useCase("1.0.0"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `最新バージョンがマイナーだけ上のとき新バージョンありと判定する`() =
        runTest {
            val useCase = createUseCase(AppUpdate("v1.1.0"))

            assertTrue(useCase("1.0.9"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `最新バージョンがメジャーだけ上のとき新バージョンありと判定する`() =
        runTest {
            val useCase = createUseCase(AppUpdate("v2.0.0"))

            assertTrue(useCase("1.9.9"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `最新バージョンが現在より古いとき新バージョンなしと判定する`() =
        runTest {
            val useCase = createUseCase(AppUpdate("v0.9.9"))

            assertFalse(useCase("1.0.0"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `タグにvプレフィックスがない場合でも正しく比較する`() =
        runTest {
            val useCase = createUseCase(AppUpdate("1.1.0"))

            assertTrue(useCase("1.0.0"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `現在バージョンにvプレフィックスがある場合でも正しく比較する`() =
        runTest {
            val useCase = createUseCase(AppUpdate("v1.1.0"))

            assertTrue(useCase("v1.0.0"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `バージョンに数値以外のセグメントが含まれる場合は0として扱う`() =
        runTest {
            val useCase = createUseCase(AppUpdate("v1.0.alpha"))

            assertFalse(useCase("1.0.0"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }

    @Test
    fun `バージョンのセグメントが3未満の場合は不足分を0として扱う`() =
        runTest {
            val useCase = createUseCase(AppUpdate("v2"))

            assertTrue(useCase("1"))
            coVerify(exactly = 1) { repository.getLatestRelease() }
            confirmVerified(repository)
        }
}
