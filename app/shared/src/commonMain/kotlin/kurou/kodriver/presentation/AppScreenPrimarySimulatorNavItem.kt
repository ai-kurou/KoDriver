package kurou.kodriver.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.nav_auto_readout
import kurou.kodriver.app.shared.generated.resources.nav_manual_readout
import kurou.kodriver.app.shared.generated.resources.nav_more
import kurou.kodriver.feature.main.AppScreenPrimarySimulatorIndicator
import kurou.kodriver.feature.main.appScreenPrimarySimulatorLabel
import org.jetbrains.compose.resources.stringResource

/**
 * シミュレーター選択項目のドロップダウン矢印アイコンを、メニュー展開時に回転させる角度。
 */
internal const val HALF_ROTATION_DEGREES = 180f

/**
 * NavigationRail ヘッダーに表示するシミュレーターロゴのサイズ。
 * ナビ項目内のアイコン（24dp）より大きくし、ヘッダーであることを視覚的に強調する。
 */
internal val PrimarySimulatorRailHeaderIconSize = 40.dp

/**
 * NavigationRail ヘッダーとナビ項目を区切る [HorizontalDivider] の幅。
 */
internal val PrimarySimulatorRailHeaderDividerWidth = 32.dp

@Composable
internal fun AppDestination.label(): String =
    when (this) {
        AppDestination.AutoReadout -> stringResource(Res.string.nav_auto_readout)
        AppDestination.ManualReadout -> stringResource(Res.string.nav_manual_readout)
        AppDestination.More -> stringResource(Res.string.nav_more)
    }

@Composable
internal fun AppNavIcon(
    dest: AppDestination,
    showBadge: Boolean,
    modifier: Modifier = Modifier,
) {
    BadgedBox(
        badge = {
            AnimatedVisibility(
                visible = showBadge,
                enter =
                    scaleIn(animationSpec = tween(durationMillis = 300)) +
                        fadeIn(animationSpec = tween(durationMillis = 300)),
                exit =
                    scaleOut(animationSpec = tween(durationMillis = 200)) +
                        fadeOut(animationSpec = tween(durationMillis = 200)),
            ) {
                Badge()
            }
        },
    ) {
        Icon(dest.icon, contentDescription = dest.label(), modifier = modifier)
    }
}

/**
 * NavigationBar / NavigationRail の先頭に表示する、現在選択中のシミュレーターの項目。
 * 他の [AppDestination] とは異なりタブ切り替えの対象ではないため、常に非選択として扱う。
 * タップするとシミュレーター選択メニューを開く。ラベル横にドロップダウン矢印アイコンを添えて、
 * 単なるタブではなくメニューを開く操作対象であることを視覚的に示す。NavigationBar ではメニューが
 * 上方向に展開されるため矢印を上向き（[Icons.Filled.ArrowDropUp]）にし、NavigationRail では
 * 右方向に展開されるため矢印を右向き（[Icons.AutoMirrored.Filled.ArrowRight]）にする。
 * ポップアップメニューの開閉状態（[expanded]）に応じて矢印アイコンを180度回転させ、
 * 開いているときは向きが反対になり、閉じると元の向きに戻ることでメニューの開閉状態を視覚的に示す。
 *
 * NavigationRail では、通常のナビ項目より大きくロゴを表示したうえで [HorizontalDivider] を添え、
 * シミュレーター選択（ヘッダ）とタブ切り替え（ナビ項目）が別の操作であることを視覚的に示すヘッダーとして表示する。
 */
@Composable
internal fun AppScreenPrimarySimulatorNavItem(
    resolvedLayoutType: NavigationSuiteType,
    selectedSimulatorId: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSimulatorSelected: (String) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    if (resolvedLayoutType == NavigationSuiteType.NavigationBar) {
        NavigationSuiteItem(
            icon = {
                AppScreenPrimarySimulatorIndicator(
                    simulatorId = selectedSimulatorId,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    onSimulatorSelected = onSimulatorSelected,
                    modifier = Modifier.size(24.dp),
                )
            },
            label = {
                val dropdownArrowRotation by
                    animateFloatAsState(targetValue = if (expanded) HALF_ROTATION_DEGREES else 0f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(appScreenPrimarySimulatorLabel(selectedSimulatorId))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).rotate(dropdownArrowRotation),
                    )
                }
            },
            selected = false,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                onExpandedChange(true)
            },
            navigationSuiteType = resolvedLayoutType,
            modifier = Modifier.testTag("primarySimulatorNavItem"),
        )
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .padding(vertical = AppSpacing.medium)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onExpandedChange(true)
                    }.testTag("primarySimulatorNavItem"),
        ) {
            AppScreenPrimarySimulatorIndicator(
                simulatorId = selectedSimulatorId,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                onSimulatorSelected = onSimulatorSelected,
                modifier = Modifier.size(PrimarySimulatorRailHeaderIconSize),
            )
            Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
            val dropdownArrowRotation by animateFloatAsState(targetValue = if (expanded) HALF_ROTATION_DEGREES else 0f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(appScreenPrimarySimulatorLabel(selectedSimulatorId), style = MaterialTheme.typography.labelMedium)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).rotate(dropdownArrowRotation),
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.medium))
            HorizontalDivider(modifier = Modifier.width(PrimarySimulatorRailHeaderDividerWidth))
        }
    }
}
