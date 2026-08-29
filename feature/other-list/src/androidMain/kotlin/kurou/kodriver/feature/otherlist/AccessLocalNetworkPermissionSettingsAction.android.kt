package kurou.kodriver.feature.otherlist

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * rememberOpenAccessLocalNetworkPermissionSettings のこのプラットフォーム向け実装。
 * アプリ詳細設定画面（権限一覧）を開く。
 */
@Composable
actual fun rememberOpenAccessLocalNetworkPermissionSettings(): () -> Unit {
    val context = LocalContext.current
    return {
        val intent =
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            )
        context.startActivity(intent)
    }
}
