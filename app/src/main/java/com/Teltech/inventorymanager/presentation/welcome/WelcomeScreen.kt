package com.Teltech.inventorymanager.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.padding(top = 80.dp)
        ) {
            Text(
                text = "ELITE",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                lineHeight = 62.sp,
                letterSpacing = (-2).sp
            )
            Text(
                text = "INVENTORY",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFA594F9),
                lineHeight = 62.sp,
                letterSpacing = (-2).sp
            )
            Text(
                text = "FOR",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                lineHeight = 62.sp,
                letterSpacing = (-2).sp
            )
            Text(
                text = "TECH",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                lineHeight = 62.sp,
                letterSpacing = (-2).sp
            )
            Text(
                text = "SERVICES",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                lineHeight = 62.sp,
                letterSpacing = (-2).sp
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Built to simplify repair workflows and give you complete visibility into your technician inventory.",
                fontSize = 15.sp,
                color = Color.Gray,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }

        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA594F9)
            )
        ) {
            Text(
                text = "Explore Inventory",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
