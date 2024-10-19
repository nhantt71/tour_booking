package com.project17.tourbooking.utils

import FirestoreHelper
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.google.firebase.Timestamp
import com.project17.tourbooking.R
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.models.Review
import com.project17.tourbooking.models.TourWithId
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackLight100
import com.project17.tourbooking.ui.theme.BlackLight200
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.ErrorDark600
import com.project17.tourbooking.ui.theme.SuccessDefault500
import com.project17.tourbooking.ui.theme.Typography
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Calendar

val addedWishListIcon2x = R.drawable.ic_added_to_wishlist_2x
val addedWishListIcon3x = R.drawable.ic_added_to_wishlist_3x

val toAddWishListIcon2x = R.drawable.ic_to_add_to_wishlist_2x
val toAddWishListIcon3x = R.drawable.ic_to_add_to_wishlist_3x

@Composable
fun AddToWishList(
    modifier: Modifier = Modifier.iconWithBackgroundModifier(),
    initiallyAddedToWishList: Boolean,
    addedIcon: Int = addedWishListIcon2x,
    toAddIcon: Int = toAddWishListIcon2x,
    context: Context = LocalContext.current
) {
    var isInWishlist by remember {
        mutableStateOf(initiallyAddedToWishList)
    }

    Icon(
        painter = painterResource(
            id = if (isInWishlist) addedIcon else toAddIcon
        ),
        tint = if (isInWishlist) Color.Red else Color.Unspecified,
        contentDescription = "Favorite",
        modifier = modifier.clickable(onClick = {
            isInWishlist = !isInWishlist
            Toast.makeText(
                context,
                if (isInWishlist) "Added to Wishlist" else "Removed from Wishlist",
                Toast.LENGTH_SHORT
            ).show()

        })
    )
}


@Composable
fun AlertDialogUtil(
    isDialogVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: Int,
    message: Int,
    confirmButtonText: Int,
    dismissButtonText: Int
){
    if(isDialogVisible){
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(id = title),
                    style = Typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(id = message),
                    style = Typography.titleMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                }) {
                    Text(
                        text = stringResource(id =confirmButtonText),
                        style = Typography.titleSmall
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                }) {
                    Text(
                        text = stringResource(id = dismissButtonText),
                        style = Typography.titleSmall
                    )
                }
            }
        )
    }
}

