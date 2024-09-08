package com.project17.tourbooking.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.navigates.NavigationGraph
import com.project17.tourbooking.navigates.VisibilityBottomBarScaffold
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.viewmodels.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TourBookingTheme {
                val navController = rememberNavController()
                var isBottomBarVisible by remember{
                    mutableStateOf(true)
                }
                val appViewModel: AppViewModel = viewModel()
               VisibilityBottomBarScaffold(
                   navController = navController,
                   isBottomBarVisible = isBottomBarVisible
               ) {paddingModifier ->
                   Column(modifier = Modifier
                       .fillMaxSize()
                       .then(paddingModifier)) {
                       NavigationGraph(
                           navController = navController,
                           onBottomBarVisibilityChanged = {
                               visible ->
                               isBottomBarVisible = visible
                           },
                           appViewModel = appViewModel
                       )
                   }
               }
            }
        }
    }
}
