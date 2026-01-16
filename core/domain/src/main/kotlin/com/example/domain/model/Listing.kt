package com.example.domain.model

data class Listing (
    val id: Int,
    val rooms: Int? = null,
    val bedrooms: Int? = null,
    val city: String,
    val area: Double,
    val imageUrl: String? = null,
    val price: Double,
    val vendor: String,
    val propertyType: PropertyType,
    val offerType: Int
)

enum class PropertyType(val value: String) {
    MAISON_VILLA("Maison - Villa"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): PropertyType {
            return when (value?.trim()) {
                "Maison - Villa" -> MAISON_VILLA
                else -> OTHER
            }
        }
    }
}