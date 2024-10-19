package com.project17.tourbooking.activities.pay

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.project17.tourbooking.R
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight300
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.InformationDefault500
import com.project17.tourbooking.ui.theme.InformationLight400
import com.project17.tourbooking.ui.theme.SuccessDefault500
import com.project17.tourbooking.ui.theme.TourBookingTheme
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.AuthState
import com.project17.tourbooking.utils.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BookingSuccessScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    billId: String,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    var name by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var totalAmount by remember { mutableStateOf(0) }
    var quantity by remember { mutableStateOf(0) }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                val currentUser = authViewModel.auth.currentUser
                val currentEmail = currentUser?.email

                email = currentEmail

                if (currentEmail != null) {
                    FirestoreHelper.getCustomerByEmail(currentEmail) { customer ->
                        customer?.let {
                            name = it.fullName
                        }
                    }

                    // Fetch Bill and BillDetail information
                    FirestoreHelper.getBillById(billId) { bill ->
                        bill?.let {
                            totalAmount = it.totalAmount.toInt()
                        }
                    }

                    FirestoreHelper.getBillDetailByBillId(billId) { billDetail ->
                        billDetail?.let {
                            quantity = it.quantity
                        }
                    }
                }
            }

            is AuthState.Error -> {
                val errorMessage = (authState.value as AuthState.Error).message
                Log.e("HeaderSection", errorMessage)
            }

            is AuthState.Unauthenticated -> {
                navController.navigate(NavigationItems.Login.route)
            }

            else -> Unit
        }
    }

    // UI content as before
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(navController)
            OrderPlacedSuccessSection()
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(BlackLight300)
            )
            Spacer(modifier = Modifier.height(16.dp))
            name?.let {
                email?.let { emailValue ->
                    OrderPlacedInformationSection(
                        personResponsibleName = it,
                        contactInfo = emailValue,
                        placedOrderTime = Date(),
                        quantity = quantity,
                        totalPrice = totalAmount
                    )
                }
            }
        }

        FooterSection(
            onBackToHomeClick = { backToHomeAndPopAllInBackStack(navController) },
            onViewYourTripClick = { navController.navigate(NavigationItems.TripBookedDetail.route + "/$billId") },
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}


@Composable
fun TourInformationSection(
    image: String,
    name: String,
    destination: String,
    startDate: String,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Image(
            painter = rememberImagePainter(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            style = Typography.headlineMedium,
            color = BlackDark900,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = destination,
            style = Typography.bodyMedium,
            color = BlackLight300,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start Date: $startDate",
            style = Typography.bodyMedium,
            color = BlackLight300,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HeaderSection(navController: NavHostController = rememberNavController()){
    Row(Modifier.fillMaxWidth()){
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_home),
            contentDescription = stringResource(id = R.string.image_description_text),
            modifier = Modifier
                .height(50.dp)
                .clickable { backToHomeAndPopAllInBackStack(navController) }
        )
    }
}

@Composable
fun OrderPlacedSuccessSection(){
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.order_placed_successfully),
            contentDescription = stringResource(id = R.string.image_description_text),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.order_placed_successfully_text),
            style = Typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = SuccessDefault500
        )
    }
}

@Composable
fun OrderPlacedInformationSection(
    personResponsibleName: String,
    contactInfo: String,
    placedOrderTime: Date,
    quantity: Int,
    totalPrice: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.order_placed_information_text),
            style = Typography.headlineMedium,
            color = InformationDefault500,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(16.dp))
        PlaceOrderInformationLine(R.string.person_responsible_name_text, personResponsibleName)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.contact_information_text, contactInfo)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val formattedOrderTime = dateFormat.format(placedOrderTime)
        PlaceOrderInformationLine(R.string.placed_order_time_text, formattedOrderTime)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.quantity_of_ticket_text, quantity.toString())
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(8.dp))
        PlaceOrderInformationLine(R.string.total_text, totalPrice.toString())
    }
}

@Composable
fun PlaceOrderInformationLine(title: Int, content: String){
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = title),
            style = Typography.titleLarge,
            color = InformationLight400
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = content,
            style = Typography.bodyLarge,
            color = BlackDark900
        )
    }
}

fun backToHomeAndPopAllInBackStack(navController: NavHostController) {
    navController.popBackStack(navController.graph.startDestinationId, inclusive = false)
}

@Composable
fun FooterSection(
    modifier: Modifier = Modifier,
    onBackToHomeClick: () -> Unit = {},
    onViewYourTripClick: () -> Unit = {},
){
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Button(
            onClick = { onViewYourTripClick() },
            colors = ButtonDefaults.buttonColors(
                containerColor = BlackWhite0,
                contentColor = BlackDark900,

            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrandDefault500, RoundedCornerShape(32.dp))
        ){
            Text(
                text = stringResource(id = R.string.view_your_trip_text),
                style = Typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onBackToHomeClick() },
            colors = ButtonColors(BrandDefault500, BlackDark900, BrandDefault500, BlackDark900),
            modifier = Modifier
                .fillMaxWidth()
        ){
            Text(
                text = stringResource(id = R.string.back_to_home_text),
                style = Typography.titleLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

