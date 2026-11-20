package kurou.kodriver.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 読み上げ開始音を表すアイコン定義。
 *
 * 画面ごとに様式（Outlined / Filled）を分けて使う。新しい画面で開始音のアイコンを使う場合も、
 * feature モジュール側でアイコンを直接指定せず、ここの値を参照すること。
 */
public object ReadoutStartSoundIcons {
    /** 設定一覧などで使う Outlined 様式の開始音アイコン（開始音あり）。 */
    public val OutlinedOn: ImageVector = Icons.Outlined.MusicNote

    /** 自動読み上げ一覧のヒント行などで使う Filled 様式の開始音アイコン（開始音あり）。 */
    public val FilledOn: ImageVector = Icons.Filled.MusicNote

    /** 自動読み上げ一覧のトグルで使う Filled 様式の開始音アイコン（開始音なし）。 */
    public val FilledOff: ImageVector = Icons.Filled.MusicOff
}
