package com.example.listings.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.core.ui.R
import com.example.listings.models.ListingUi
import com.example.listings.ui.detail.DetailUiState
import com.example.listings.ui.detail.ListingDetailScreen
import com.example.ui.models.DisplayDoubleValue
import com.example.ui.models.DisplayIntValue
import com.example.ui.models.UiErrorConfig
import org.junit.Rule
import org.junit.Test


class ListingDetailScreenTest {
    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testListingUi = ListingUi(
        id = 1,
        price = DisplayDoubleValue(value = 1500000.0, formatted = "1 500 000 €"),
        pricePerSquareMeter = DisplayDoubleValue(
            value = 6000.0,
            formatted = "6 000 €/m²"
        ),
        rooms = DisplayIntValue(value = 8, formatted = "8 rooms"),
        bedrooms = DisplayIntValue(value = 4, formatted = "4 bedrooms"),
        city = "Villers-sur-Mer",
        area = DisplayDoubleValue(value = 250.0, formatted = "250.0 m²"),
        imageUrl = "",
        vendor = "GSL EXPLORE", propertyType = "Maison - Villa"
    )

    @Test
    fun showsLoader_whenLoading() {
        composeTestRule.setContent {
            ListingDetailScreen(
                viewState = DetailUiState(
                    isLoading = true
                )
            )
        }

        composeTestRule
            .onNodeWithTag("loader")
            .assertExists()
    }

    @Test
    fun showsError_whenErrorPresent() {
        composeTestRule.setContent {
            ListingDetailScreen(
                viewState = DetailUiState(
                    error = UiErrorConfig(
                        errorText = stringResource(R.string.error_unknown),
                        onRetry = {}
                    )
                )
            )
        }

        composeTestRule
            .onNodeWithTag("error_view")
            .assertExists()
    }

    @Test
    fun showsListingDetails_whenListingExistsAndNoErrorAndLoading() {
        composeTestRule.setContent {
            ListingDetailScreen(
                viewState = DetailUiState(
                    listing = testListingUi,
                    isLoading = false,
                    error = null
                )
            )
        }

        composeTestRule.onNodeWithTag("listing_detail").assertExists()

        composeTestRule.onNodeWithText("1 500 000 €").assertIsDisplayed()
        composeTestRule.onNodeWithText("6 000 €/m²").assertIsDisplayed()
        composeTestRule.onNodeWithText("4 bedrooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("8 rooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Villers-sur-Mer").assertIsDisplayed()
        composeTestRule.onNodeWithText("250.0 m²").assertIsDisplayed()
        composeTestRule.onNodeWithText("Maison - Villa").assertIsDisplayed()
        composeTestRule.onNodeWithText("GSL EXPLORE").assertIsDisplayed()
        composeTestRule.onNodeWithTag("listing_image", useUnmergedTree = true).assertExists()
    }
}