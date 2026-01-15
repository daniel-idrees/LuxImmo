package com.example.domain.model

data class Listing (
    val id: Int,
    val rooms: Int = 0,
    val bedrooms: Int = 0,
    val city: String,
    val area: Double,
    val imageUrl: String? = null,
    val price: String,
    val vendor: String,
    val propertyType: PropertyType,
)

enum class PropertyType {
    MAISON_VILLA,
    OTHER;

    companion object {
        fun fromString(value: String?): PropertyType {
            return when (value?.trim()) {
                "Maison - Villa" -> MAISON_VILLA
                else -> OTHER
            }
        }
    }
}