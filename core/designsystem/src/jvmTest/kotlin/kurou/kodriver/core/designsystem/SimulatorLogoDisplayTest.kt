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
import kotlin.test.assertNull

class SimulatorLogoDisplayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `gt7_ps5のロゴ画像を返す`() {
        var painter by mutableStateOf<Painter?>(null)
        composeRule.setContent {
            painter = simulatorLogoImage("gt7_ps5")
        }

        assertNotNull(painter)
    }

    @Test
    fun `lmu_windowsのロゴ画像を返す`() {
        var painter by mutableStateOf<Painter?>(null)
        composeRule.setContent {
            painter = simulatorLogoImage("lmu_windows")
        }

        assertNotNull(painter)
    }

    @Test
    fun `ace_windowsはロゴ画像がないためnullを返す`() {
        var painter by mutableStateOf<Painter?>(null)
        composeRule.setContent {
            painter = simulatorLogoImage("ace_windows")
        }

        assertNull(painter)
    }

    @Test
    fun `lmu_windowsのプラットフォーム名はWindows版を返す`() {
        var label by mutableStateOf<String?>(null)
        composeRule.setContent {
            label = simulatorPlatformLabel("lmu_windows")
        }

        assertEquals("Windows版", label)
    }

    @Test
    fun `gt7_ps5とace_windowsはプラットフォーム名を返さない`() {
        var gt7 by mutableStateOf<String?>("初期値")
        var ace by mutableStateOf<String?>("初期値")
        composeRule.setContent {
            gt7 = simulatorPlatformLabel("gt7_ps5")
            ace = simulatorPlatformLabel("ace_windows")
        }

        assertNull(gt7)
        assertNull(ace)
    }

    @Test
    fun `未対応のsimulatorIdを渡すとプラットフォーム名の取得で例外が発生する`() {
        assertFailsWith<IllegalStateException> {
            composeRule.setContent {
                simulatorPlatformLabel("unknown_simulator")
            }
        }
    }

    @Test
    fun `未対応のsimulatorIdを渡すとロゴ画像の取得で例外が発生する`() {
        assertFailsWith<IllegalStateException> {
            composeRule.setContent {
                simulatorLogoImage("unknown_simulator")
            }
        }
    }
}
