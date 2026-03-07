//package com.example.maxx.presentation.components
//
//import android.content.Intent
//import androidx.activity.result.ActivityResultLauncher
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.AsyncImage
//import com.example.maxx.data.repository.ProxyRepository
//import com.example.maxx.domain.models.ProxyProfile
//import com.example.maxx.presentation.viewmodel.ProxyViewModel
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ProxyBottomSheet(
//    selectedProxy: MutableState<ProxyProfile?>,
//    proxyViewModel: ProxyViewModel,
//    connectionState: ProxyRepository.ConnectionState,
//    vpnPermissionLauncher: ActivityResultLauncher<Intent>,
//    coroutineScope: CoroutineScope = rememberCoroutineScope()
//) {
//    val sheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = false
//    )
//    val proxy = selectedProxy.value ?: return
//    val colorScheme = MaterialTheme.colorScheme
//
//    // Automatically expand when proxy is selected
//    LaunchedEffect(selectedProxy.value) {
//        if (selectedProxy.value != null) {
//            sheetState.show()
//        }
//    }
//
//    ModalBottomSheet(
//        onDismissRequest = { selectedProxy.value = null },
//        sheetState = sheetState,
//        containerColor = colorScheme.surfaceContainerHigh,
//        tonalElevation = 8.dp,
//        windowInsets = WindowInsets(0),
//        dragHandle = {
//            Box(
//                modifier = Modifier
//                    .padding(vertical = 12.dp)
//                    .width(40.dp)
//                    .height(4.dp)
//                    .background(
//                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
//                        shape = RoundedCornerShape(2.dp)
//                    )
//            )
//        }
//    ) {
//        // Rest of the content remains the same...
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 8.dp)
//                .navigationBarsPadding()
//        ) {
//            // Proxy information row
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    AsyncImage(
//                        model = proxy.flagUrl ?: "https://flagcdn.com/w80/un.png",
//                        contentDescription = "Country Flag",
//                        modifier = Modifier
//                            .size(42.dp)
//                            .padding(end = 12.dp)
//                            .clip(RoundedCornerShape(4.dp))
//                    )
//
//                    Column {
//                        Text(
//                            text = proxy.name,
//                            color = colorScheme.onSurface,
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 18.sp
//                        )
//                        Text(
//                            text = "${proxy.ip}:${proxy.port} | ${proxy.protocol.uppercase()}",
//                            color = colorScheme.onSurfaceVariant,
//                            fontSize = 12.sp
//                        )
//                    }
//                }
//
//                TextButton(
//                    onClick = { selectedProxy.value = null },
//                    colors = ButtonDefaults.textButtonColors(
//                        contentColor = colorScheme.primary
//                    )
//                ) {
//                    Text(
//                        "Change",
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 16.sp
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Connection button
//            val isConnected = connectionState is ProxyRepository.ConnectionState.Connected
//            val buttonColors = if (isConnected) {
//                ButtonDefaults.buttonColors(
//                    containerColor = colorScheme.errorContainer,
//                    contentColor = colorScheme.onErrorContainer
//                )
//            } else {
//                ButtonDefaults.buttonColors(
//                    containerColor = colorScheme.primary,
//                    contentColor = colorScheme.onPrimary
//                )
//            }
//
//            Button(
//                onClick = {
//                    coroutineScope.launch {
//                        if (isConnected) {
//                            proxyViewModel.disconnect()
//                        } else {
//                            val intent = proxyViewModel.getVpnPrepareIntent()
//                            if (intent != null) {
//                                vpnPermissionLauncher.launch(intent)
//                            } else {
//                                proxyViewModel.connect(proxy)
//                            }
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(52.dp),
//                colors = buttonColors,
//                shape = RoundedCornerShape(12.dp),
//                elevation = ButtonDefaults.buttonElevation(
//                    defaultElevation = 0.dp,
//                    pressedElevation = 0.dp
//                )
//            ) {
//                val buttonText = when (connectionState) {
//                    ProxyRepository.ConnectionState.Connected -> "Disconnect"
//                    ProxyRepository.ConnectionState.Connecting -> "Connecting..."
//                    ProxyRepository.ConnectionState.Disconnected -> "Connect"
//                    ProxyRepository.ConnectionState.Failed -> "Reconnect"
//                    ProxyRepository.ConnectionState.Idle -> "Connect"
//                    is ProxyRepository.ConnectionState.Error -> "Error"
//                }
//                Text(
//                    text = buttonText,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 18.sp
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//    }
//}