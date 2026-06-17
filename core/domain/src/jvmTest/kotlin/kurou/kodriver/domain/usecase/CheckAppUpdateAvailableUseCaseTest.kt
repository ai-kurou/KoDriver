package kurou.kodriver.domain.usecase

import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.AppUpdate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckAppUpdateAvailableUseCaseTest {

    @Test
    fun `最新リリースがnullのとき新バージョンなしと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(null))

        assertFalse(useCase("1.0.0"))
    }

    @Test
    fun `最新バージョンが現在と同じとき新バージョンなしと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(AppUpdate("v1.0.0")))

        assertFalse(useCase("1.0.0"))
    }

    @Test
    fun `最新バージョンがパッチだけ上のとき新バージョンありと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(AppUpdate("v1.0.1")))

        assertTrue(useCase("1.0.0"))
    }

    @Test
    fun `最新バージョンがマイナーだけ上のとき新バージョンありと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(AppUpdate("v1.1.0")))

        assertTrue(useCase("1.0.9"))
    }

    @Test
    fun `最新バージョンがメジャーだけ上のとき新バージョンありと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(AppUpdate("v2.0.0")))

        assertTrue(useCase("1.9.9"))
    }

    @Test
    fun `最新バージョンが現在より古いとき新バージョンなしと判定する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(AppUpdate("v0.9.9")))

        assertFalse(useCase("1.0.0"))
    }

    @Test
    fun `タグにvプレフィックスがない場合でも正しく比較する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(AppUpdate("1.1.0")))

        assertTrue(useCase("1.0.0"))
    }

    @Test
    fun `現在バージョンにvプレフィックスがある場合でも正しく比較する`() = runBlocking {
        val useCase = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository(AppUpdate("v1.1.0")))

        assertTrue(useCase("v1.0.0"))
    }
}
