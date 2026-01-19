package com.example.data.model

import com.example.database.model.ListingEntity
import com.example.domain.model.PropertyType
import com.example.network.model.NetworkListing
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ListingMappersTest {

    @Test
    fun `test mapping from NetworkListing to ListingEntity`() {
        val networkListing = NetworkListing(
            id = 1,
            bedrooms = 4,
            city = "Paris",
            area = 150.0,
            url = "https://example.com/image.jpg",
            price = 2000000.0,
            professional = "Pro Real Estate",
            propertyType = "Maison",
            offerType = 1,
            rooms = 6
        )

        val listingEntity = networkListing.asEntity()

        // Assert
        assertEquals(1, listingEntity.id)
        assertEquals(2000000.0, listingEntity.price)
        assertEquals(150.0, listingEntity.area)
        assertEquals("Maison", listingEntity.propertyType)
        assertEquals("https://example.com/image.jpg", listingEntity.imageUrl)
        assertEquals(4, listingEntity.bedrooms)
        assertEquals(6, listingEntity.rooms)
        assertEquals("Paris", listingEntity.city)
        assertEquals("Pro Real Estate", listingEntity.professional)
        assertEquals(1, listingEntity.offerType)
    }

    @Test
    fun `test mapping from ListingEntity to domain Listing`() {
        val listingEntity = ListingEntity(
            id = 2,
            price = 750000.0,
            area = 80.0,
            propertyType = "Maison - Villa",
            imageUrl = "https://example.com/image2.jpg",
            bedrooms = 2,
            rooms = 4,
            city = "Nice",
            professional = "Nice Homes",
            offerType = 2
        )

        val domainListing = listingEntity.asExternalModel()

        // Assert
        assertEquals(2, domainListing.id)
        assertEquals(750000.0, domainListing.price)
        assertEquals(80.0, domainListing.area)
        assertEquals("Nice", domainListing.city)
        assertEquals(4, domainListing.rooms)
        assertEquals(2, domainListing.bedrooms)
        assertEquals("https://example.com/image2.jpg", domainListing.imageUrl)
        assertEquals(PropertyType.MAISON_VILLA, domainListing.propertyType) // Verify string conversion
        assertEquals("Nice Homes", domainListing.vendor) // Verify field rename
        assertEquals(2, domainListing.offerType)
    }
}
