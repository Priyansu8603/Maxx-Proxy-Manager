package com.example.maxx.presentation.screens.logs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class LogLevel {
    ALL, SUCCESS, ERROR, WARNING, INFO;

    val icon: ImageVector
        get() = when (this) {
            ALL     -> Icons.Default.Schedule
            SUCCESS -> Icons.Default.CheckCircle
            ERROR   -> Icons.Default.Error
            WARNING -> Icons.Default.Warning
            INFO    -> Icons.Default.Info
        }

    val displayColor: Color
        get() = when (this) {
            ALL     -> Color(0xFF9E9E9E)
            SUCCESS -> Color(0xFF4CAF50)
            ERROR   -> Color(0xFFE53935)
            WARNING -> Color(0xFFF57C00)
            INFO    -> Color(0xFF1E88E5)
        }
}

