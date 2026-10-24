package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.runtime.Composable

/** rememberOpenVolumePanel のこのプラットフォーム向け実装。 */
@Composable
actual fun rememberOpenVolumePanel(): (() -> Unit)? = null

/** rememberOpenSoundSettings のこのプラットフォーム向け実装。 */
@Composable
actual fun rememberOpenSoundSettings(): (() -> Unit)? = null
