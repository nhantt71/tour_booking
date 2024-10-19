package com.project17.tourbooking.activities.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.project17.tourbooking.R
import com.project17.tourbooking.ui.theme.Typography
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource

@Composable
fun Explore(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Where is your favorite place to explore?",
            style = Typography.headlineLarge,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(50.dp))

        MultipleChoiceGrid()


        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFCD240),
                contentColor = Color.Black
            )
        ) {
            Text(
                "Next",
                style = Typography.headlineSmall,
            )
        }
    }
}

@Composable
fun MultipleChoiceGrid() {
    val options = listOf(
        Option("Beach", R.drawable.beach),
        Option("Mountain", R.drawable.mountain),
        Option("Forest", R.drawable.forest),
        Option("Ocean", R.drawable.ic_discovery),
        Option("Camping", R.drawable.camping),
        Option("Fishing", R.drawable.fishing)
    )

    val selectedOptions = remember { mutableStateListOf<Option>() }

    Column {
        for (row in options.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (option in row) {
                    OptionCard(option, selectedOptions.contains(option)) { isSelected ->
                        if (isSelected) {
                            selectedOptions.add(option)
                        } else {
                            selectedOptions.remove(option)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun OptionCard(option: Option, isSelected: Boolean, onSelectionChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .selectable(
                selected = isSelected,
                onClick = { onSelectionChange(!isSelected) }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color.Green else Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
        Image(
            painter = painterResource(id
                    = option.imageResId),
        contentDescription = option.text,
        modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = option.text)

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.End)
                    .offset(x = (-8).dp, y = (-8).dp), // Điều chỉnh vị trí lên trên phải
                tint = Color.Green // Màu xanh lá cây cho dấu tích
            )
        }
    }
    }
}


data class Option(val text: String, val imageResId: Int)