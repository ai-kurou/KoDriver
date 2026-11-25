@file:Suppress("FunctionNaming")

package kurou.kodriver.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.provider.Settings
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AccessLocalNetworkPermissionRequesterAndroidTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val registry = FakeActivityResultRegistry()
    private var resultCount = 0
    private lateinit var activity: Activity
    private lateinit var request: (Boolean) -> Unit

    @Test
    fun `権限を要求し、結果を受け取るとonResultを呼ぶ`() {
        setRequesterContent()

        requestAndRespond(granted = true)

        assertEquals(listOf<Any?>(Manifest.permission.ACCESS_LOCAL_NETWORK), registry.launchedInputs)
        assertEquals(1, resultCount)
    }

    @Test
    fun `1回目の曖昧な拒否では恒久拒否とせず、次も権限を要求する`() {
        setRequesterContent()

        requestAndRespond(granted = false)
        requestFromUi(openSettingsWhenPermanentlyDenied = true)

        assertEquals(2, registry.launchedInputs.size)
        assertNull(nextStartedAction())
    }

    @Test
    fun `曖昧な拒否が2回続くと恒久拒否とみなし、設定画面を開く`() {
        setRequesterContent()

        requestAndRespond(granted = false)
        requestAndRespond(granted = false)
        requestFromUi(openSettingsWhenPermanentlyDenied = true)

        assertEquals(2, registry.launchedInputs.size)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, nextStartedAction())
    }

    @Test
    fun `恒久拒否でも設定画面を開かない指定なら何もしない`() {
        setRequesterContent()

        requestAndRespond(granted = false)
        requestAndRespond(granted = false)
        requestFromUi(openSettingsWhenPermanentlyDenied = false)

        assertEquals(2, registry.launchedInputs.size)
        assertNull(nextStartedAction())
    }

    @Test
    fun `許可されると曖昧な拒否の回数をリセットする`() {
        setRequesterContent()

        requestAndRespond(granted = false)
        requestAndRespond(granted = true)
        requestAndRespond(granted = false)
        requestFromUi(openSettingsWhenPermanentlyDenied = true)

        assertEquals(4, registry.launchedInputs.size)
        assertNull(nextStartedAction())
    }

    @Test
    fun `拒否後も根拠の表示対象なら曖昧な拒否の回数をリセットする`() {
        setRequesterContent()

        requestAndRespond(granted = false)
        requestAndRespond(granted = false, rationaleAfter = true)
        // 次の要求の前に対象外へ戻し、「要求前は対象だった」経路に入らないようにする
        setShouldShowRationale(false)
        requestAndRespond(granted = false)
        requestFromUi(openSettingsWhenPermanentlyDenied = true)

        assertEquals(4, registry.launchedInputs.size)
        assertNull(nextStartedAction())
    }

    @Test
    fun `要求前は根拠の表示対象で拒否後に対象外になると1回で恒久拒否とみなす`() {
        setRequesterContent()
        setShouldShowRationale(true)

        requestAndRespond(granted = false, rationaleAfter = false)
        requestFromUi(openSettingsWhenPermanentlyDenied = true)

        assertEquals(1, registry.launchedInputs.size)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, nextStartedAction())
    }

    @Test
    fun `ContextWrapperに包まれたActivityでも根拠の表示対象を判定する`() {
        setRequesterContent { ContextWrapper(it) }
        setShouldShowRationale(true)

        requestAndRespond(granted = false, rationaleAfter = false)
        requestFromUi(openSettingsWhenPermanentlyDenied = false)

        assertEquals(1, registry.launchedInputs.size)
    }

    @Test
    fun `Activityが見つからない場合は根拠の表示対象外として扱う`() {
        setRequesterContent { ApplicationProvider.getApplicationContext() }
        setShouldShowRationale(true)

        // Activityを参照できれば根拠の表示対象となって回数がリセットされ、3回目も権限を要求してしまう
        requestAndRespond(granted = false)
        requestAndRespond(granted = false)
        requestFromUi(openSettingsWhenPermanentlyDenied = false)

        assertEquals(2, registry.launchedInputs.size)
    }

    private fun setRequesterContent(wrapContext: (Context) -> Context = { it }) {
        val owner =
            object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }
        composeRule.setContent {
            val context = LocalContext.current
            activity = context as Activity
            CompositionLocalProvider(
                LocalContext provides wrapContext(context),
                LocalActivityResultRegistryOwner provides owner,
            ) {
                request = rememberAccessLocalNetworkPermissionRequester(onResult = { resultCount++ })
            }
        }
        composeRule.waitForIdle()
    }

    private fun requestFromUi(openSettingsWhenPermanentlyDenied: Boolean) {
        composeRule.runOnIdle { request(openSettingsWhenPermanentlyDenied) }
        composeRule.waitForIdle()
    }

    private fun requestAndRespond(
        granted: Boolean,
        rationaleAfter: Boolean? = null,
    ) {
        requestFromUi(openSettingsWhenPermanentlyDenied = false)
        if (rationaleAfter != null) setShouldShowRationale(rationaleAfter)
        composeRule.runOnIdle { registry.respond(granted) }
        composeRule.waitForIdle()
    }

    private fun setShouldShowRationale(value: Boolean) {
        shadowOf(activity.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.ACCESS_LOCAL_NETWORK, value)
    }

    private fun nextStartedAction(): String? = shadowOf(activity).nextStartedActivity?.action

    private class FakeActivityResultRegistry : ActivityResultRegistry() {
        val launchedInputs = mutableListOf<Any?>()
        private var lastRequestCode = 0

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            lastRequestCode = requestCode
            launchedInputs += input
        }

        fun respond(granted: Boolean) {
            dispatchResult(lastRequestCode, granted)
        }
    }
}
