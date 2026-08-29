package kurou.kodriver

import android.os.Build

/**
 * Android 16 (API 36) 以降はプライベートIPアドレスへの接続に
 * [android.Manifest.permission.ACCESS_LOCAL_NETWORK] のランタイム許可が必要になる。
 * それ未満のOSでは権限自体が存在しないため、リクエスト自体を行わない。
 */
internal fun shouldRequestAccessLocalNetworkPermission(
    sdkInt: Int,
    isPermissionGranted: Boolean,
): Boolean = sdkInt >= Build.VERSION_CODES.BAKLAVA && !isPermissionGranted
