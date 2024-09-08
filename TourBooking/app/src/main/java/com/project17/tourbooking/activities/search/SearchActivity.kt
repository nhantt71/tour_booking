package com.project17.tourbooking.activities.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.activities.home.FavoritePlaceSection
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight100
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.AlertDialogUtil
import com.project17.tourbooking.utils.Category
import com.project17.tourbooking.utils.CategoryItem
import com.project17.tourbooking.utils.TourCardInHorizontal
import com.project17.tourbooking.utils.TourPackage
import com.project17.tourbooking.viewmodels.AppViewModel

class SearchActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TourBookingTheme {
                Scaffold( modifier = Modifier.fillMaxSize() ) {
                    SearchScreen()
                }
            }
        }
    }
}

const val MAX_HISTORY_ITEM_DISPLAY = 4

@Composable
fun SearchScreen(
    navController: NavHostController = rememberNavController(),
    appViewModel: AppViewModel = viewModel()
){
    val searchViewModel: SearchViewModel = viewModel()
    var isSearched by remember {
        searchViewModel.isSearched
    }
    val tours = remember {
        mutableStateListOf(
            TourPackage("Kuta Resort", R.drawable.kuta_resort, 250.0, 4.5),
            TourPackage("Kuta Resort", R.drawable.kuta_resort, 250.0, 4.5, true),
            TourPackage("Kuta Resort", R.drawable.kuta_resort, 250.0, 4.5, true),
            TourPackage("Kuta Resort", R.drawable.kuta_resort, 250.0, 4.5)
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier
            .height(16.dp)
            .fillMaxWidth())
        SearchHeaderSection(navController, searchViewModel, appViewModel)
        Spacer(modifier = Modifier
            .height(16.dp)
            .fillMaxWidth())
        SearchBarSection(searchViewModel)
        Spacer(modifier = Modifier
            .height(16.dp)
            .fillMaxWidth())
        if(appViewModel.isChosenCategory.value == true){
            SearchWithCategoryScreenContent(tourList = tours, navController = navController)
        }
        else{
            if(!isSearched){
                DefaultSearchScreenContent(navController = navController, viewModel = searchViewModel)
            }
            else{
                SearchedScreenContent(tourList = tours, navController = navController, searchViewModel = searchViewModel)
            }
        }
    }
}

@Composable
fun DefaultSearchScreenContent(navController: NavHostController, viewModel: SearchViewModel){
    Column(Modifier.fillMaxWidth()) {
        SearchHistorySection(viewModel)
        Spacer(modifier = Modifier
            .weight(1f)
            .fillMaxWidth())
        FavoritePlaceSection(navController = navController)
        Spacer(modifier = Modifier
            .height(16.dp)
            .fillMaxWidth())
    }
}