@Composable
fun TourCardInHorizontal(
    modifier: Modifier = Modifier,
    tour: TourPackage,
    navController: NavHostController,
    onMeasured: (Dp) -> Unit = {}
) {
    val density = LocalDensity.current
    val tourId = "1"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val height = with(density) { coordinates.size.height.toDp() }
                onMeasured(height)
            }
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = {
                navController.navigate(NavigationItems.TripDetail.route + "/${tourId}")
            })
            .border(
                width = 1.dp,
                color = BlackLight200,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackWhite0)
                .padding(8.dp)
        ) {
            Image(
                painter = rememberImagePainter(tour.image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxHeight()
                    .weight(2f)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .weight(3f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BlackWhite0)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tour.name,
                        style = Typography.titleLarge,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = String.format("$" + "%.2f", tour.price),
                        style = Typography.bodyLarge,
                        color = ErrorDark600
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    GenerateStarFromRating(rating = tour.averageRating, Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = tour.description,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = BlackLight400,
                        style = Typography.bodyMedium,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}



@SuppressLint("DefaultLocale")
@Composable
fun GenerateStarFromRating(
    rating: Double,
    textColor: Color = BlackWhite0,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        val starSize = 16.dp
        if (rating.toInt() == 5) {
            repeat(5) {
                Image(
                    painter = painterResource(id = R.drawable.ic_yellow_star),
                    contentDescription = "",
                    modifier = Modifier.size(starSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        } else {
            val filledStars = rating.toInt()
            val emptyStars = 5 - filledStars
            repeat(filledStars) {
                Image(
                    painter = painterResource(id = R.drawable.ic_yellow_star),
                    contentDescription = "",
                    modifier = Modifier.size(starSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            repeat(emptyStars) {
                Image(
                    painter = painterResource(id = R.drawable.ic_white_star),
                    contentDescription = "",
                    modifier = Modifier.size(starSize)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Text(
            text = String.format("%.2f", rating),
            style = Typography.titleMedium,
            color = textColor
        )
    }
}


@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .background(
                color = BlackWhite0,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) SuccessDefault500 else BlackLight200,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Load image using Coil
            Image(
                painter = rememberImagePainter(data = category.icon), // Use Coil's painter for URL
                contentDescription = null, // Set to null for accessibility
                modifier = Modifier
                    .size(40.dp) // Adjust the size as per your design
                    .background(
                        color = BlackLight100,
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                style = Typography.titleLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
    Spacer(modifier = Modifier.width(5.dp))
}


@Composable
fun TourCardInVertical(
    tour: Tour,
    navController: NavHostController,
    context: Context = LocalContext.current
) {

    val toursWithIds = remember { mutableStateListOf<TourWithId>() }
    val tourId = remember { mutableStateOf("")}

    LaunchedEffect(Unit) {
        val loadedToursWithIds = FirestoreHelper.loadToursWithIds()
        toursWithIds.clear()
        toursWithIds.addAll(loadedToursWithIds)

        val tourWithId = toursWithIds.find { it.tour == tour }
        if (tourWithId != null) {
            tourId.value = tourWithId.id
        }
    }

    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = {
                navController.navigate(NavigationItems.TripDetail.route + "/${tourId.value}")
            })
    ) {
        AsyncImage(
            model = tour.image,
            contentDescription = "Tour Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .height(200.dp)
        )
        AddToWishList(
            initiallyAddedToWishList = false,
            modifier = Modifier
                .iconWithBackgroundModifier()
                .align(Alignment.TopEnd)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Log.d("TourCardInVertical", "Tour name: ${tour.name}")
            Text(
                text = tour.name,
                style = Typography.titleLarge,
                color = BlackWhite0
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = "",
                    tint = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = tour.destination,
                    style = Typography.bodyMedium,
                    color = BlackWhite0
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            GenerateStarFromRating(rating = tour.averageRating)
        }
    }
    Spacer(modifier = Modifier.width(16.dp))
}



@Composable
fun TourSummaryCard(tour: Tour) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(tour.image),
            contentDescription = stringResource(id = R.string.image_description_text),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(painter = painterResource(id = R.drawable.fuji_mountain), contentScale = ContentScale.Crop)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                Spacer(modifier = Modifier.weight(1f))

                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = tour.name,
                        style = Typography.headlineMedium,
                        color = BlackWhite0
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = "",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = tour.destination,
                            style = Typography.titleLarge,
                            color = BlackWhite0
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.people_have_explored_text, 100),
                        style = Typography.bodyLarge,
                        color = BlackWhite0
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GenerateStarFromRating(rating = tour.averageRating, textColor = BlackWhite0)
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

fun calculateAverageRating(reviews: List<Review>, tourId: String): Double {
    val tourReviews = reviews.filter { it.tourId == tourId }

    if (tourReviews.isEmpty()) {
        return 0.0
    }

    val totalRating = tourReviews.sumOf { it.rating }
    val averageRating = totalRating / tourReviews.size

    return String.format("%.1f", averageRating).toDouble()
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReviewItem(review: Review){
    var name by remember { mutableStateOf("Loading...") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val currentEmail = review.email

        FirestoreHelper.getCustomerByEmail(currentEmail) { customer ->
            customer?.let {
                name = it.fullName
            }
        }

        FirestoreHelper.getAvatarUrlFromAccount(currentEmail) { url ->
            avatarUrl = url
        }

    }
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = rememberImagePainter(
                data = avatarUrl,
                builder = {
                    placeholder(R.drawable.avatar_placeholder)
                    error(R.drawable.avatar_placeholder)
                }
            ),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = Typography.titleLarge,
                )
                Text(
                    text = calculateReviewDateDisplay(review.createdDate),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            GenerateStarFromRating(rating = review.rating.toDouble())

            Text(
                text = review.comment,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun calculateReviewDateDisplay(reviewDate: Timestamp): String {
    val reviewCalendar = Calendar.getInstance().apply {
        time = reviewDate.toDate()
    }
    val currentCalendar = Calendar.getInstance()

    val years = currentCalendar.get(Calendar.YEAR) - reviewCalendar.get(Calendar.YEAR)
    val months = currentCalendar.get(Calendar.MONTH) - reviewCalendar.get(Calendar.MONTH)
    val days = currentCalendar.get(Calendar.DAY_OF_MONTH) - reviewCalendar.get(Calendar.DAY_OF_MONTH)

    return when {
        years > 0 -> {
            val adjustedMonths = if (months < 0) months + 12 else months
            if (adjustedMonths > 0) {
                "$years years $adjustedMonths months ago"
            } else {
                "$years years ago"
            }
        }
        months > 0 -> "${months} months ago"
        days > 0 -> "${days} days ago"
        else -> "Today"
    }
}

@Composable
fun LoginPrompt(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Please log in to view your trips.",
            style = Typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("login") }) {
            Text("Login")
        }
    }
}