package com.project17.tourbooking.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.constant.AppInfo.APP_ID
import com.project17.tourbooking.navigates.NavigationGraph
import com.project17.tourbooking.navigates.VisibilityBottomBarScaffold
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.viewmodels.AppViewModel
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            ZaloPaySDK.init(APP_ID, Environment.SANDBOX)
            Log.d("ZaloPayInit", "ZaloPay SDK initialized successfully")
        } catch (e: Exception) {
            Log.e("ZaloPayInitError", "Error initializing ZaloPay SDK: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }
}