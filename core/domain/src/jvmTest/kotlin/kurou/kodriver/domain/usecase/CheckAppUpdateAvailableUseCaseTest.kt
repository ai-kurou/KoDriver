package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun createAppUpdateRepository(release: AppUpdate?): AppUpdateRepository {
    val repository = mockk<AppUpdateRepository>()
    coEvery { repository.getLatestRelease() } returns release
    return repository
}

class CheckAppUpdateAvailableUseCaseTest {

    @Test
    fun `最新リリースがnullのとき新バージョンなしと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(null))

        assertFalse(useCase("1.0.0"))
    }

    @Test
    fun `最新バージョンが現在と同じとき新バージョンなしと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("v1.0.0")))

        assertFalse(useCase("1.0.0"))
    }

    @Test
    fun `最新バージョンがパッチだけ上のとき新バージョンありと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("v1.0.1")))

        assertTrue(useCase("1.0.0"))
    }

    @Test
    fun `最新バージョンがマイナーだけ上のとき新バージョンありと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("v1.1.0")))

        assertTrue(useCase("1.0.9"))
    }

    @Test
    fun `最新バージョンがメジャーだけ上のとき新バージョンありと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("v2.0.0")))

        assertTrue(useCase("1.9.9"))
    }

    @Test
    fun `最新バージョンが現在より古いとき新バージョンなしと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("v0.9.9")))

        assertFalse(useCase("1.0.0"))
    }

    @Test
    fun `タグにvプレフィックスがない場合でも正しく比較する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("1.1.0")))

        assertTrue(useCase("1.0.0"))
    }

    @Test
    fun `現在バージョンにvプレフィックスがある場合でも正しく比較する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("v1.1.0")))

        assertTrue(useCase("v1.0.0"))
    }

    @Test
    fun `バージョンに数値以外のセグメントが含まれる場合は0として扱う`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("v1.0.alpha")))

        assertFalse(useCase("1.0.0"))
    }

    @Test
    fun `バージョンのセグメントが3未満の場合は不足分を0として扱う`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(createAppUpdateRepository(AppUpdate("v2")))

        assertTrue(useCase("1"))
    }
}
