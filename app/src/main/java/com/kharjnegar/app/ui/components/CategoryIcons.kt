package com.kharjnegar.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import com.kharjnegar.app.domain.Category

fun Category.icon(): ImageVector = when (this) {
    Category.FOOD -> Icons.Rounded.Restaurant
    Category.GROCERIES -> Icons.Rounded.ShoppingCart
    Category.TRANSPORT -> Icons.Rounded.DirectionsCar
    Category.SHOPPING -> Icons.Rounded.ShoppingBag
    Category.BILLS -> Icons.Rounded.ReceiptLong
    Category.TELECOM -> Icons.Rounded.PhoneAndroid
    Category.HEALTH -> Icons.Rounded.LocalHospital
    Category.ENTERTAINMENT -> Icons.Rounded.Movie
    Category.TRANSFER -> Icons.Rounded.SwapHoriz
    Category.SALARY -> Icons.Rounded.Payments
    Category.FEES -> Icons.Rounded.AccountBalance
    Category.OTHER -> Icons.Rounded.Category
}
