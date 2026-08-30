@file:Suppress("FunctionNaming")

package kurou.kodriver.data.device

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * `isAccessLocalNetworkPermissionCheckRequired` のSDKバージョン境界判定は、Robolectricが
 * 現時点でAPI 37のシミュレーションに対応していないため、Robolectricに依存しないプレーンな
 * JUnitテストとして検証する（Android 17実機での`isGranted()`のContextCompat連携自体は
 * [AndroidAccessLocalNetworkPermissionRepositoryLegacyTest] のAPI 36側の分岐でのみ確認できる）。
 */
class AccessLocalNetworkPermissionCheckRequiredTest {
    @Test
    fun `Android17未満では権限チェックは不要`() {
        assertFalse(isAccessLocalNetworkPermissionCheckRequired(Build.VERSION_CODES.CINNAMON_BUN - 1))
    }

    @Test
    fun `Android17以降では権限チェックが必要`() {
        assertTrue(isAccessLocalNetworkPermissionCheckRequired(Build.VERSION_CODES.CINNAMON_BUN))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidAccessLocalNetworkPermissionRepositoryLegacyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AndroidAccessLocalNetworkPermissionRepository(context)

    @Test
    fun `Android17未満では権限が未許可でもtrueを返す`() {
        shadowOf(context as Application).denyPermissions(Manifest.permission.ACCESS_LOCAL_NETWORK)

        assertTrue(repository.isGranted())
    }
}
