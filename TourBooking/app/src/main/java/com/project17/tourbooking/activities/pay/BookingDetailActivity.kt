package com.project17.tourbooking.activities.pay

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project17.tourbooking.R
import com.project17.tourbooking.api.CreateOrder
import com.project17.tourbooking.constant.CurrencyRate
import com.project17.tourbooking.models.Bill
import com.project17.tourbooking.models.BillDetail
import com.project17.tourbooking.models.Ticket
import com.project17.tourbooking.models.Tour
import com.project17.tourbooking.navigates.NavigationItems
import com.project17.tourbooking.ui.theme.BlackDark900
import com.project17.tourbooking.ui.theme.BlackLight200
import com.project17.tourbooking.ui.theme.BlackLight400
import com.project17.tourbooking.ui.theme.BlackWhite0
import com.project17.tourbooking.ui.theme.BrandDefault500
import com.project17.tourbooking.ui.theme.ErrorDefault500
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.AuthState
import com.project17.tourbooking.utils.AuthViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener


@OptIn(DelicateCoroutinesApi::class)
@Composable
fun BookingDetailScreen(
    navController: NavHostController, tourId: String,
    modifier: Modifier = Modifier, authViewModel: AuthViewModel = AuthViewModel()
) {
    val tour = remember { mutableStateOf<Tour?>(null) }
    var customerName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    val ticket = remember { mutableStateListOf<Ticket?>() }
    var ticketDocumentId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val authState = authViewModel.authState.observeAsState()
    var slotQuantity by remember { mutableIntStateOf(0) }

    val success = remember { mutableStateOf(false) }
    var billid1 = remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()


    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                val currentUser = authViewModel.auth.currentUser
                val currentEmail = currentUser?.email

                if (currentEmail != null) {
                    FirestoreHelper.getCustomerByEmail(currentEmail) { customer ->
                        customer?.let {
                            customerName = it.fullName
                            contactInfo = it.email
                        }
                    }
                }

                tour.value = FirestoreHelper.getTourById(tourId)

                slotQuantity = tour.value?.slotQuantity ?: 0

                val loadedTickets = FirestoreHelper.loadTicketForTour(tourId)
                ticket.clear()
                ticket.addAll(loadedTickets)

                val firstTicket = ticket.firstOrNull()
                if (firstTicket != null) {
                    FirestoreHelper.getTicketDocumentId(firstTicket.tourId) { documentId ->
                        if (documentId != null) {
                            ticketDocumentId = documentId
                            Log.d("BookingDetailScreen", "Ticket Document ID: $ticketDocumentId")
                        }
                    }
                }
                Log.d("BookingDetailScreen", "Slot quantity: $slotQuantity")

            }

            is AuthState.Error -> {
                val errorMessage = (authState.value as AuthState.Error).message
                Log.e("BookingDetailScreen", errorMessage)
            }

            is AuthState.Unauthenticated -> {
                navController.navigate("login")
            }

            else -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth()
            )

            NavBarSection(navController)
            Spacer(
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxWidth()
            )
            BookingForm(
                customerName = customerName,
                contactInfo = contactInfo,
                onCustomerNameChanged = { customerName = it },
                onContactInfoChanged = { contactInfo = it },
                onQuantityChanged = { quantity = it },
                slotQuantity = slotQuantity
            )
        }

        FooterSection(
            price = ticket.firstOrNull()?.price ?: 0.0,
            quantity = quantity,
            onConfirmClick = {
                ZaloPaySDK.init(554, Environment.SANDBOX)
                val orderApi = CreateOrder()
                Log.d("asd", "ok")
                scope.launch {
                    try {
                        val data = orderApi.createOrder(
                            ((ticket.firstOrNull()?.price
                                ?: 0.0).toInt() * quantity * CurrencyRate.VND).toString()
                        )
                        val code = data.getString("return_code")
                        if (code == "1") {
                            val token = data.getString("zp_trans_token")
                            ZaloPaySDK.getInstance().payOrder(
                                context as Activity,
                                token,
                                "demozpdk://app",
                                object : PayOrderListener {
                                    override fun onPaymentCanceled(
                                        zpTransToken: String?,
                                        appTransID: String?
                                    ) {
                                        //Xử lý logic khi người dùng từ chối thanh toán
                                    }

                                    override fun onPaymentError(
                                        zaloPayError: ZaloPayError?,
                                        zpTransToken: String?,
                                        appTransID: String?
                                    ) {
                                        if (zaloPayError == ZaloPayError.PAYMENT_APP_NOT_FOUND) {
                                            ZaloPaySDK.getInstance().navigateToZaloOnStore(context)
                                            ZaloPaySDK.getInstance()
                                                .navigateToZaloPayOnStore(context)
                                        }
                                    }

                                    override fun onPaymentSucceeded(
                                        transactionId: String,
                                        transToken: String,
                                        appTransID: String?
                                    ) {
                                        val bill = Bill(
                                            totalAmount = quantity * (ticket.firstOrNull()?.price
                                                ?: 0.0),
                                            email = contactInfo
                                        )
                                        scope.launch {
                                            try {
                                                val billId = FirestoreHelper.addBill(bill)
                                                Log.d("BookingDetailScreen", "Bill ID: $billId")

                                                if (billId != null) {
                                                    val billDetail = ticketDocumentId?.let {
                                                        BillDetail(
                                                            ticketId = it,
                                                            quantity = quantity,
                                                            billId = billId
                                                        )
                                                    }

                                                    billid1.value = billId

                                                    if (billDetail != null) {
                                                        val isSuccess =
                                                            FirestoreHelper.addBillDetail(billDetail)
                                                        success.value = true
                                                        withContext(Dispatchers.Main) {
                                                            if (isSuccess) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Thanh toán và lưu thông tin thành công!",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                                navController.navigate(NavigationItems.BookingSuccess.route + "/${billid1.value}")
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Lỗi khi lưu BillDetail",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            context,
                                                            "Lỗi không có BillID",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("ZaloPayError", "Exception: ${e.message}")
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun NavBarSection(
    navController: NavController = rememberNavController()
) {
    Row(
        Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "",
            tint = BlackDark900,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { navController.popBackStack() }
        )
        Text(
            text = stringResource(id = R.string.title_activity_detail_booking_text),
            style = Typography.titleLarge,
            color = BlackDark900,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BookingForm(
    customerName: String,
    contactInfo: String,
    onCustomerNameChanged: (String) -> Unit,
    onContactInfoChanged: (String) -> Unit,
    onQuantityChanged: (Int) -> Unit,
    slotQuantity: Int
) {
    var showErrorName by remember { mutableStateOf(false) }
    var showErrorContact by remember { mutableStateOf(false) }
    var errorMessageName by remember { mutableStateOf<String?>(null) }
    var errorMessageContact by remember { mutableStateOf<String?>(null) }

    val validSlotQuantity = if (slotQuantity > 0) slotQuantity else 1

    Column {
        InformationTextField(
            label = R.string.person_responsible_text,
            value = customerName,
            onValueChange = { newValue ->
                onCustomerNameChanged(newValue)
                if (showErrorName) {
                    showErrorName = false
                }
            },
            errorMessage = errorMessageName,
            showError = showErrorName
        )

        Spacer(modifier = Modifier.height(16.dp))
        InformationTextField(
            label = R.string.contact_info_text,
            value = contactInfo,
            onValueChange = { newValue ->
                onContactInfoChanged(newValue)
                if (showErrorContact) {
                    showErrorContact = false
                }
            },
            errorMessage = errorMessageContact,
            showError = showErrorContact,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        val dropdownList =
            (1..validSlotQuantity).map { "$it " + stringResource(id = R.string.member_text) }
        DropdownMenuWithOptions(
            options = dropdownList,
            onOptionSelected = { newValue -> onQuantityChanged(newValue + 1) }
        )
    }
}


@Composable
fun InformationTextField(
    label: Int,
    value: String = "",
    onValueChange: (String) -> Unit,
    errorMessage: String? = null,
    showError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {


    Column {
        OutlinedTextField(
            value = value,
            readOnly = true,
            onValueChange = { newValue -> onValueChange(newValue) },
            label = { Text(stringResource(id = label)) },
            singleLine = true,
            isError = showError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (showError) Color.Red else BlackLight200,
                focusedBorderColor = if (showError) Color.Red else BlackDark900,
                focusedLabelColor = if (showError) Color.Red else BlackDark900
            ),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            )
        )
        if (showError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = Typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuWithOptions(options: List<String>, onOptionSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options.getOrElse(0) { "No options available" }) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            singleLine = true,
            label = { Text(stringResource(id = R.string.member_text)) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(id = R.string.image_description_text),
                    Modifier.clickable { expanded = expanded }
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BlackLight200,
                focusedBorderColor = BlackDark900,
                focusedLabelColor = BlackDark900
            ),
            shape = RoundedCornerShape(16.dp),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(BlackWhite0)
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(text = "No options available") },
                    onClick = { expanded = false },
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                options.forEachIndexed { index, selectionOption ->
                    DropdownMenuItem(
                        text = { Text(text = selectionOption) },
                        onClick = {
                            selectedOptionText = selectionOption
                            expanded = false
                            onOptionSelected(index)
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun FooterSection(
    price: Double,
    quantity: Int,
    onConfirmClick: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BlackWhite0),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.total_text),
                    color = BlackLight400,
                    style = Typography.bodyLarge
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = String.format("$%.2f", (price * quantity)),
                    style = Typography.headlineMedium,
                    color = ErrorDefault500
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onConfirmClick() },
                colors = ButtonColors(BrandDefault500, BlackDark900, BrandDefault500, BlackDark900),
                modifier = Modifier
                    .width(150.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.confirm_text),
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}