package kurou.kodriver.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SimulatorDisplayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `lmu_windowsの表示名を返す`() {
        var name: String? = null
        composeRule.setContent {
            name = simulatorDisplayName("lmu_windows")
        }

        assertEquals("Le Mans Ultimate（Windows版）", name)
    }

    @Test
    fun `gt7_ps5の表示名を返す`() {
        var name: String? = null
        composeRule.setContent {
            name = simulatorDisplayName("gt7_ps5")
        }

        assertEquals("Gran Turismo 7（PS5）", name)
    }

    @Test
    fun `ace_windowsの表示名を返す`() {
        var name: String? = null
        composeRule.setContent {
            name = simulatorDisplayName("ace_windows")
        }

        assertEquals("Assetto Corsa EVO（Windows版）", name)
    }

    @Test
    fun `未対応のsimulatorIdを渡すと表示名の取得で例外が発生する`() {
        assertFailsWith<IllegalStateException> {
            composeRule.setContent {
                simulatorDisplayName("unknown_simulator")
            }
        }
    }

    @Test
    fun `lmu_windowsのアイコンを返す`() {
        var painter by mutableStateOf<Painter?>(null)
        composeRule.setContent {
            painter = simulatorIcon("lmu_windows")
        }

        assertNotNull(painter)
    }

    @Test
    fun `gt7_ps5のアイコンを返す`() {
        var painter by mutableStateOf<Painter?>(null)
        composeRule.setContent {
            painter = simulatorIcon("gt7_ps5")
        }

        assertNotNull(painter)
    }

    @Test
    fun `ace_windowsのアイコンを返す`() {
        var painter by mutableStateOf<Painter?>(null)
        composeRule.setContent {
            painter = simulatorIcon("ace_windows")
        }

        assertNotNull(painter)
    }

    @Test
    fun `未対応のsimulatorIdを渡すとアイコンの取得で例外が発生する`() {
        assertFailsWith<IllegalStateException> {
            composeRule.setContent {
                simulatorIcon("unknown_simulator")
            }
        }
    }

    @Test
    fun `lmu_windowsの短縮名を返す`() {
        assertEquals("LMU", simulatorShortName("lmu_windows"))
    }

    @Test
    fun `gt7_ps5の短縮名を返す`() {
        assertEquals("GT7", simulatorShortName("gt7_ps5"))
    }

    @Test
    fun `ace_windowsの短縮名を返す`() {
        assertEquals("ACE", simulatorShortName("ace_windows"))
    }

    @Test
    fun `未対応のsimulatorIdを渡すと短縮名の取得で例外が発生する`() {
        assertFailsWith<IllegalStateException> {
            simulatorShortName("unknown_simulator")
        }
    }
}