@Composable
fun SearchedScreenContent(tourList: SnapshotStateList<TourPackage>, navController: NavHostController, searchViewModel: SearchViewModel){
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.we_found_trip_text, tourList.size, searchViewModel.inputValue.value),
            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(tourList) { item ->
                TourCardInHorizontal(
                    tour = item,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun SearchWithCategoryScreenContent(
    tourList: SnapshotStateList<TourPackage>,
    navController: NavHostController
){
    val categories = listOf(
        Category(R.string.discovery_category_name_text, R.drawable.ic_discovery),
        Category(R.string.discovery_category_name_text, R.drawable.ic_discovery),
        Category(R.string.discovery_category_name_text, R.drawable.ic_discovery),
        Category(R.string.discovery_category_name_text, R.drawable.ic_discovery),
        Category(R.string.discovery_category_name_text, R.drawable.ic_discovery),
        Category(R.string.discovery_category_name_text, R.drawable.ic_discovery),
        Category(R.string.discovery_category_name_text, R.drawable.ic_discovery))

    var selectedIndex by remember {
        mutableStateOf(-1)
    }

    Column(){
        LazyRow {
            itemsIndexed(categories){index, category ->
                CategoryItem(
                    category = category,
                    isSelected = selectedIndex == index,
                    onClick = {
                        selectedIndex = if(selectedIndex == index) -1 else index
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(tourList) { item ->
                TourCardInHorizontal(
                    tour = item,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun SearchHeaderSection(
    navController: NavHostController,
    searchViewModel: SearchViewModel,
    appViewModel: AppViewModel
){
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = stringResource(id = R.string.back_button_description_text),
            tint = BlackDark900,
            modifier = Modifier.clickable(onClick = {
                searchViewModel.onBackButtonPress(navController)
            })
        )
        
        Text(
            text = stringResource(id = if(appViewModel.isChosenCategory.value == true) R.string.search_with_category_text else R.string.search_screen_name_text),
            style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = BlackDark900
        )

        if(searchViewModel.isSearched.value || appViewModel.isChosenCategory.value == true){
            Icon(
                painter = painterResource(id = R.drawable.ic_search_filter),
                contentDescription = stringResource(id = R.string.search_filter_icon_description_text),
                tint = BlackDark900,
                modifier = Modifier.clickable(
                    onClick = {
                        navController.navigate(NavigationItems.SearchFilter.route)
                    }
                )
            )
        }
        else{
            Spacer(modifier = Modifier)
        }
    }
}

@Composable
fun SearchBarSection(searchViewModel: SearchViewModel = viewModel()){
    TextField(
        value = searchViewModel.inputValue.value,
        onValueChange = {
            searchViewModel.inputValue.value = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp)
            .clip(RoundedCornerShape(16.dp)),
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.search_icon_description_text),
                tint = BlackDark900,
                modifier = Modifier
                    .clickable(onClick = {
                        if(searchViewModel.inputValue.value != ""){
                            addHistoryItemIfNotExistInList(searchViewModel)
                            searchViewModel.isSearched.value = true
                        }
                    })
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
        ),
        keyboardActions = KeyboardActions(onSearch = {
            if(searchViewModel.inputValue.value != ""){
                addHistoryItemIfNotExistInList(searchViewModel)
            }
        }),
        maxLines = 1
    )
}

fun addHistoryItemIfNotExistInList(viewModel: SearchViewModel){
    if(!viewModel.historyItems.any(viewModel.inputValue.value::equals)){
        viewModel.addHistoryItem(viewModel.inputValue.value)
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SearchHistorySection(
    viewModel: SearchViewModel,
){
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        var itemHeight by remember { mutableStateOf(0.dp) }

        Column {
            Text(
                text = stringResource(id = R.string.last_search_text),
                style = Typography.titleLarge,
                color = BlackDark900
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = (itemHeight * MAX_HISTORY_ITEM_DISPLAY) + 16.dp)
            ) {
                itemsIndexed(viewModel.historyItems) { index, item ->
                    SearchHistoryItem(
                        historyContent = item,
                        viewModel = viewModel,
                        onMeasured = {
                            itemHeight = it
                        },
                        onDeleteClicked = {
                            viewModel.deleteHistoryItem(index = index)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchHistoryItem(
    historyContent: String,
    viewModel: SearchViewModel,
    onMeasured: (Dp) -> Unit = {},
    onDeleteClicked: () -> Unit = {}
){
    val density = LocalDensity.current
    var isDeleteClicked by remember {
        mutableStateOf(false)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val height = with(density) { coordinates.size.height.toDp() }
                onMeasured(height)
            }
            .clickable(onClick = {
                viewModel.inputValue.value = historyContent
                viewModel.isSearched.value = true
                addHistoryItemIfNotExistInList(viewModel)
            }),
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            painter = painterResource(id = R.drawable.ic_search_history),
            contentDescription = stringResource(
                id = R.string.search_history_icon_description_text),
            tint = BlackLight300
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = historyContent,
            style = Typography.titleMedium,
            color = BlackDark900
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = stringResource(id = R.string.delete_icon_description_text),
            tint = BlackDark900,
            modifier = Modifier.clickable(onClick = {
                isDeleteClicked = true
            })
        )

        if(isDeleteClicked){
            AlertDialogUtil(
                isDialogVisible = true,
                onDismiss = { isDeleteClicked = !isDeleteClicked },
                onConfirm = {
                    isDeleteClicked = !isDeleteClicked
                    onDeleteClicked()
                            },
                title = R.string.delete_history_item_alert_dialog_title_text,
                message = R.string.delete_history_item_alert_dialog_message_text,
                confirmButtonText = R.string.delete_history_item_alert_dialog_confirm_button_text,
                dismissButtonText = R.string.delete_history_item_alert_dialog_dismiss_button_text
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    TourBookingTheme { 
        SearchScreen()
    }
}