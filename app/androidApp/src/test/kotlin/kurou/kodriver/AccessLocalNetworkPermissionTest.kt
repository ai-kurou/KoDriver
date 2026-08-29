package kurou.kodriver

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessLocalNetworkPermissionTest {
    @Test
    fun `Android17未満では権限が未許可でもリクエストしない`() {
        val result =
            shouldRequestAccessLocalNetworkPermission(
                sdkInt = Build.VERSION_CODES.CINNAMON_BUN - 1,
                isPermissionGranted = false,
            )

        assertFalse(result)
    }

    @Test
    fun `Android17以降で未許可の場合はリクエストする`() {
        val result =
            shouldRequestAccessLocalNetworkPermission(
                sdkInt = Build.VERSION_CODES.CINNAMON_BUN,
                isPermissionGranted = false,
            )

        assertTrue(result)
    }

    @Test
    fun `Android17以降でも許可済みの場合はリクエストしない`() {
        val result =
            shouldRequestAccessLocalNetworkPermission(
                sdkInt = Build.VERSION_CODES.CINNAMON_BUN,
                isPermissionGranted = true,
            )

        assertFalse(result)
    }
}
