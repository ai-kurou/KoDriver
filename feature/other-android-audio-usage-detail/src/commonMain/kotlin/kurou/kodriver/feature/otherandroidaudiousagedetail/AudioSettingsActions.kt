package kurou.kodriver.feature.otherandroidaudiousagedetail

import androidx.compose.runtime.Composable

/** 音量パネルを開くアクションを取得する。 */
@Composable
expect fun rememberOpenVolumePanel(): (() -> Unit)?

/** サウンド設定を開くアクションを取得する。 */
@Composable
expect fun rememberOpenSoundSettings(): (() -> Unit)?
