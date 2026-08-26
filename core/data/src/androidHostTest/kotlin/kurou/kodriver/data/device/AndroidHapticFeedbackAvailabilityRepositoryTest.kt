@file:Suppress("FunctionNaming")

package kurou.kodriver.data.device

import android.content.Context
import android.os.VibratorManager
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
class AndroidHapticFeedbackAvailabilityRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AndroidHapticFeedbackAvailabilityRepository(context)

    @Test
    fun `振動機能がある端末ではtrueを返す`() {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        shadowOf(manager.defaultVibrator).setHasVibrator(true)

        assertTrue(repository.isHapticFeedbackAvailable())
    }

    @Test
    fun `振動機能がない端末ではfalseを返す`() {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        shadowOf(manager.defaultVibrator).setHasVibrator(false)

        assertFalse(repository.isHapticFeedbackAvailable())
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class AndroidHapticFeedbackAvailabilityRepositoryLegacyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AndroidHapticFeedbackAvailabilityRepository(context)

    @Test
    fun `Android12未満で振動機能がある端末ではtrueを返す`() {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        shadowOf(vibrator).setHasVibrator(true)

        assertTrue(repository.isHapticFeedbackAvailable())
    }

    @Test
    fun `Android12未満で振動機能がない端末ではfalseを返す`() {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        shadowOf(vibrator).setHasVibrator(false)

        assertFalse(repository.isHapticFeedbackAvailable())
    }
}
