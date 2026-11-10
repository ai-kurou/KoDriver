package kurou.kodriver

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kurou.kodriver.presentation.MainWindowSavedBounds
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainWindowBoundsPersistenceTest {
    private val primaryScreen = Rectangle(0, 0, 1920, 1080)

    @Test
    fun `保存値が画面内なら位置とサイズをそのまま復元する`() {
        val restored =
            restoreMainWindowBounds(
                MainWindowSavedBounds(x = 100, y = 50, width = 1200, height = 800),
                listOf(primaryScreen),
            )

        assertEquals(DpSize(1200.dp, 800.dp), restored.size)
        assertEquals(WindowPosition.Absolute(100.dp, 50.dp), restored.position)
    }

    @Test
    fun `最小サイズを下回る保存値は最小サイズまで引き上げる`() {
        val restored =
            restoreMainWindowBounds(
                MainWindowSavedBounds(x = 100, y = 50, width = 100, height = 100),
                listOf(primaryScreen),
            )

        assertEquals(MAIN_WINDOW_MIN_SIZE, restored.size)
    }

    @Test
    fun `位置が未保存なら位置は復元せずサイズだけ復元する`() {
        val restored =
            restoreMainWindowBounds(
                MainWindowSavedBounds(x = null, y = null, width = 1200, height = 800),
                listOf(primaryScreen),
            )

        assertEquals(DpSize(1200.dp, 800.dp), restored.size)
        assertNull(restored.position)
    }

    @Test
    fun `保存時のモニタが外れて画面外になる位置は復元しない`() {
        val restored =
            restoreMainWindowBounds(
                MainWindowSavedBounds(x = -1800, y = 100, width = 1200, height = 800),
                listOf(primaryScreen),
            )

        assertNull(restored.position)
    }

    @Test
    fun `読み込み中は復元も保存もしない`() =
        runComposeUiTest {
            val windowState = WindowState(size = DpSize(1000.dp, 600.dp))
            val saved = mutableListOf<List<Int>>()

            setContent {
                MainWindowBoundsPersistence(
                    windowState = windowState,
                    savedBounds = null,
                    onBoundsChange = { x, y, width, height -> saved += listOf(x, y, width, height) },
                    screenBounds = { listOf(primaryScreen) },
                )
            }
            waitForIdle()
            mainClock.advanceTimeBy(2_000)

            assertEquals(DpSize(1000.dp, 600.dp), windowState.size)
            assertTrue(saved.isEmpty())
        }

    @Test
    fun `読み込み完了後に復元し、移動とリサイズを保存する`() =
        runComposeUiTest {
            val windowState = WindowState(size = DpSize(1000.dp, 600.dp))
            val savedBounds = mutableStateOf<MainWindowSavedBounds?>(null)
            val saved = mutableListOf<List<Int>>()

            setContent {
                MainWindowBoundsPersistence(
                    windowState = windowState,
                    savedBounds = savedBounds.value,
                    onBoundsChange = { x, y, width, height -> saved += listOf(x, y, width, height) },
                    screenBounds = { listOf(primaryScreen) },
                )
            }
            savedBounds.value = MainWindowSavedBounds(x = 100, y = 50, width = 1200, height = 800)
            waitForIdle()

            assertEquals(DpSize(1200.dp, 800.dp), windowState.size)
            assertEquals(WindowPosition.Absolute(100.dp, 50.dp), windowState.position)

            waitUntil(timeoutMillis = 5_000) { saved.isNotEmpty() }
            assertEquals(listOf(listOf(100, 50, 1200, 800)), saved)

            windowState.position = WindowPosition.Absolute(300.dp, 200.dp)
            windowState.size = DpSize(900.dp, 700.dp)
            waitUntil(timeoutMillis = 5_000) { saved.size == 2 }

            assertEquals(listOf(300, 200, 900, 700), saved.last())
        }

    @Test
    fun `最大化中は保存しない`() =
        runComposeUiTest {
            val windowState = WindowState(size = DpSize(1000.dp, 600.dp))
            val saved = mutableListOf<List<Int>>()

            setContent {
                MainWindowBoundsPersistence(
                    windowState = windowState,
                    savedBounds = MainWindowSavedBounds(x = 100, y = 50, width = 1200, height = 800),
                    onBoundsChange = { x, y, width, height -> saved += listOf(x, y, width, height) },
                    screenBounds = { listOf(primaryScreen) },
                )
            }
            waitForIdle()
            mainClock.advanceTimeBy(1_000)
            saved.clear()

            windowState.placement = WindowPlacement.Maximized
            windowState.position = WindowPosition.Absolute(0.dp, 0.dp)
            windowState.size = DpSize(1920.dp, 1080.dp)
            waitForIdle()
            mainClock.advanceTimeBy(1_000)

            assertTrue(saved.isEmpty())
        }
}
