package com.example.maxx.presentation.screens.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.HttpAuthHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.maxx.domain.models.ProxyProfile
import com.example.maxx.presentation.theme.Brand400
import com.example.maxx.presentation.theme.Brand600
import com.example.maxx.presentation.theme.PoppinsFontFamily
import com.example.maxx.presentation.theme.isDark
import com.example.maxx.presentation.viewmodel.ProxyViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  Neo-brutalist offset block shadow + border
//  • Draws the filled shadow rect shifted right+bottom (equal offset)
//  • Draws a thin border around the component (all 4 sides of the border rect)
//    The shadow sits on right+bottom, top+left border stays hairline thin.
//  Apply BEFORE .clip(); caller adds end/bottom padding so shadow isn't clipped.
// ─────────────────────────────────────────────────────────────────────────────
private fun Modifier.neoBrutalistShadow(
    shadowColor: Color  = Color.Black,
    borderColor: Color  = Color.Black,
    cornerRadius: Dp    = 14.dp,
    offsetX: Dp         = 4.dp,
    offsetY: Dp         = 4.dp,
    strokeWidth: Dp     = 1.5.dp,
): Modifier = this.drawBehind {
    val r      = cornerRadius.toPx()
    val offX   = offsetX.toPx()
    val offY   = offsetY.toPx()
    val stroke = strokeWidth.toPx()

    // 1. Offset shadow block — right + bottom
    drawRoundRect(
        color        = shadowColor,
        topLeft      = Offset(offX, offY),
        size         = Size(size.width, size.height),
        cornerRadius = CornerRadius(r),
    )

    // 2. Border drawn on top — top+left side will be hidden by the white bg painted
    //    by .background() AFTER this drawBehind call, so only right+bottom are visible
    drawRoundRect(
        color        = borderColor,
        topLeft      = Offset(stroke / 2, stroke / 2),
        size         = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(r),
        style        = Stroke(width = stroke),
    )
}

private data class QuickLink(val label: String, val url: String, val dotColor: Color)

