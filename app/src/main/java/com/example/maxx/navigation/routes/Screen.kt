package com.example.maxx.navigation.routes

sealed class Screen(val route: String) {
    object Home : Screen("Home")

    object AddProxy : Screen("addProxy?proxyId={proxyId}") {
        fun createRoute(proxyId: Int = -1) = "addProxy?proxyId=$proxyId"
    }

    object Browser : Screen("browser?proxyId={proxyId}") {
        fun createRoute(proxyId: Int) = "browser?proxyId=$proxyId"
    }

    object SettingsHome : Screen("settingshome")
    object AppSettings : Screen("AppSettingsScreen")
    object ProxySettings : Screen("ProxySettingsScreen")

    object Logs : Screen("logs")

    object GetPro : Screen("getpro")
}


