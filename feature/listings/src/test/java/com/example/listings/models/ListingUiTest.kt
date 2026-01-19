package com.example.listings.models

import com.example.domain.model.Listing
import com.example.domain.model.PropertyType
import com.example.listings.resource.TestResourceProvider
import org.junit.Test
import java.text.NumberFormat
import java.util.Locale
import kotlin.test.assertEquals


/**
 * Unit tests for the mapping logic from the domain [Listing] to the UI [ListingUi].
 */
class ListingUiMapperTest {

    private val testResourceProvider = TestResourceProvider()

    @Test
    fun `test mapping from domain Listing to ListingUi`() {
        // 1. Arrange: Create a sample domain object
        val domainListing = Listing(
            id = 1,
            price = 1500000.0,
            area = 250.0,
            city = "Paris",
            bedrooms = 4,
            rooms = 8,
            imageUrl = "https://example.com/image.jpg",
            propertyType = PropertyType.MAISON_VILLA,
            vendor = "Luxe Properties",
            offerType = 1
        )


        val listingUi = domainListing.toListingUi(testResourceProvider)

        // Assert
        assertEquals(1, listingUi.id)
        assertEquals("Paris", listingUi.city)
        assertEquals("Luxe Properties", listingUi.vendor)
        assertEquals("Maison - Villa", listingUi.propertyType) // from PropertyType.HOUSE.value
        assertEquals("https://example.com/image.jpg", listingUi.imageUrl)

        // Assert price formatting
        assertEquals(1500000.0, listingUi.price.value)
        val expected = NumberFormat.getCurrencyInstance(Locale.FRANCE).apply {
            maximumFractionDigits = 0
        }.format(1500000.0)
        assertEquals(expected, listingUi.price.formatted)

        // Assert area formatting
        assertEquals(250.0, listingUi.area.value)
        assertEquals("250.0 m²", listingUi.area.formatted)

        // Assert rooms and bedrooms plural formatting
        assertEquals(8, listingUi.rooms?.value)
        assertEquals("8 rooms", listingUi.rooms?.formatted)
        assertEquals(4, listingUi.bedrooms?.value)
        assertEquals("4 bedrooms", listingUi.bedrooms?.formatted)

        // Assert price per square meter calculation and formatting
        val expectedPpsm = 1500000.0 / 250.0
        val formattedPpsm = NumberFormat.getCurrencyInstance(Locale.FRANCE).apply {
            maximumFractionDigits = 0
        }.format(expectedPpsm)
        assertEquals(expectedPpsm, listingUi.pricePerSquareMeter.value)
        assertEquals("$formattedPpsm/m²", listingUi.pricePerSquareMeter.formatted)
    }
}
