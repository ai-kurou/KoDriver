package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.feature.otherlist.generated.resources.Res
import kurou.kodriver.feature.otherlist.generated.resources.overlay_text_size_extra_large
import kurou.kodriver.feature.otherlist.generated.resources.overlay_text_size_extra_small
import kurou.kodriver.feature.otherlist.generated.resources.overlay_text_size_huge
import kurou.kodriver.feature.otherlist.generated.resources.overlay_text_size_large
import kurou.kodriver.feature.otherlist.generated.resources.overlay_text_size_maximum
import kurou.kodriver.feature.otherlist.generated.resources.overlay_text_size_medium
import kurou.kodriver.feature.otherlist.generated.resources.overlay_text_size_small
import org.jetbrains.compose.resources.StringResource

internal fun OverlayTextSize.labelResource(): StringResource =
    when (this) {
        OverlayTextSize.EXTRA_SMALL -> Res.string.overlay_text_size_extra_small
        OverlayTextSize.SMALL -> Res.string.overlay_text_size_small
        OverlayTextSize.MEDIUM -> Res.string.overlay_text_size_medium
        OverlayTextSize.LARGE -> Res.string.overlay_text_size_large
        OverlayTextSize.EXTRA_LARGE -> Res.string.overlay_text_size_extra_large
        OverlayTextSize.HUGE -> Res.string.overlay_text_size_huge
        OverlayTextSize.MAXIMUM -> Res.string.overlay_text_size_maximum
    }
