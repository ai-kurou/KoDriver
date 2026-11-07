package kurou.kodriver.data.manualreadout

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InMemorySpeechPlaybackStateRepositoryTest {
    private val repository = InMemorySpeechPlaybackStateRepository()

    @Test
    fun `初期状態は再生中ではない`() {
        assertFalse(repository.isActive.value)
    }

    @Test
    fun `いずれかが再生中になると再生中になる`() {
        repository.update("lmu_windows", true)

        assertTrue(repository.isActive.value)
    }

    @Test
    fun `再生中のシミュレーターが終わると再生中ではなくなる`() {
        repository.update("lmu_windows", true)
        repository.update("lmu_windows", false)

        assertFalse(repository.isActive.value)
    }

    @Test
    fun `複数が再生中のときは全部終わるまで再生中のまま`() {
        repository.update("lmu_windows", true)
        repository.update("gt7_ps5", true)
        repository.update("lmu_windows", false)

        assertTrue(repository.isActive.value)

        repository.update("gt7_ps5", false)

        assertFalse(repository.isActive.value)
    }

    @Test
    fun `同じシミュレーターの重複した通知は1回の終了で打ち消される`() {
        repository.update("lmu_windows", true)
        repository.update("lmu_windows", true)
        repository.update("lmu_windows", false)

        assertFalse(repository.isActive.value)
    }

    @Test
    fun `再生中でないシミュレーターの終了通知は無視する`() {
        repository.update("lmu_windows", false)

        assertFalse(repository.isActive.value)
    }
}
