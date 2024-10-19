package com.project17.tourbooking.activities.tripdetail

import FirestoreHelper
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.R.drawable.fuji_mountain
import com.project17.tourbooking.models.CategoryWithId
import com.project17.tourbooking.models.Review
import com.project17.tourbooking.models.Ticket
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.models.TourWithId
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.AddToWishList
import com.project17.tourbooking.utils.AuthState
import com.project17.tourbooking.utils.AuthViewModel
import com.project17.tourbooking.utils.CategoryItem
import com.project17.tourbooking.utils.ReviewItem
import com.project17.tourbooking.utils.TourSummaryCard
import com.project17.tourbooking.utils.addedWishListIcon3x
import com.project17.tourbooking.utils.createTourPackages
import com.project17.tourbooking.utils.iconWithBackgroundModifier
import com.project17.tourbooking.utils.iconWithoutBackgroundModifier
import com.project17.tourbooking.utils.toAddWishListIcon3x


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TripDetailScreen(navController: NavHostController = rememberNavController(), tourId: String) {
    val tourState = remember { mutableStateOf<Tour?>(null) }
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel() // Obtain the AuthViewModel instance

    LaunchedEffect(tourId) {
        if (tourId.isNotEmpty()) {
            FirestoreHelper.getTourById2(tourId) { tour ->
                tourState.value = tour
            }
        }
    }

    val tour = tourState.value

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (tour != null) {
                NavBarSection(tour, navController)
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (tour != null) {
                TourSummaryCard(tour = tour)
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (tour != null) {
                CategoryListSection(tour = tour)
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (tour != null) {
                AboutTripSection(tour = tour)
            }
            Spacer(modifier = Modifier.height(32.dp))
            if (tour != null) {
                ReviewSection(tour = tour)
            }
        }
        if (tour != null) {
            FooterSection(
                tour = tour,
                modifier = Modifier.align(Alignment.BottomStart),
                onBookingButtonClick = { navController.navigate(NavigationItems.BookingDetail.route + "/${tourId}") },
                navController = navController,
                tourId = tourId,
                authViewModel = authViewModel // Pass AuthViewModel instance
            )
        }
    }
}


@Composable
fun CategoryListSection(tour: Tour) {
    var category by remember { mutableStateOf<CategoryWithId?>(null) }

    LaunchedEffect(Unit) {
        val categories = FirestoreHelper.loadCategories2()
        category = categories.find { it.id == tour.categoryId }
    }

    Column {
        Text(
            text = stringResource(R.string.category_text),
            style = Typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))

        category?.let {
            CategoryItem(category = it.category, onClick = {})
        } ?: Text(text = stringResource(R.string.category_not_found))
    }
}

@Composable
fun AboutTripSection(tour: Tour){
    Column {
        Text(
            text = stringResource(R.string.about_trip_text),
            style = Typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.sample_text),
            style = Typography.bodyLarge,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewSection(tour: Tour) {
    val reviews = remember { mutableStateListOf<Review>() }
    val toursWithIds = remember { mutableStateListOf<TourWithId>() }
    var averageRating by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        val loadedToursWithIds = FirestoreHelper.loadToursWithIds()
        toursWithIds.clear()
        toursWithIds.addAll(loadedToursWithIds)

        val tourWithId = toursWithIds.find { it.tour == tour }
        if (tourWithId != null) {
            val loadedReviews = FirestoreHelper.loadReviewsForTour(tourWithId.id)
            reviews.clear()
            reviews.addAll(loadedReviews)

            val newAverageRating = if (reviews.isNotEmpty()) {
                reviews.map { it.rating }.average()
            } else {
                0.0
            }

            averageRating = newAverageRating

            FirestoreHelper.updateTourRating(tourWithId.id, newAverageRating)
        }
    }

    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.review_text, reviews.size),
                style = Typography.headlineMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.ic_yellow_star_3x),
                contentDescription = stringResource(id = R.string.image_description_text),
                modifier = Modifier
                    .size(50.dp)
            )
            Text(
                text = String.format("%.1f", averageRating),
                style = Typography.headlineSmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(Modifier.height(if (reviews.isEmpty()) 0.dp else 300.dp)) {
            items(reviews) { review ->
                ReviewItem(review = review)
            }
        }
    }
}



@Composable
fun NavBarSection(
    tour: Tour,
    navController: NavHostController
){
    Row(
        Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "",
            tint = BlackDark900,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { navController.popBackStack() }
        )
        val context = LocalContext.current
        Text(
            text = tour.name,
            style = Typography.titleLarge,
            color = BlackDark900,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        AddToWishList(
            initiallyAddedToWishList = false,
            modifier = Modifier
                .iconWithBackgroundModifier()
                .align(Alignment.Top)
        )
    }

}


@Composable
fun FooterSection(
    tour: Tour,
    modifier: Modifier,
    onBookingButtonClick: (String) -> Unit,
    navController: NavHostController,
    tourId: String,
    authViewModel: AuthViewModel // Pass AuthViewModel as parameter
) {
    val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)
    val isAuthenticated = authState is AuthState.Authenticated
    val toursWithIds = remember { mutableStateListOf<TourWithId>() }
    val ticket = remember { mutableStateListOf<Ticket?>() }

    LaunchedEffect(tour) {
        val loadedToursWithIds = FirestoreHelper.loadToursWithIds()
        toursWithIds.clear()
        toursWithIds.addAll(loadedToursWithIds)

        val tourWithId = toursWithIds.find { it.tour == tour }
        if (tourWithId != null) {
            val loadedTickets = FirestoreHelper.loadTicketForTour(tourWithId.id)
            ticket.clear()
            ticket.addAll(loadedTickets)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BlackWhite0),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text(
                    text = String.format("%.2f", ticket.firstOrNull()?.price ?: 0.0),
                    style = Typography.headlineMedium,
                    color = ErrorDefault500
                )

                Text(
                    text = " /Person",
                    color = BlackLight400,
                    style = Typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isAuthenticated) {
                        onBookingButtonClick(tourId)
                    } else {
                        navController.navigate(NavigationItems.Login.route)
                    }
                },
                colors = ButtonColors(BrandDefault500, BlackDark900, BrandDefault500, BlackDark900),
                modifier = Modifier
                    .width(150.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.booking_button_text),
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