// ─────────────────────────────────────────────────────────────────────────────
//  BrowserScreen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    navController: NavHostController,
    proxyId: Int,
) {
    val vm: ProxyViewModel = hiltViewModel()
    val proxies by vm.allProxies.collectAsState()
    val proxy: ProxyProfile? = remember(proxies, proxyId) { proxies.find { it.id == proxyId } }

    // ── Proxy auth lifecycle — managed by ViewModel, not UI ──────────────
    val hasCredentials = remember(proxy) {
        vm.proxyHasCredentials(proxy)
    }
    DisposableEffect(proxy?.id) {
        vm.installBrowserAuth(proxy)
        onDispose { vm.clearBrowserAuth() }
    }

    val cs   = MaterialTheme.colorScheme
    val dark = MaterialTheme.isDark

    val accentPrimary      = if (dark) Brand400 else Brand600
    val bannerBg           = if (dark) Color(0xFF1E1B33) else Color(0xFFEBF0FF)
    val bannerText         = if (dark) Color(0xFF9B8FFF) else Color(0xFF4F6EF7)
    val encryptedBadgeBg   = if (dark) Color(0xFF2A2640) else Color(0xFFFFFFFF)
    val encryptedBadgeText = if (dark) Color(0xFFBBB8FF) else Color(0xFF4F6EF7)
    val urlBarBg           = cs.surface
    val checkIpBtnBg       = if (dark) Color(0xFF7B6FE8) else Brand600
    val privacyCardBorder  = if (dark) Color(0xFF2A2A48) else Color(0xFFDDE0FF)
    val iconBoxBg          = if (dark) Color(0xFF6B5FD0) else Brand600
    val dividerColor       = cs.outline.copy(alpha = 0.4f)

    var urlText         by rememberSaveable { mutableStateOf("https://proxy-search.io") }
    var isUrlFocused    by remember { mutableStateOf(false) }
    var webViewRef      by remember { mutableStateOf<WebView?>(null) }
    var isPageLoading   by remember { mutableStateOf(false) }
    var loadProgress    by remember { mutableStateOf(0) }
    var showBrowserMenu by remember { mutableStateOf(false) }
    var isWebViewMode   by remember { mutableStateOf(false) }

    val keyboard  = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboardManager.current

    val proxyProtocol = proxy?.protocol?.uppercase() ?: "HTTP/SOCKS5"
    val proxyIsp      = proxy?.isp ?: "Unknown ISP"
    val proxyCountry  = proxy?.country ?: "Unknown"
    val flagEmoji     = proxy?.countryCode?.toFlagEmoji() ?: "🌐"
    val latencyLabel  = "Stable (—ms)"

    Column(modifier = Modifier.fillMaxSize().background(cs.background)) {
        BrowserTopBar(
            onBack        = { navController.popBackStack() },
            onMenuClick   = { showBrowserMenu = true },
            showMenu      = showBrowserMenu,
            onDismissMenu = { showBrowserMenu = false },
            onRefresh     = { webViewRef?.reload(); showBrowserMenu = false },
            onCopyUrl     = { clipboard.setText(AnnotatedString(urlText)); showBrowserMenu = false },
            onShareUrl    = { showBrowserMenu = false },
            cs            = cs,
        )

        HorizontalDivider(color = cs.outline.copy(alpha = 0.25f), thickness = 0.5.dp)

        BrowserUrlBar(
            urlText        = urlText,
            isFocused      = isUrlFocused,
            isLoading      = isPageLoading,
            loadProgress   = loadProgress,
            urlBarBg       = urlBarBg,
            cs             = cs,
            onFocusChanged = { isUrlFocused = it },
            onUrlChange    = { urlText = it },
            onGo           = {
                keyboard?.hide()
                val finalUrl = normalizeUrl(urlText)
                urlText = finalUrl
                isWebViewMode = true
                webViewRef?.loadUrl(finalUrl)
            },
            onRefresh     = { if (isWebViewMode) webViewRef?.reload() },
            onLayersClick = { },
        )

        ProxyActiveBanner(
            protocol  = proxyProtocol,
            bannerBg  = bannerBg,
            bannerText = bannerText,
            badgeBg   = encryptedBadgeBg,
            badgeText = encryptedBadgeText,
        )

        Box(modifier = Modifier.weight(1f)) {
            if (isWebViewMode) {
                ProxyWebView(
                    url               = urlText,
                    proxy             = proxy,
                    hasCredentials    = hasCredentials,
                    onWebViewReady    = { webViewRef = it },
                    onPageStarted     = { url -> isPageLoading = true;  urlText = url ?: urlText },
                    onPageFinished    = { url -> isPageLoading = false; urlText = url ?: urlText },
                    onProgressChanged = { loadProgress = it },
                )
            } else {
                ProxyHomePage(
                    dark              = dark,
                    cs                = cs,
                    accentPrimary     = accentPrimary,
                    iconBoxBg         = iconBoxBg,
                    privacyCardBorder = privacyCardBorder,
                    dividerColor      = dividerColor,
                    proxyProtocol     = proxyProtocol,
                    proxyIsp          = proxyIsp,
                    proxyCountry      = proxyCountry,
                    latencyLabel      = latencyLabel,
                    onSearch          = { query ->
                        val finalUrl = normalizeUrl(query)
                        urlText = finalUrl
                        isWebViewMode = true
                    },
                    onQuickLinkClick  = { url ->
                        urlText = url
                        isWebViewMode = true
                    },
                )
            }
        }

        BrowserBottomBar(
            flagEmoji    = flagEmoji,
            proxyIp      = proxy?.ip ?: "—",
            checkIpBtnBg = checkIpBtnBg,
            cs           = cs,
            onCheckIp    = { urlText = "https://ip-api.com"; isWebViewMode = true },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Top App Bar
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserTopBar(
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    cs: ColorScheme,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onRefresh: () -> Unit,
    onCopyUrl: () -> Unit,
    onShareUrl: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(text = "Safe Browser", style = MaterialTheme.typography.titleLarge, color = cs.onSurface, fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = cs.onSurface) }
        },
        actions = {
            Box {
                IconButton(onClick = onMenuClick) { Icon(Icons.Default.MoreVert, "More options", tint = cs.onSurface) }
                DropdownMenu(expanded = showMenu, onDismissRequest = onDismissMenu) {
                    DropdownMenuItem(
                        text        = { Text("Refresh", style = MaterialTheme.typography.bodyMedium) },
                        onClick     = onRefresh,
                        leadingIcon = { Icon(Icons.Default.Refresh, null, tint = cs.onSurface) },
                    )
                    DropdownMenuItem(
                        text        = { Text("Copy URL", style = MaterialTheme.typography.bodyMedium) },
                        onClick     = onCopyUrl,
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = cs.onSurface) },
                    )
                    DropdownMenuItem(
                        text        = { Text("Share", style = MaterialTheme.typography.bodyMedium) },
                        onClick     = onShareUrl,
                        leadingIcon = { Icon(Icons.Default.Share, null, tint = cs.onSurface) },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  URL Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BrowserUrlBar(
    urlText: String,
    isFocused: Boolean,
    isLoading: Boolean,
    loadProgress: Int,
    urlBarBg: Color,
    cs: ColorScheme,
    onFocusChanged: (Boolean) -> Unit,
    onUrlChange: (String) -> Unit,
    onGo: () -> Unit,
    onRefresh: () -> Unit,
    onLayersClick: () -> Unit,
) {
    val dark = MaterialTheme.isDark
    val borderColor = when {
        isFocused -> cs.primary
        dark      -> cs.outline.copy(alpha = 0.55f)
        else      -> Color(0xFF111111).copy(alpha = 0.75f)
    }
    val borderWidth = if (isFocused) 1.5.dp else 1.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(cs.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(urlBarBg)
                .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Lock, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            BasicUrlTextField(
                value          = urlText,
                onValueChange  = onUrlChange,
                onFocusChanged = onFocusChanged,
                onGo           = onGo,
                modifier       = Modifier.weight(1f),
                cs             = cs,
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, "Refresh", tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onLayersClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Layers, "Tabs", tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }

        AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
            val progress by animateFloatAsState(
                targetValue   = loadProgress / 100f,
                animationSpec = tween(300),
                label         = "load_progress",
            )
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier.fillMaxWidth().padding(top = 4.dp).height(2.dp).clip(CircleShape),
                color      = cs.primary,
                trackColor = Color.Transparent,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Basic URL text field
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BasicUrlTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onGo: () -> Unit,
    modifier: Modifier = Modifier,
    cs: ColorScheme,
) {
    androidx.compose.foundation.text.BasicTextField(
        value           = value,
        onValueChange   = onValueChange,
        singleLine      = true,
        textStyle       = MaterialTheme.typography.bodyMedium.copy(color = cs.onSurface, fontFamily = PoppinsFontFamily),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onGo() }),
        modifier        = modifier.onFocusChanged { onFocusChanged(it.isFocused) },
        decorationBox   = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text("Search or type URL…", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                inner()
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Proxy Active Banner
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProxyActiveBanner(
    protocol: String,
    bannerBg: Color,
    bannerText: Color,
    badgeBg: Color,
    badgeText: Color,
) {
    Row(
        modifier              = Modifier.fillMaxWidth().background(bannerBg).padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text  = "PROXY ACTIVE: $protocol TUNNEL",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
            color = bannerText,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(badgeBg)
                .border(0.5.dp, bannerText.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 3.dp),
        ) {
            Text(
                text  = "Encrypted",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium),
                color = badgeText,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Proxy Home Page
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProxyHomePage(
    dark: Boolean,
    cs: ColorScheme,
    accentPrimary: Color,
    iconBoxBg: Color,
    privacyCardBorder: Color,
    dividerColor: Color,
    proxyProtocol: String,
    proxyIsp: String,
    proxyCountry: String,
    latencyLabel: String,
    onSearch: (String) -> Unit,
    onQuickLinkClick: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val keyboard    = LocalSoftwareKeyboardController.current

    val quickLinks = remember {
        listOf(
            QuickLink("Market Cap", "https://coinmarketcap.com", Color(0xFF4FC3F7)),
            QuickLink("Tech News",  "https://techcrunch.com",    Color(0xFFFFC107)),
            QuickLink("Reddit",     "https://reddit.com",        Color(0xFFFF7043)),
            QuickLink("GitHub",     "https://github.com",        Color(0xFF9E9E9E)),
        )
    }

    Column(
        modifier            = Modifier.fillMaxSize().verticalScroll(scrollState).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(28.dp))

        Box(
            modifier         = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(iconBoxBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(36.dp))
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text  = "ProxySearch",
            style = MaterialTheme.typography.titleLarge.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
            color = cs.onSurface,
        )

        Spacer(Modifier.height(16.dp))

        // Divider above search bar
        HorizontalDivider(color = cs.outline.copy(alpha = if (dark) 0.40f else 0.55f), thickness = 0.75.dp)

        Spacer(Modifier.height(18.dp))

        // ── Search bar (neo-brutalist offset shadow) ──────────────────────────
        var inPageQuery by rememberSaveable { mutableStateOf("") }

        val searchBorderColor   = if (dark) cs.outline.copy(alpha = 0.60f) else Color(0xFF111111)
        val searchShadowColor   = if (dark) Color(0xFF9B8FFF).copy(alpha = 0.85f) else Color.Black

        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp, end = 5.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .neoBrutalistShadow(
                        shadowColor  = searchShadowColor,
                        borderColor  = searchBorderColor,
                        cornerRadius = 14.dp,
                        offsetX      = 4.dp,
                        offsetY      = 4.dp,
                        strokeWidth  = if (dark) 1.dp else 1.5.dp,
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(cs.surface)
                    .height(50.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Search, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value           = inPageQuery,
                    onValueChange   = { inPageQuery = it },
                    singleLine      = true,
                    textStyle       = MaterialTheme.typography.bodyMedium.copy(color = cs.onSurface, fontFamily = PoppinsFontFamily),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboard?.hide()
                        if (inPageQuery.isNotBlank()) onSearch(inPageQuery)
                    }),
                    modifier        = Modifier.weight(1f),
                    decorationBox   = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (inPageQuery.isEmpty()) Text("Search or type URL…", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                            inner()
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Divider below search bar
        HorizontalDivider(color = cs.outline.copy(alpha = if (dark) 0.40f else 0.55f), thickness = 0.75.dp)

        Spacer(Modifier.height(24.dp))

        PrivacyInfoCard(cs = cs, cardBorder = privacyCardBorder, proxyCountry = proxyCountry, accentPrimary = accentPrimary, dark = dark)

        Spacer(Modifier.height(20.dp))

        QuickLinksGrid(links = quickLinks, cs = cs, onLinkClick = onQuickLinkClick)

        Spacer(Modifier.height(20.dp))

        RoutingDetailsCard(cs = cs, protocol = proxyProtocol, isp = proxyIsp, latencyLabel = latencyLabel, accentPrimary = accentPrimary, dividerColor = dividerColor)

        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Privacy Info Card  (neo-brutalist offset shadow)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PrivacyInfoCard(
    cs: ColorScheme,
    cardBorder: Color,
    proxyCountry: String,
    accentPrimary: Color,
    dark: Boolean,
) {
    val iconBg      = if (dark) Color(0xFF1E2040) else Color(0xFFEFF1FF)
    val borderColor = if (dark) cardBorder else Color(0xFF111111).copy(alpha = 0.82f)
    val strokeWidth = if (dark) 1.dp else 1.5.dp
    val shadowColor = if (dark) Color(0xFF9B8FFF).copy(alpha = 0.85f) else Color.Black
    val bg          = cs.surface

    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp, end = 5.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .neoBrutalistShadow(
                    shadowColor  = shadowColor,
                    borderColor  = borderColor,
                    cornerRadius = 16.dp,
                    offsetX      = 4.dp,
                    offsetY      = 4.dp,
                    strokeWidth  = strokeWidth,
                )
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier         = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Shield, null, tint = accentPrimary, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    "Ironclad Privacy",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold),
                    color = cs.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Your browsing data is routed through $proxyCountry. All trackers and scripts are isolated within this session.",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, lineHeight = 18.sp),
                    color = cs.onSurfaceVariant,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Quick Links Grid (2 × N)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickLinksGrid(links: List<QuickLink>, cs: ColorScheme, onLinkClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        links.chunked(2).forEach { rowLinks ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowLinks.forEach { link ->
                    QuickLinkItem(link = link, cs = cs, modifier = Modifier.weight(1f), onLinkClick = onLinkClick)
                }
                if (rowLinks.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickLinkItem(link: QuickLink, cs: ColorScheme, modifier: Modifier = Modifier, onLinkClick: (String) -> Unit) {
    Row(
        modifier          = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cs.surface)
            .border(1.dp, cs.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onLinkClick(link.url) }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(link.dotColor))
        Spacer(Modifier.width(8.dp))
        Text(
            text     = link.label,
            style    = MaterialTheme.typography.labelMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium),
            color    = cs.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Routing Details Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RoutingDetailsCard(cs: ColorScheme, protocol: String, isp: String, latencyLabel: String, accentPrimary: Color, dividerColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                "ROUTING DETAILS",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
                color = cs.onSurfaceVariant,
            )
            Text(
                latencyLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium),
                color = cs.onSurfaceVariant,
            )
        }
        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
        RoutingDetailRow("Protocol", protocol, accentPrimary, cs)
        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
        RoutingDetailRow("ISP", isp, cs.onSurface, cs)
    }
}

@Composable
private fun RoutingDetailRow(label: String, value: String, valueColor: Color, cs: ColorScheme) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily), color = cs.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold), color = valueColor)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Bottom Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BrowserBottomBar(flagEmoji: String, proxyIp: String, checkIpBtnBg: Color, cs: ColorScheme, onCheckIp: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = cs.outline.copy(alpha = 0.4f), thickness = 0.5.dp)
        Row(
            modifier          = Modifier.fillMaxWidth().background(cs.surface).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier         = Modifier.size(40.dp).clip(CircleShape).background(cs.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(flagEmoji, fontSize = 22.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, null, tint = cs.primary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "EXIT NODE IP",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, fontSize = 9.sp),
                        color = cs.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    proxyIp,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = cs.onSurface,
                )
            }

            // Neo-brutalist offset shadow — plain black, equal right & bottom offset
            Box(modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)) {
                Box(
                    modifier = Modifier.neoBrutalistShadow(
                        shadowColor  = Color.Black,
                        cornerRadius = 12.dp,
                        offsetX      = 4.dp,
                        offsetY      = 4.dp,
                    ),
                ) {
                    Button(
                        onClick        = onCheckIp,
                        shape          = RoundedCornerShape(12.dp),
                        colors         = ButtonDefaults.buttonColors(containerColor = checkIpBtnBg, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        elevation      = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Icon(Icons.Default.Bolt, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Check IP", style = MaterialTheme.typography.labelMedium.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Proxy-enabled WebView
// ─────────────────────────────────────────────────────────────────────────────
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ProxyWebView(
    url: String,
    proxy: ProxyProfile?,
    hasCredentials: Boolean,
    onWebViewReady: (WebView) -> Unit,
    onPageStarted: (String?) -> Unit,
    onPageFinished: (String?) -> Unit,
    onProgressChanged: (Int) -> Unit,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled    = true
                    domStorageEnabled    = true
                    loadWithOverviewMode = true
                    useWideViewPort      = true
                    setSupportZoom(true)
                    builtInZoomControls  = true
                    displayZoomControls  = false
                }
                if (proxy != null && androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.PROXY_OVERRIDE)) {
                    try {
                        val proxyConfig = androidx.webkit.ProxyConfig.Builder().addProxyRule("${proxy.ip}:${proxy.port}").build()
                        androidx.webkit.ProxyController.getInstance().setProxyOverride(proxyConfig, {}, {})
                    } catch (_: Exception) { }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) { super.onPageStarted(view, url, favicon); onPageStarted(url) }
                    override fun onPageFinished(view: WebView?, url: String?) { super.onPageFinished(view, url); onPageFinished(url) }
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

                    // ── HTTP/HTTPS proxy auth: respond to 407 challenge ──────
                    override fun onReceivedHttpAuthRequest(
                        view: WebView?,
                        handler: HttpAuthHandler?,
                        host: String?,
                        realm: String?
                    ) {
                        if (hasCredentials && handler != null && proxy != null) {
                            handler.proceed(proxy.username, proxy.password)
                        } else {
                            super.onReceivedHttpAuthRequest(view, handler, host, realm)
                        }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) { super.onProgressChanged(view, newProgress); onProgressChanged(newProgress) }
                }
                onWebViewReady(this)
                loadUrl(url)
            }
        },
        update   = { },
        modifier = Modifier.fillMaxSize(),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun normalizeUrl(input: String): String {
    val trimmed = input.trim()
    return when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.contains(".") && !trimmed.contains(" ")                  -> "https://$trimmed"
        else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
    }
}

private fun String.toFlagEmoji(): String {
    if (length != 2) return "🌐"
    val first  = Character.codePointAt(this, 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(this, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}

