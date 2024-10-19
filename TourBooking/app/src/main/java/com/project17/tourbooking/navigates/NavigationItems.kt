package com.project17.tourbooking.navigates

import com.project17.tourbooking.R

sealed class NavigationItems(
    val route:String,
    val title: String,
    val icon: Int,
    val iconFocused: Int
) {
    object Home: NavigationItems("home", "Home", R.drawable.ic_home, R.drawable.ic_home_focused)
    object MyTrip: NavigationItems("my_trip", "My Trip", R.drawable.ic_mytrip, R.drawable.ic_mytrip_focused)
    object WishList: NavigationItems("wishlist", "Wish List", R.drawable.ic_wishlist, R.drawable.ic_to_add_to_wishlist_3x)
    object Profile: NavigationItems("profile", "Profile", R.drawable.ic_profile, R.drawable.ic_profile_focused)
    object TripDetail: NavigationItems("tripdetail", "Trip Detail", 0, 0)
    object Search: NavigationItems("search", "Search", 0, 0)
    object SearchFilter: NavigationItems("searchfilter", "Search Filter", 0, 0)
    object BookingDetail: NavigationItems("bookingdetail", "Booking Detail", 0, 0)
    object BookingPaymentMethod: NavigationItems("booking", "Payment Method", 0, 0)
    object BookingSuccess: NavigationItems("bookingsuccess", "Booking Success", 0, 0)
    object PayActivity: NavigationItems("payactivity", "Pay Activity", 0, 0)
    object Login: NavigationItems("login", "Login", 0, 0)
    object Register: NavigationItems("create_account", "Register", 0, 0)
    object ForgotPassword: NavigationItems("forgot_password", "Forgot Password", 0, 0)
    object ResetPassword: NavigationItems("resetpassword", "Reset Password", 0, 0)
    object ManageAccount: NavigationItems("manage_accounts", "Manage Account", 0, 0)
    object ManageCategory: NavigationItems("manage_categories", "Manage Category", 0, 0)
    object ManageTour: NavigationItems("manage_tours", "Manage Tour", 0, 0)
    object ManageOverview : NavigationItems("manage_overview", "Manage Overview", 0, 0)
    object TripBookedDetail: NavigationItems("tripbookeddetail", "Trip Booked Detail", 0, 0)
    object Statistic: NavigationItems("statistic", "Statistic", 0, 0)
    object AccountCreated: NavigationItems("account_created", "Account Created", 0, 0)
    object Explore: NavigationItems("explore", "Explore", 0, 0)
    object EditTour: NavigationItems("edit_tour", "Edit Tour", 0, 0)
    object AddTour: NavigationItems("add_tour", "Add Tour", 0, 0)
}