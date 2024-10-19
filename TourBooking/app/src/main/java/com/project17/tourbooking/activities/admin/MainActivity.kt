package com.project17.tourbooking.activities.admin

import ManageAccountsScreen
import MyTripActivity
import ProfileActivity
import WishListScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.activities.admin.ui.theme.TourBookingTheme
import com.project17.tourbooking.activities.home.HomeScreen
import com.project17.tourbooking.activities.search.SearchScreen
import com.project17.tourbooking.activities.search.SearchViewModel
import com.project17.tourbooking.activities.search.SearchWithCategoryScreenContent
import com.project17.tourbooking.activities.tripdetail.TripDetailScreen
import com.project17.tourbooking.activities.user.AccountCreated
import com.project17.tourbooking.activities.user.CreateAccount
import com.project17.tourbooking.activities.user.CreateNewPassword
import com.project17.tourbooking.activities.user.Explore
import com.project17.tourbooking.activities.user.ForgotPasswordScreen
import com.project17.tourbooking.activities.user.LoginScreen
import com.project17.tourbooking.activities.user.OtpVerification
import com.project17.tourbooking.utils.AuthViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TourBookingTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "my_trip") {
                    composable("login") { LoginScreen(navController, AuthViewModel()) }
                    composable("create_account") { CreateAccount(navController, AuthViewModel()) }
                    composable("otp_verification") { OtpVerification(navController) }
                    composable("forgot_password") { ForgotPasswordScreen(navController) }
                    composable("create_new_password/{email}") { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        CreateNewPassword(navController, email)
                    }
                    composable("account_created") { AccountCreated(navController) }
                    composable("explore") { Explore(navController) }
                    composable("my_trip") { MyTripActivity(SearchViewModel(), navController, AuthViewModel()) }
                    composable("profile") { ProfileActivity(navController, AuthViewModel()) }
                    composable("manage_categories") { ManageCategoriesScreen(navController) }
                    composable("manage_tours") { ManageToursScreen(navController) }
                    composable("edit_tour/{tourId}") { backStackEntry ->
                        val tourId = backStackEntry.arguments?.getString("tourId")
                        if (tourId != null) {
                            EditTourScreen(tourId, navController)
                        }
                    }
                    composable("add_tour") { AddTourScreen(navController) }
                    composable("manage_overview") { ManageOverviewScreen(navController, AuthViewModel()) }
                    composable("manage_accounts") { ManageAccountsScreen(navController, AuthViewModel()) }
//                    composable("tourDetail/{documentId}") { backStackEntry ->
//                        val documentId = backStackEntry.arguments?.getString("documentId")
//                        if (documentId != null) {
//                            TripDetailScreen(navController, documentId)
//                            TripDetailScreen(navController, documentId)
//                        }
//                    }
                    composable("home") { HomeScreen(modifier = Modifier, navController) }
                    composable("search") { SearchScreen(navController) }
//                    composable("search_category") { SearchWithCategoryScreenContent(tourList = mutableListOf(), navController) }
                    composable("trip_detail") { TripDetailScreen(navController, "0M8a6XbAea2Nvdk0mZ2o") }
                }
            }
        }
    }
}

