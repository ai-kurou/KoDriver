package kurou.kodriver.data.device

import android.content.Context
import android.os.Build
import android.os.VibratorManager
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository

internal class AndroidHapticFeedbackAvailabilityRepository(
    private val context: Context,
) : HapticFeedbackAvailabilityRepository {
    override fun isHapticFeedbackAvailable(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator?.hasVibrator() ?: false
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            vibrator?.hasVibrator() ?: false
        }
}
