package kurou.kodriver.core.devicevolumedata

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PollingVolumeFlowTest {
    @Test
    fun `指定間隔で取得し値が変化したときだけ流す`() =
        runTest {
            val values = listOf(10, 10, 20, 20)
            var calls = 0
            val result = mutableListOf<Int>()
            backgroundScope.launch {
                pollingVolumeFlow(intervalMillis = 1_000) { values[calls++] }
                    .take(3)
                    .toList(result)
            }

            runCurrent()
            assertEquals(listOf(10), result)
            assertEquals(1, calls)
            advanceTimeBy(999)
            runCurrent()
            assertEquals(listOf(10), result)
            assertEquals(1, calls)
            advanceTimeBy(1)
            runCurrent()
            assertEquals(listOf(10), result)
            assertEquals(2, calls)
            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(listOf(10, 20), result)
            assertEquals(3, calls)
            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(listOf(10, 20), result)
            assertEquals(4, calls)
        }

    @Test
    fun `取得に失敗した回は流さず次回の取得を続ける`() =
        runTest {
            var calls = 0
            val result = mutableListOf<Int>()
            backgroundScope.launch {
                pollingVolumeFlow(intervalMillis = 1_000) {
                    when (calls++) {
                        0 -> error("no device")
                        1 -> 40
                        2 -> error("disconnected")
                        else -> 50
                    }
                }.take(2).toList(result)
            }

            runCurrent()
            assertEquals(emptyList(), result)
            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(listOf(40), result)
            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(listOf(40), result)
            advanceTimeBy(1_000)
            runCurrent()
            assertEquals(listOf(40, 50), result)
            assertEquals(4, calls)
        }
}
