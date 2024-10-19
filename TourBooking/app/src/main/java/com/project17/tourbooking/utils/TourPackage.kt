package com.project17.tourbooking.utils

import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.project17.tourbooking.models.Review
import com.project17.tourbooking.models.Ticket
import com.project17.tourbooking.models.TourWithId


data class TourPackage(
    val name: String,
    val image: String,
    val price: Double,
    val averageRating: Double,
    val description: String,
    val categoryId: String = "",
    val tourId: String = ""
)

fun createTourPackages(
    toursWithIds: List<TourWithId>,
    reviews: List<Review>,
    tickets: List<Ticket>
): List<TourPackage> {
    return toursWithIds.map { tourWithId ->
        val tour = tourWithId.tour

        // Lấy giá vé từ Ticket
        val ticket = tickets.find { it.tourId == tourWithId.id }

        // Tính điểm trung bình từ Review
        val tourReviews = reviews.filter { it.tourId == tourWithId.id }
        val averageRating = if (tourReviews.isNotEmpty()) {
            tourReviews.map { it.rating }.average()
        } else {
            0.0
        }
        tour.averageRating = averageRating

        // Tạo TourPackage
        TourPackage(
            name = tour.name,
            image = tour.image,
            price = ticket?.price ?: 0.0,
            averageRating = averageRating,
            description = LoremIpsum(10).values.joinToString(" "),
            categoryId = tour.categoryId,
            tourId = tourWithId.id
        )
    }
}

