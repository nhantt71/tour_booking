
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.project17.tourbooking.activities.search.SearchBarSection
import com.project17.tourbooking.activities.search.SearchViewModel
import com.project17.tourbooking.models.Review
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.utils.AuthState
import com.project17.tourbooking.utils.AuthViewModel
import com.project17.tourbooking.utils.LoginPrompt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MyTripActivity(searchViewModel: SearchViewModel, navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val tours = remember { mutableStateListOf<Pair<String, Tour>>() }
    val filteredTours = remember { mutableStateListOf<Pair<String, Tour>>() }
    val tourToBillIds = remember { mutableMapOf<String, List<String>>() } // Add this line
    var refreshNeeded by remember { mutableStateOf(false) } // Add this line

    LaunchedEffect(authState.value, searchViewModel.inputValue.value, refreshNeeded) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                val currentUser = authViewModel.auth.currentUser
                val currentEmail = currentUser?.email

                if (currentEmail != null) {
                    tours.clear()
                    tourToBillIds.clear() // Clear the map at the start

                    FirestoreHelper.getBillsByEmail(currentEmail) { bills ->
                        bills.forEach { bill ->
                            FirestoreHelper.getBillDetailsByBillId(bill.id) { billDetails ->
                                val billIds = mutableListOf<String>()
                                billDetails.forEach { billDetail ->
                                    FirestoreHelper.getTicketById(billDetail.ticketId) { ticket ->
                                        ticket?.let { nonNullTicket ->
                                            FirestoreHelper.getTourById2(nonNullTicket.tourId) { tour ->
                                                tour?.let { nonNullTour ->
                                                    // Add the bill ID to the mapping
                                                    billIds.add(bill.id)

                                                    if (tours.none { it.first == nonNullTicket.tourId }) {
                                                        tours.add(nonNullTicket.tourId to nonNullTour)
                                                    }

                                                    // Update the map with the list of bill IDs
                                                    tourToBillIds[nonNullTicket.tourId] = billIds

                                                    // Filter tours based on search input
                                                    filteredTours.clear()
                                                    filteredTours.addAll(
                                                        tours.filter { it.second.name.contains(searchViewModel.inputValue.value, ignoreCase = true) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is AuthState.Error -> {
                val errorMessage = (authState as AuthState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            is AuthState.Unauthenticated -> {
                // No action needed here; handled in the composable body
            }
            else -> Unit
        }
    }

    // Display login prompt if unauthenticated
    if (authState.value is AuthState.Unauthenticated) {
        LoginPrompt(navController)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Your Tours",
                style = Typography.headlineLarge,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchBarSection(searchViewModel)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredTours) { (documentId, tour) ->
                    val billIds = tourToBillIds[documentId] ?: emptyList() // Fetch the bill IDs for this tour
                    TourItem(
                        tour = tour,
                        documentId = documentId,
                        navController = navController,
                        authViewModel = authViewModel,
                        billIds = billIds,
                        onTourCanceled = {
                            refreshNeeded = !refreshNeeded
                        }
                    )
                }
            }
        }
    }
}




fun formatTimestampToDate(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

fun isEventPassed(startDate: Timestamp): Boolean {
    val currentDate = Calendar.getInstance().time
    return startDate.toDate().before(currentDate)
}

fun isWithinOneWeek(startDate: Timestamp): Boolean {
    val currentDate = Calendar.getInstance().time
    val diffInMillis = startDate.toDate().time - currentDate.time
    val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
    return diffInDays in 0..7
}

@Composable
fun TourItem(
    tour: Tour,
    documentId: String,
    navController: NavController,
    authViewModel: AuthViewModel,
    billIds: List<String>,
    onTourCanceled: () -> Unit // Add this parameter
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableFloatStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    val startDate = tour.startDate

    // Check if the tour can be canceled
    val canCancel = !isEventPassed(startDate) && !isWithinOneWeek(startDate)

    // Show review dialog if needed
    if (showDialog) {
        AddReviewDialog(
            onDismiss = { showDialog = false },
            onSubmit = {
                val email = authViewModel.currentUserEmail ?: ""
                if (email.isNotEmpty()) {
                    val review = Review(
                        rating = (rating * 10).toInt() / 10.0,
                        comment = comment,
                        email = email,
                        tourId = documentId
                    )
                    FirestoreHelper.addReview(review) { success, exception ->
                        if (success) {
                            Toast.makeText(context, "Review added successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorMessage = exception?.localizedMessage ?: "Failed to add review"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            },
            rating = rating,
            onRatingChange = { rating = it },
            comment = comment,
            onCommentChange = { comment = it }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate(NavigationItems.TripBookedDetail.route + "/${billIds.first()}")
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = tour.name, style = Typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Destination: ${tour.destination}", style = Typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                val formattedDate = formatTimestampToDate(tour.startDate)
                Text(text = "Start Date: $formattedDate", style = Typography.bodyMedium, color = Color.Gray)
            }

            Column {
                Text(
                    text = if (canCancel) "Cancel" else "Cannot Cancel",
                    style = Typography.bodyMedium.copy(color = if (canCancel) Color.Red else Color.Gray),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clickable {
                            if (canCancel) {
                                FirestoreHelper.deleteBillsAndDetails(billIds) { success, exception ->
                                    if (success) {
                                        Toast.makeText(context, "Tour canceled successfully", Toast.LENGTH_SHORT).show()
                                        onTourCanceled() // Call the callback to refresh the page
                                    } else {
                                        val errorMessage = exception?.localizedMessage ?: "Failed to cancel tour"
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Cannot cancel: Event is too close or has passed", Toast.LENGTH_LONG).show()
                            }
                        }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Review",
                    style = Typography.bodyMedium.copy(color = Color.Blue),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clickable {
                            showDialog = true
                        }
                )
            }
        }
    }
}


@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    rating: Float,
    onRatingChange: (Float) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Review") },
        text = {
            Column {
                Text("Rating:")
                Slider(
                    value = rating,
                    onValueChange = onRatingChange,
                    valueRange = 0f..5f
                )
                Text(text = "Rating: ${String.format("%.1f", rating)}", modifier = Modifier.padding(vertical = 8.dp))

                Text("Comment:")
                TextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    placeholder = { Text("Enter your comment") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit()
                onDismiss()
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}