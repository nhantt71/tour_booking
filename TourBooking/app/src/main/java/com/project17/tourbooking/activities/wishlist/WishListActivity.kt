import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.project17.tourbooking.R
import com.project17.tourbooking.activities.search.SearchBarSection
import com.project17.tourbooking.activities.search.SearchViewModel
import com.project17.tourbooking.ui.theme.Typography
import com.project17.tourbooking.utils.AuthState
import com.project17.tourbooking.utils.AuthViewModel
import com.project17.tourbooking.utils.LoginPrompt

data class WishlistItem(
    val id: Int,
    val imageUrl: String,
    val name: String,
    val price: String,
    val rating: Double,
    val description: String
)

@Composable
fun WishListScreen(searchViewModel: SearchViewModel = viewModel(), navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val wishlistItems =
        remember {
            listOf(
                WishlistItem(
                    1,
                    "https://hips.hearstapps.com/hmg-prod/images/maldive-dove-andare-1673264264.jpg", // Thay thế bằng URL hình ảnh thực tế
                    "Kuta Beach",
                    "$245.00",
                    4.8,
                    "Bali is an island in Indonesia known for its verdant volcanic mountains, iconic rice paddies, beaches and coral reefs."
                ),
                WishlistItem(
                    1,
                    "https://hips.hearstapps.com/hmg-prod/images/maldive-dove-andare-1673264264.jpg", // Thay thế bằng URL hình ảnh thực tế
                    "Kuta Beach",
                    "$245.00",
                    4.8,
                    "Bali is an island in Indonesia known for its verdant volcanic mountains, iconic rice paddies, beaches and coral reefs."
                ),
                WishlistItem(
                    1,
                    "https://hips.hearstapps.com/hmg-prod/images/maldive-dove-andare-1673264264.jpg", // Thay thế bằng URL hình ảnh thực tế
                    "Kuta Beach",
                    "$245.00",
                    4.8,
                    "Bali is an island in Indonesia known for its verdant volcanic mountains, iconic rice paddies, beaches and coral reefs."
                ),
            )
        }

    if (authState.value is AuthState.Unauthenticated) {
        LoginPrompt(navController)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "List Your Trip",
                style = Typography.headlineLarge,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchBarSection(searchViewModel)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wishlistItems) { item ->
                    WishlistItem(item, onRemoveFromWishlist = { /* Xử lý bỏ yêu thích */ }) {
                        navController.navigate("login")
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistItem(item: WishlistItem, onRemoveFromWishlist: (Int) -> Unit, onClick: () -> Unit) {
    var isFavorite by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(
                    data = item.imageUrl,
                builder = {
                    crossfade(true)
                    placeholder(R.drawable.kuta_resort)
                    error(R.drawable.kuta_resort)
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.name, style = Typography.titleLarge)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        isFavorite = !isFavorite
                        if (!isFavorite) {
                            onRemoveFromWishlist(item.id)
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from wishlist" else "Add to wishlist",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = item.price, style = Typography.bodyLarge, color = Color(0xFFFFA500))
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    for (i in 1..5) {
                        Icon(
                            painter = painterResource(id = if (i <= item.rating.toInt()) R.drawable.ic_yellow_star else R.drawable.ic_white_star),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (i <= item.rating.toInt()) Color.Yellow else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.rating.toString(), style = Typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = item.description, style = Typography.bodySmall, color = Color.LightGray)
            }
        }
    }
}