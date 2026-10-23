@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherandroidaudiousagedetail

import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AudioSettingsActionsTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(sdk = [36])
    fun `音量パネルアクションは音量パネルIntentを起動する`() {
        rule.setContent {
            val action = rememberOpenVolumePanel()
            Text("open", Modifier.clickable { action?.invoke() })
        }
        rule.onNode(hasText("open")).performClick()
        assertEquals(Settings.Panel.ACTION_VOLUME, shadowOf(rule.activity).nextStartedActivity.action)
    }

    @Test
    @Config(sdk = [28])
    fun `API28では音量パネルアクションを提供しない`() {
        rule.setContent {
            val action = rememberOpenVolumePanel()
            Text(if (action == null) "null" else "available")
        }
        rule.onNode(hasText("null")).assertExists()
    }

    @Test
    @Config(sdk = [36])
    fun `サウンド設定アクションは設定Intentを起動する`() {
        rule.setContent {
            val action = rememberOpenSoundSettings()
            Text("open", Modifier.clickable(onClick = action!!))
        }
        rule.onNode(hasText("open")).performClick()
        assertEquals(Settings.ACTION_SOUND_SETTINGS, shadowOf(rule.activity).nextStartedActivity.action)
    }
}
