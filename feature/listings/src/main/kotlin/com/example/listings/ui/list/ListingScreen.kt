package com.example.listings.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.R
import com.example.designsystem.theme.LuxImmoTheme
import com.example.designsystem.util.SPACING_MEDIUM
import com.example.listings.models.ListingUi
import com.example.listings.ui.components.ListingDetailView
import com.example.listings.ui.components.SortingDropdownButton
import com.example.ui.models.DisplayDoubleValue
import com.example.ui.models.DisplayIntValue
import com.example.ui.util.OneTimeLaunchedEffect

@Composable
internal fun ListingScreen(
    listingViewModel: ListingViewModel,
    isDetailVisible: Boolean,
) {
    val viewState by listingViewModel.viewState.collectAsStateWithLifecycle()

    OneTimeLaunchedEffect {
        listingViewModel.setAction(ListingUiAction.Init)
    }

    if (!isDetailVisible) {
        listingViewModel.setAction(ListingUiAction.RemoveSelection)
    }

    MainContent(
        viewState = viewState,
        onAction = listingViewModel::setAction
    )
}

@Composable
private fun MainContent(
    viewState: ListingUiState,
    onAction: (ListingUiAction) -> Unit
) {
    if (viewState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (viewState.error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = viewState.error,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = SPACING_MEDIUM.dp, horizontal = 8.dp)
        ) {
            val itemCount = viewState.listings.size

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SPACING_MEDIUM.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerText = if (itemCount > 0) pluralStringResource(
                        R.plurals.listing_results_header,
                        itemCount,
                        itemCount
                    ) else stringResource(R.string.no_results_found)

                    Text(
                        modifier = Modifier,
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (itemCount > 0) {
                        SortingDropdownButton(
                            activeSort = viewState.activeSort,
                            onSortSelected = { newSort ->
                                onAction(ListingUiAction.OnSortChange(newSort))
                            })
                    }
                }
            }
            items(viewState.listings) { listing ->
                ListingDetailView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(bottom = SPACING_MEDIUM.dp)
                        .clickable {
                            onAction(ListingUiAction.OnListingClick(listing))
                        },
                    imageModifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    listing = listing,
                    imageContentScale = ContentScale.FillWidth,
                    isSelected = listing.id == viewState.selectedListing?.id
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
@Preview(showBackground = true)
private fun ListingPreview() {
    val list = listOf(
        ListingUi(
            id = 1,
            price = DisplayDoubleValue(value = 1500000.0, formatted = "1 500 000 €"),
            pricePerSquareMeter = DisplayDoubleValue(value = 6000.0, formatted = "6 000 €/m²"),
            rooms = DisplayIntValue(value = 8, formatted = "8 rooms"),
            bedrooms = DisplayIntValue(value = 4, formatted = "4 bedrooms"),
            city = "Villers-sur-Mer",
            area = DisplayDoubleValue(value = 250.0, formatted = "250.0 m²"),
            imageUrl = "https://v.seloger.com/s/crop/590x330/visuels/1/7/t/3/17t3fitclms3bzwv8qshbyzh9dw32e9l0p0udr80k.jpg",
            vendor = "GSL EXPLORE", propertyType = "Maison - Villa"
        ),
        ListingUi(
            id = 2,
            price = DisplayDoubleValue(value = 1500000.0, formatted = "1 500 000 €"),
            pricePerSquareMeter = DisplayDoubleValue(value = 6000.0, formatted = "6 000 €/m²"),
            rooms = DisplayIntValue(value = 8, formatted = "8 rooms"),
            bedrooms = DisplayIntValue(value = 4, formatted = "4 bedrooms"),
            city = "Villers-sur-Mer",
            area = DisplayDoubleValue(value = 250.0, formatted = "250.0 m²"),
            imageUrl = "https://v.seloger.com/s/crop/590x330/visuels/1/7/t/3/17t3fitclms3bzwv8qshbyzh9dw32e9l0p0udr80k.jpg",
            vendor = "GSL EXPLORE", propertyType = "Maison - Villa"
        )
    )
    val viewState = ListingUiState(listings = list) //TODO preview parameter
    LuxImmoTheme {
        MainContent(viewState, onAction = {})
    }
}