package com.example.listings.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.core.ui.R
import com.example.listings.models.ListingUi
import com.example.listings.ui.list.ListingScreen
import com.example.listings.ui.list.ListingUiState
import com.example.ui.models.DisplayDoubleValue
import com.example.ui.models.DisplayIntValue
import com.example.ui.models.UiErrorConfig
import org.junit.Rule
import org.junit.Test

class ListingScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val listings = listOf(
        ListingUi(
            id = 1,
            price = DisplayDoubleValue(value = 1500000.0, formatted = "1 500 000 €"),
            pricePerSquareMeter = DisplayDoubleValue(value = 6000.0, formatted = "6 000 €/m²"),
            rooms = DisplayIntValue(value = 8, formatted = "8 rooms"),
            bedrooms = DisplayIntValue(value = 4, formatted = "4 bedrooms"),
            city = "Villers-sur-Mer",
            area = DisplayDoubleValue(value = 250.0, formatted = "250.0 m²"),
            imageUrl = "",
            vendor = "GSL EXPLORE", propertyType = "Maison - Villa"
        ),
        ListingUi(
            id = 2,
            price = DisplayDoubleValue(value = 1500000.0, formatted = "3 500 000 €"),
            pricePerSquareMeter = DisplayDoubleValue(value = 6000.0, formatted = "8 000 €/m²"),
            rooms = DisplayIntValue(value = 8, formatted = "18 rooms"),
            bedrooms = DisplayIntValue(value = 4, formatted = "14 bedrooms"),
            city = "Paris",
            area = DisplayDoubleValue(value = 250.0, formatted = "450.0 m²"),
            imageUrl = "",
            vendor = "GSL", propertyType = "Villa"
        )
    )

    @Test
    fun loader_WhenScreenIsLoading_isShown() {
        composeTestRule.setContent {
            ListingScreen(
                viewState = ListingUiState(
                    isLoading = true
                ),
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithTag("loader")
            .assertExists()
    }

    @Test
    fun errorView_whenScreenHasError_isShown() {
        composeTestRule.setContent {
            ListingScreen(
                viewState = ListingUiState(
                    errorConfig = UiErrorConfig(
                        errorText = stringResource(R.string.error_unknown),
                        onRetry = {}
                    )
                ),
                onAction = {}
            )
        }

        composeTestRule
            .onNodeWithTag("error_view")
            .assertExists()
    }

    @Test
    fun showsHeaderText_whenNoListingsAvailable() {
        var headerText = ""

        composeTestRule.setContent {
            ListingScreen(
                viewState = ListingUiState(
                    errorConfig = null,
                    isLoading = false,
                    listings = emptyList()
                ),
                onAction = {}
            )

            headerText = stringResource(R.string.no_results_found)
        }

        composeTestRule.onNodeWithText(headerText).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sorting_dropdown").assertDoesNotExist()
    }

    @Test
    fun showsListingDetails_whenListingExistsAndNoErrorAndLoading() {
        var headerText = ""

        composeTestRule.setContent {
            ListingScreen(
                viewState = ListingUiState(
                    listings = listings,
                    isLoading = false,
                    errorConfig = null
                ),
                onAction = {}
            )
           headerText = pluralStringResource(
                R.plurals.listing_results_header,
                listings.size,
                listings.size
            )
        }

        composeTestRule.onNodeWithText(headerText).assertIsDisplayed()
        composeTestRule.onNodeWithTag("sorting_dropdown").assertExists()

        composeTestRule.onNodeWithText("1 500 000 €").assertIsDisplayed()
        composeTestRule.onNodeWithText("6 000 €/m²").assertIsDisplayed()
        composeTestRule.onNodeWithText("4 bedrooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("8 rooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Villers-sur-Mer").assertIsDisplayed()
        composeTestRule.onNodeWithText("250.0 m²").assertIsDisplayed()
        composeTestRule.onNodeWithText("Maison - Villa").assertIsDisplayed()
        composeTestRule.onNodeWithText("GSL EXPLORE").assertIsDisplayed()
        composeTestRule.onNodeWithTag("listing_image_1", useUnmergedTree = true).assertExists()



        composeTestRule.onNodeWithText("3 500 000 €").assertIsDisplayed()
        composeTestRule.onNodeWithText("8 000 €/m²").assertIsDisplayed()
        composeTestRule.onNodeWithText("14 bedrooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("18 rooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paris").assertIsDisplayed()
        composeTestRule.onNodeWithText("450.0 m²").assertIsDisplayed()
        composeTestRule.onNodeWithText("Villa").assertIsDisplayed()
        composeTestRule.onNodeWithText("GSL").assertIsDisplayed()
        composeTestRule.onNodeWithTag("listing_image_2", useUnmergedTree = true).assertExists()
    }
}