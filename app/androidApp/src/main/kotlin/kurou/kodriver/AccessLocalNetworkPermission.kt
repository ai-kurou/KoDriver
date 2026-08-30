package kurou.kodriver

import android.os.Build

/**
 * Android 17 (API 37) 以降はプライベートIPアドレスへの接続に
 * [android.Manifest.permission.ACCESS_LOCAL_NETWORK] のランタイム許可が必要になる。
 * Android 16 (API 36) はオプトイン移行期間であり、ローカルネットワークアクセスは
 * 暗黙的に許可されたままのため、それ未満のOSでは権限自体をリクエストしない。
 * 参考: https://developer.android.com/privacy-and-security/local-network-permission
 */
internal fun shouldRequestAccessLocalNetworkPermission(
    sdkInt: Int,
    isPermissionGranted: Boolean,
): Boolean = sdkInt >= Build.VERSION_CODES.CINNAMON_BUN && !isPermissionGranted
