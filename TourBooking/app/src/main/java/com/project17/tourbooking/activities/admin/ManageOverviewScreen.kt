package com.project17.tourbooking.activities.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.project17.tourbooking.activities.admin.ui.theme.Typography
import com.project17.tourbooking.utils.AuthState
import com.project17.tourbooking.utils.AuthViewModel

@Composable
fun ManageOverviewScreen(navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {}
            is AuthState.Error -> {
                val errorMessage = (authState as AuthState.Error).message
                navController.navigate("login")
            }
            is AuthState.Unauthenticated -> {
                navController.navigate("login")
            }
            else -> Unit
        }
    }

    // Optional: Show a loading indicator while the authentication state is being resolved
    if (authState.value is AuthState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Manage Overview", style = Typography.titleLarge)

            Button(onClick = { navController.navigate("manage_tours") }) {
                Text("Manage Tour")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("manage_tickets") }) {
                Text("Manage Ticket")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("manage_accounts") }) {
                Text("Manage Account")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("manage_categories") }) {
                Text("Manage Category")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { navController.navigate("statistic") }) {
                Text("Statistic Report")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}
