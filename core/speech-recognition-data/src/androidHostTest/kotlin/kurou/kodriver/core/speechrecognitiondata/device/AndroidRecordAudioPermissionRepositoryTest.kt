@file:Suppress("FunctionNaming")

package kurou.kodriver.core.speechrecognitiondata.device

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
class AndroidRecordAudioPermissionRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = AndroidRecordAudioPermissionRepository(context)

    @Test
    fun `マイクの権限が許可されている場合はtrueを返す`() {
        shadowOf(context as Application).grantPermissions(Manifest.permission.RECORD_AUDIO)

        assertTrue(repository.isGranted())
    }

    @Test
    fun `マイクの権限が未許可の場合はfalseを返す`() {
        shadowOf(context as Application).denyPermissions(Manifest.permission.RECORD_AUDIO)

        assertFalse(repository.isGranted())
    }
}
