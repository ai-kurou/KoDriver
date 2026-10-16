package kurou.kodriver.buildlogic.screenshottest

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert

fun SemanticsNodeInteraction.assertEditableTextEquals(expected: String) =
    assert(
        SemanticsMatcher("EditableText == $expected") {
            it.config.getOrNull(SemanticsProperties.EditableText)?.text == expected
        },
    )
