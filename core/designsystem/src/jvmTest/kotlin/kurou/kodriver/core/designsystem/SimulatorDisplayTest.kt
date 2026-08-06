@file:OptIn(ExperimentalTestApi::class)

package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SimulatorDisplayTest {
    @Test
    fun `lmu_windowsの表示名を返す`() =
        runDesktopComposeUiTest {
            var name: String? = null
            setContent {
                name = simulatorDisplayName("lmu_windows")
            }

            assertEquals("Le Mans Ultimate（Windows版）", name)
        }

    @Test
    fun `gt7_ps5の表示名を返す`() =
        runDesktopComposeUiTest {
            var name: String? = null
            setContent {
                name = simulatorDisplayName("gt7_ps5")
            }

            assertEquals("Gran Turismo 7（PS5）", name)
        }

    @Test
    fun `ace_windowsの表示名を返す`() =
        runDesktopComposeUiTest {
            var name: String? = null
            setContent {
                name = simulatorDisplayName("ace_windows")
            }

            assertEquals("Assetto Corsa EVO（Windows版）", name)
        }

    @Test
    fun `未対応のsimulatorIdを渡すと表示名の取得で例外が発生する`() =
        runDesktopComposeUiTest {
            assertFailsWith<IllegalStateException> {
                setContent {
                    simulatorDisplayName("unknown_simulator")
                }
            }
        }

    @Test
    fun `lmu_windowsのアイコンを返す`() =
        runDesktopComposeUiTest {
            var painter by mutableStateOf<Painter?>(null)
            setContent {
                painter = simulatorIcon("lmu_windows")
            }

            assertNotNull(painter)
        }

    @Test
    fun `gt7_ps5のアイコンを返す`() =
        runDesktopComposeUiTest {
            var painter by mutableStateOf<Painter?>(null)
            setContent {
                painter = simulatorIcon("gt7_ps5")
            }

            assertNotNull(painter)
        }

    @Test
    fun `ace_windowsのアイコンを返す`() =
        runDesktopComposeUiTest {
            var painter by mutableStateOf<Painter?>(null)
            setContent {
                painter = simulatorIcon("ace_windows")
            }

            assertNotNull(painter)
        }

    @Test
    fun `未対応のsimulatorIdを渡すとアイコンの取得で例外が発生する`() =
        runDesktopComposeUiTest {
            assertFailsWith<IllegalStateException> {
                setContent {
                    simulatorIcon("unknown_simulator")
                }
            }
        }
}
