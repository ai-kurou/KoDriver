package kurou.kodriver.core.speechrecognitiondata.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository

/**
 * [Manifest.permission.RECORD_AUDIO]（マイク）の許可状態を返す。
 */
internal class AndroidRecordAudioPermissionRepository(
    private val context: Context,
) : RecordAudioPermissionRepository {
    override fun isGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
}
