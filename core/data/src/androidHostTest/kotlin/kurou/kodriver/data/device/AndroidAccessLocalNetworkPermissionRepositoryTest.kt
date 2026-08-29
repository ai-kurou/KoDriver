@file:Suppress("FunctionNaming")

package kurou.kodriver.data.device

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidAccessLocalNetworkPermissionRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AndroidAccessLocalNetworkPermissionRepository(context)

    @Test
    fun `Android16以降で権限が許可済みの場合trueを返す`() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.ACCESS_LOCAL_NETWORK)

        assertTrue(repository.isGranted())
    }

    @Test
    fun `Android16以降で権限が未許可の場合falseを返す`() {
        shadowOf(context as Application).denyPermissions(Manifest.permission.ACCESS_LOCAL_NETWORK)

        assertFalse(repository.isGranted())
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidAccessLocalNetworkPermissionRepositoryLegacyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AndroidAccessLocalNetworkPermissionRepository(context)

    @Test
    fun `Android16未満では権限が未許可でもtrueを返す`() {
        shadowOf(context as Application).denyPermissions(Manifest.permission.ACCESS_LOCAL_NETWORK)

        assertTrue(repository.isGranted())
    }
}
