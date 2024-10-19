package com.project17.tourbooking.activities.home

import FirestoreHelper
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.project17.tourbooking.R
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight100
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.AlertDialogUtil
import com.project17.tourbooking.models.Category
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.models.TourWithId
import com.project17.tourbooking.utils.AuthState
import com.project17.tourbooking.utils.AuthViewModel
import com.project17.tourbooking.utils.CategoryItem
import com.project17.tourbooking.utils.TourCardInHorizontal
import com.project17.tourbooking.utils.TourCardInVertical
import com.project17.tourbooking.utils.TourPackage
import com.project17.tourbooking.viewmodels.AppViewModel
import kotlin.system.exitProcess

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TourBookingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController(),
               appViewModel: AppViewModel = viewModel(), authViewModel: AuthViewModel = viewModel()) {
    var isDialogVisible by remember {
        mutableStateOf(false)
    }
    BackHandler (
        onBack = {
            isDialogVisible = !isDialogVisible
        }
    )
    if(isDialogVisible){
        AlertDialogUtil(
            isDialogVisible = true,
            onDismiss = { isDialogVisible = false },
            onConfirm = {
                exitProcess(0)
                        },
            title = R.string.exit_app_alert_dialog_title_text,
            message = R.string.exit_app_alert_dialog_message_text,
            confirmButtonText = R.string.exit_app_alert_dialog_confirm_button_text,
            dismissButtonText = R.string.exit_app_alert_dialog_dismiss_button_text
        )
    }
    Column(modifier = modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(
            top = 24.dp,
            start = 16.dp,
            end = 16.dp
        )
        .verticalScroll(rememberScrollState())) {
        HeaderSection(navController = navController, authViewModel = authViewModel)
        Spacer(modifier = Modifier.height(16.dp))
        SearchBarSection(navController, appViewModel)
        Spacer(modifier = Modifier
            .height(24.dp)
            .fillMaxWidth())
        ChooseCategorySection(navController = navController, appViewModel =  appViewModel)
        Spacer(modifier = Modifier
            .height(24.dp)
            .fillMaxWidth())
        FavoritePlaceSection(navController = navController)
        Spacer(modifier = Modifier
            .height(24.dp)
            .fillMaxWidth())
        PopularPackageSection(navController = navController)
    }
}

@Composable
fun HeaderSection(
    modifier: Modifier = Modifier,
    hasUnreadNotification: Boolean = false,
    navController: NavHostController,
    authViewModel: AuthViewModel // Thêm AuthViewModel để lấy thông tin người dùng
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf<String?>(null) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    // Watch the authentication state
    val authState = authViewModel.authState.observeAsState()

    // LaunchedEffect to fetch user data when authenticated
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                val currentUser = authViewModel.auth.currentUser
                val currentEmail = currentUser?.email

                if (currentEmail != null) {
                    // Fetch customer information from Firestore
                    FirestoreHelper.getCustomerByEmail(currentEmail) { customer ->
                        customer?.let {
                            name = it.fullName // Set the name
                        }
                    }

                    // Fetch avatar URL from Firestore
                    FirestoreHelper.getAvatarUrlFromAccount(currentEmail) { url ->
                        avatarUrl = url // Set the avatar URL
                    }
                }
            }
            is AuthState.Error -> {
                val errorMessage = (authState.value as AuthState.Error).message
                Log.e("HeaderSection", errorMessage)
            }
            is AuthState.Unauthenticated -> {

            }
            else -> Unit
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable { navController.navigate(NavigationItems.Profile.route) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = avatarUrl ?: R.drawable.default_avatar,
                contentDescription = stringResource(id = R.string.avatar_description_text),
                placeholder = painterResource(id = R.drawable.default_avatar),
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .clickable(onClick = {
                        navController.navigate(NavigationItems.Profile.route)
                    })
            )

            Text(
                text = "Hi, ${name ?: stringResource(id = R.string.default_username)}!",
                modifier = Modifier.padding(start = 10.dp),
                style = Typography.titleMedium
            )
        }

        // Notification Icon with Badge
        if (hasUnreadNotification) {
            BadgedBox(
                badge = {
                    Badge()
                },
                modifier = Modifier.wrapContentSize()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = stringResource(id = R.string.icon_notification_description_text),
                    modifier = Modifier.clickable(onClick = {
                        // Navigate to NotificationActivity
                    })
                )
            }
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = stringResource(id = R.string.icon_notification_description_text),
                modifier = Modifier.clickable(onClick = {
                    Toast.makeText(context, "Navigate to NotificationActivity", Toast.LENGTH_SHORT).show()
                })
            )
        }
    }

    // Spacer and extra text section
    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(id = R.string.home_question_text),
        modifier = Modifier.fillMaxWidth(),
        style = Typography.headlineMedium,
        maxLines = 2,
        overflow = TextOverflow.Clip
    )
}


