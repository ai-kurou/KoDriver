package kurou.kodriver.data.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository

/**
 * Android 17 (API 37) 以降で強制適用される [Manifest.permission.ACCESS_LOCAL_NETWORK] の許可状態を返す。
 * Android 16 (API 36) はオプトイン移行期間でありローカルネットワークアクセスは暗黙的に許可されたままのため、
 * それ未満のOSでは常に許可済み（true）を返す。
 * 参考: https://developer.android.com/privacy-and-security/local-network-permission
 */
internal class AndroidAccessLocalNetworkPermissionRepository(
    private val context: Context,
) : AccessLocalNetworkPermissionRepository {
    override fun isGranted(): Boolean {
        if (!isAccessLocalNetworkPermissionCheckRequired(Build.VERSION.SDK_INT)) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK) ==
            PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Android 17 (API 37) 未満は権限チェック自体が不要（常に許可済み扱い）かどうかを判定する。
 * Robolectricが現時点でAPI 37のシミュレーションに対応していないため、SDKバージョン判定のみを
 * 独立した純粋関数として切り出し、Robolectricに依存しないユニットテストで検証できるようにしている。
 */
internal fun isAccessLocalNetworkPermissionCheckRequired(sdkInt: Int): Boolean =
    sdkInt >= Build.VERSION_CODES.CINNAMON_BUN
