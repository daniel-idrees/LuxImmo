package com.example.listings.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.LuxImmoTheme
import com.example.designsystem.util.SPACING_SMALL
import com.example.listings.models.ListingUi
import com.example.listings.ui.components.ListingDetailView
import com.example.ui.components.LoadingView
import com.example.ui.models.DisplayDoubleValue
import com.example.ui.models.DisplayIntValue
import com.example.ui.util.DevicePreviews

@Composable
internal fun ListingDetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onErrorAction: (String) -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    viewState.error?.let {
        onErrorAction(it)
    } ?: MainContent(viewState = viewState)
}

@Composable
private fun MainContent(
    viewState: DetailUiState,
) {

    when {
        viewState.isLoading -> {
            LoadingView(
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> {
            viewState.listing?.let { listing ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SPACING_SMALL.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ListingDetailView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent),
                        imageModifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        listing = listing,
                        imageContentScale = ContentScale.FillBounds
                    )
                }

            }
        }
    }
}


@Composable
@DevicePreviews
private fun ListingDetailPreview() {
    val listing = ListingUi(
        id = 1,
        price = DisplayDoubleValue(value = 1500000.0, formatted = "1 500 000 €"),
        pricePerSquareMeter = DisplayDoubleValue(value = 6000.0, formatted = "6 000 €/m²"),
        rooms = DisplayIntValue(value = 8, formatted = "8 rooms"),
        bedrooms = DisplayIntValue(value = 4, formatted = "4 bedrooms"),
        city = "Villers-sur-Mer",
        area = DisplayDoubleValue(value = 250.0, formatted = "250.0 m²"),
        imageUrl = "https://v.seloger.com/s/crop/590x330/visuels/1/7/t/3/17t3fitclms3bzwv8qshbyzh9dw32e9l0p0udr80k.jpg",
        vendor = "GSL EXPLORE", propertyType = "Maison - Villa"
    )
    val viewState = DetailUiState(listing = listing)
    LuxImmoTheme {
        MainContent(viewState = viewState)
    }
}