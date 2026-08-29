package kurou.kodriver.data.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository

internal class AndroidAccessLocalNetworkPermissionRepository(
    private val context: Context,
) : AccessLocalNetworkPermissionRepository {
    override fun isGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK) ==
            PackageManager.PERMISSION_GRANTED
    }
}