@Composable
fun SearchBarSection(navController: NavHostController, appViewModel: AppViewModel){
    TextField(
        value = "",
        onValueChange = {
        },
        interactionSource = remember {
            MutableInteractionSource()
        }.also {
            source ->
            LaunchedEffect(source) {
                source.interactions.collect {
                    if (it is PressInteraction.Release) {
                        appViewModel.isChosenCategory.value = false
                        navController.navigate(NavigationItems.Search.route)
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.search_icon_description_text),
                tint = BlackDark900
            )
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_hint_text),
                style = Typography.bodyMedium,
                color = BlackLight300
            )
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = BlackLight100,
            focusedContainerColor = BlackLight100,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun ChooseCategorySection(modifier: Modifier = Modifier, navController: NavHostController, appViewModel: AppViewModel) {
    LaunchedEffect(Unit) {
        appViewModel.loadCategories(FirestoreHelper)
    }

    var isSeeAllClicked by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.choose_category_text),
                style = Typography.headlineMedium
            )
            Text(
                text = if (!isSeeAllClicked) stringResource(id = R.string.see_all_text)
                else stringResource(id = R.string.collapse_text),
                style = Typography.labelSmall,
                color = BlackLight300,
                modifier = Modifier.clickable { isSeeAllClicked = !isSeeAllClicked }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (!isSeeAllClicked) {
            LazyRow {
                items(appViewModel.categories) { category ->
                    CategoryItem(category = category, onClick = {
                        appViewModel.isChosenCategory.value = true
                        navController.navigate(route = NavigationItems.Search.route)
                    })
                }
            }
        } else {
            CategoryGrid(items = appViewModel.categories, columns = 2, navController = navController, appViewModel = appViewModel)
        }
    }
}

@Composable
fun CategoryGrid(
    items: List<Category>,
    columns: Int,
    navController: NavHostController,
    appViewModel: AppViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val itemWeight = 1f / columns
                rowItems.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .weight(itemWeight)
                    ) {
                        CategoryItem(
                            category = item,
                            isSelected = appViewModel.selectedCategory.value == item,
                            onClick = {
                                appViewModel.selectCategory(item)  // Update selected category
                                appViewModel.isChosenCategory.value = true
                                navController.navigate(route = NavigationItems.Search.route)
                            }
                        )
                    }
                }
                // Add spacers for any missing columns in the row
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(itemWeight))
                }
            }
        }
    }
}


@Composable
fun FavoritePlaceSection(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val tours = remember { mutableStateListOf<Tour>() }

    LaunchedEffect(Unit) {
        val loadedTours = FirestoreHelper.loadTours()
        val highRatedTours = loadedTours.filter { it.averageRating > 4.0 }
        tours.addAll(highRatedTours)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.favorite_place_text),
                style = Typography.headlineMedium
            )
            Text(
                text = stringResource(id = R.string.explore_text),
                style = Typography.labelSmall,
                color = BlackLight300,
                modifier = Modifier.clickable(onClick = {
                    navController.navigate(NavigationItems.WishList.route )
                })
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(tours) { tour ->
                TourCardInVertical(tour = tour, navController = navController)
            }
        }
    }
}



@Composable
fun PopularPackageSection(modifier: Modifier = Modifier, navController: NavHostController) {
    val packages = remember {
        mutableStateListOf(
            TourPackage("Kuta Resort", "https://example.com/kuta_resort.jpg", 250.0, 4.5,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
            TourPackage("Bali Beach", "https://example.com/bali_beach.jpg", 300.0, 4.7,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
            TourPackage("Ubud Retreat", "https://example.com/ubud_retreat.jpg", 350.0, 4.8,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
            TourPackage("Seminyak Villa", "https://example.com/seminyak_villa.jpg", 400.0, 4.6,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
        )
    }
    var isSeeAllClicked by remember{
        mutableStateOf(false)
    }
    var lazyColumnHeight by remember {
        mutableStateOf(0.dp)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.popular_package_text),
                style = Typography.headlineMedium
            )
            Text(
                text = if(!isSeeAllClicked) stringResource(id = R.string.see_all_text)
                else stringResource(id = R.string.collapse_text),
                style = Typography.labelSmall,
                color = BlackLight300,
                modifier = Modifier.clickable(onClick = {
                    isSeeAllClicked = !isSeeAllClicked
                })
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if(!isSeeAllClicked){
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lazyColumnHeight)
            ) {
                items(packages) { item ->
                    TourCardInHorizontal(
                        tour = item,
                        navController = navController,
                        onMeasured = {
                            lazyColumnHeight = it
                        }
                    )
                }
            }
        }
        else{
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(lazyColumnHeight * packages.size)
            ) {
                packages.forEach { item ->
                    TourCardInHorizontal(
                        tour = item,
                        navController = navController,
                        onMeasured = {
                            lazyColumnHeight = it
                        })
                }
            }
        }
    }
        Spacer(modifier = Modifier.height(40.dp))
}

