package com.example.maxx.presentation.screens.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.maxx.presentation.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetProScreen(
    navController: NavHostController,
    isDarkMode: Boolean
) {
    var selectedPlan by remember { mutableStateOf(PricingPlan.ANNUAL) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = CustomColors.backgroundColor(isDarkMode)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Header with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6200EE),
                                Color(0xFF3700B3)
                            )
                        )
                    )
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Upgrade to Pro",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unlock all premium features",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Features List
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProFeatureItem(
                    icon = Icons.Default.AllInclusive,
                    title = "Unlimited Proxies",
                    description = "Add and manage unlimited proxy servers"
                )
                
                ProFeatureItem(
                    icon = Icons.Default.Speed,
                    title = "Advanced Bandwidth Monitoring",
                    description = "Real-time bandwidth tracking and alerts"
                )
                
                ProFeatureItem(
                    icon = Icons.Default.Dns,
                    title = "Custom DNS Configuration",
                    description = "Set custom DNS servers for enhanced privacy"
                )
                
                ProFeatureItem(
                    icon = Icons.Default.Block,
                    title = "App Bypass List",
                    description = "Exclude specific apps from proxy"
                )
                
                ProFeatureItem(
                    icon = Icons.Default.SupportAgent,
                    title = "Priority Support",
                    description = "Get help from our team within 24 hours"
                )
                
                ProFeatureItem(
                    icon = Icons.Default.CloudOff,
                    title = "No Ads",
                    description = "Enjoy ad-free experience"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pricing Cards
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Choose Your Plan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                PricingCard(
                    plan = PricingPlan.MONTHLY,
                    isSelected = selectedPlan == PricingPlan.MONTHLY,
                    onClick = { selectedPlan = PricingPlan.MONTHLY }
                )
                
                PricingCard(
                    plan = PricingPlan.ANNUAL,
                    isSelected = selectedPlan == PricingPlan.ANNUAL,
                    onClick = { selectedPlan = PricingPlan.ANNUAL },
                    badge = "SAVE 40%"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subscribe Button
            Button(
                onClick = { /* TODO: Implement subscription */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Subscribe ${selectedPlan.displayName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Restore Purchase Link
            TextButton(
                onClick = { /* TODO: Restore purchase */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Restore Purchase",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms and Privacy
            Text(
                text = "By subscribing, you agree to our Terms of Service and Privacy Policy. " +
                        "Subscription automatically renews unless cancelled at least 24 hours before the end of the current period.",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun PricingCard(
    plan: PricingPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                Color(0xFF6200EE).copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF6200EE)) 
        else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF6200EE)
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    badge?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = Color(0xFF4CAF50)
                        ) {
                            Text(
                                text = it,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                
                Text(
                    text = plan.description,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = plan.price,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF6200EE) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = plan.period,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

enum class PricingPlan(
    val displayName: String,
    val price: String,
    val period: String,
    val description: String
) {
    MONTHLY(
        displayName = "Monthly",
        price = "$4.99",
        period = "/month",
        description = "Billed monthly"
    ),
    ANNUAL(
        displayName = "Annual",
        price = "$29.99",
        period = "/year",
        description = "Billed annually • $2.50/month"
    )
}

