package kurou.kodriver.presentation

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kurou.kodriver.feature.readoutlist.ReadoutListItemType
import org.junit.Rule
import org.junit.Test

class ReadoutItemDetailContentTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `GT7燃料残量の読み上げ項目は燃料残量詳細ペインを表示する`() {
        rule.setContent {
            ReadoutItemDetailContent(
                itemType = ReadoutListItemType.Gt7Ps5.RemainingFuel,
                gt7Ps5RemainingFuelContent = { Text("GT7 remaining fuel detail") },
            )
        }

        rule.onNodeWithText("GT7 remaining fuel detail").assertIsDisplayed()
    }
}
