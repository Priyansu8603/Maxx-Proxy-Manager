package com.example.maxx.presentation.screens.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.maxx.domain.models.ProxyProfile
import com.example.maxx.presentation.screens.testing.ProxyTestResultBottomSheet
import com.example.maxx.presentation.theme.extraColors
import com.example.maxx.presentation.viewmodel.ProxyViewModel
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
//  UI state  — hoisted into a single data class (stable, no recomposition leak)
// ─────────────────────────────────────────────────────────────────────────────
private data class HomeUiState(
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Int>   = emptySet(),
    val showTestSheet: Boolean  = false,
    val testingProxyId: Int?    = null,
    // ── The proxy the user last tapped — shown in Active Connection banner ──
    val activeProxyId: Int?     = null,
)

// ─────────────────────────────────────────────────────────────────────────────
//  Home / Dashboard Screen
//  ▸ No Scaffold (parent AppNavGraph owns the one Scaffold + BottomBar)
//  ▸ No isDarkMode param — reads MaterialTheme.colorScheme directly
//  ▸ No hardcoded Color(...) literals — all colours from design tokens
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    toggleTheme: () -> Unit,
) {
    val vm           = hiltViewModel<ProxyViewModel>()
    val haptic       = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    // ── observe ───────────────────────────────────────────────
    val proxies         by vm.allProxies.collectAsState()
    val proxyState      by vm.proxyState.collectAsState()
    val testResult      by vm.testResult.collectAsState()
    val lastTestResults by vm.lastTestResults.collectAsState()
    val pendingDelete   by vm.pendingDeleteProxy.collectAsState()
    val undoSecondsLeft by vm.undoSecondsLeft.collectAsState()

    // ── local ui state ────────────────────────────────────────
    var ui by remember { mutableStateOf(HomeUiState()) }

    // ── derived: activeProxy = the one the user tapped, or favourite/first on first load ──
    val activeProxy = remember(proxies, ui.activeProxyId) {
        when {
            ui.activeProxyId != null -> proxies.find { it.id == ui.activeProxyId }
            else                     -> proxies.firstOrNull { it.isFavorite } ?: proxies.firstOrNull()
        }
    }

    // ── auto-set activeProxyId once proxies load (only if not yet set) ──
    LaunchedEffect(proxies) {
        if (ui.activeProxyId == null && proxies.isNotEmpty()) {
            val default = proxies.firstOrNull { it.isFavorite } ?: proxies.first()
            ui = ui.copy(activeProxyId = default.id)
        }
    }

    // ── theme tokens ──────────────────────────────────────────
    val cs    = MaterialTheme.colorScheme
    val extra = MaterialTheme.extraColors

    // ── auto-open test sheet ──────────────────────────────────
    LaunchedEffect(testResult) {
        if (testResult != null) ui = ui.copy(showTestSheet = true)
    }

    // ── system back exits selection mode ─────────────────────
    BackHandler(enabled = ui.isSelectionMode) {
        ui = ui.copy(isSelectionMode = false, selectedIds = emptySet())
    }

    // ── Root Box — lets us overlay the snackbar ───────────────
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Main content column ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cs.background)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
        ) {

            HomeTopBar(
                isSelectionMode   = ui.isSelectionMode,
                selectedCount     = ui.selectedIds.size,
                toggleTheme       = toggleTheme,
                onCancelSelection = { ui = ui.copy(isSelectionMode = false, selectedIds = emptySet()) },
                onDeleteSelected  = {
                    vm.deleteMultiple(ui.selectedIds.toList())
                    ui = ui.copy(isSelectionMode = false, selectedIds = emptySet())
                },
                onExportSelected  = {
                    vm.exportProxies(proxies.filter { it.id in ui.selectedIds })
                    ui = ui.copy(isSelectionMode = false, selectedIds = emptySet())
                }
            )

            ActiveConnectionBanner(
                proxy           = activeProxy,
                lastTestResult  = activeProxy?.let { lastTestResults[it.id] },
                extra           = extra,
                primary         = cs.primary,
                onBackground    = cs.onBackground,
                onSpeedTest     = {
                    activeProxy?.let { p ->
                        ui = ui.copy(testingProxyId = p.id)
                        vm.performFullProxyTest(p)
                    }
                },
                onBrowse = {
                    activeProxy?.let { p ->
                        navController.navigate(
                            com.example.maxx.navigation.routes.Screen.Browser.createRoute(p.id)
                        )
                    }
                },
            )

            ProxyListHeader(
                count    = proxies.size,
                primary  = cs.primary,
                onBg     = cs.onBackground,
                onTestAll = {
                    proxies.firstOrNull()?.let { p ->
                        ui = ui.copy(testingProxyId = p.id)
                        vm.performFullProxyTest(p)
                    }
                }
            )

            if (proxies.isEmpty()) {
                EmptyProxyState(
                    hasSearch        = false,
                    onBackground     = cs.onBackground,
                    onSurfaceVariant = cs.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(proxies, key = { it.id }) { proxy ->
                        val isTesting = proxyState is ProxyViewModel.ProxyState.Testing &&
                                        ui.testingProxyId == proxy.id
                        val lastResult = lastTestResults[proxy.id]

                        // While actively testing this proxy, latencyMs is null (shows spinner).
                        // Once done, the last result from the map drives the badge (persists across sheet close).
                        val latencyMs: Long? = when {
                            isTesting  -> null
                            lastResult != null -> if (lastResult.success) lastResult.latencyMs else -1L
                            else       -> null
                        }

                        ProxyCard(
                            proxy            = proxy,
                            isSelected       = proxy.id in ui.selectedIds,
                            isSelectionMode  = ui.isSelectionMode,
                            isTesting        = isTesting,
                            latencyMs        = latencyMs,
                            lastTestSuccess  = lastResult?.success,
                            isActive         = proxy.id == ui.activeProxyId,
                            extra            = extra,
                            cardBg           = cs.surface,
                            primary          = cs.primary,
                            onSurface        = cs.onSurface,
                            onSurfaceVariant = cs.onSurfaceVariant,
                            onTap = {
                                if (ui.isSelectionMode) {
                                    val ids = ui.selectedIds.toMutableSet()
                                    if (proxy.id in ids) ids.remove(proxy.id) else ids.add(proxy.id)
                                    ui = ui.copy(selectedIds = ids, isSelectionMode = ids.isNotEmpty())
                                } else {
                                    ui = ui.copy(activeProxyId = proxy.id)
                                }
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                ui = ui.copy(
                                    isSelectionMode = true,
                                    selectedIds     = ui.selectedIds + proxy.id
                                )
                            },
                            onTest   = {
                                ui = ui.copy(testingProxyId = proxy.id, showTestSheet = true)
                                vm.performFullProxyTest(proxy)
                            },
                            onEdit   = { navController.navigate("addProxy?proxyId=${proxy.id}") },
                            onDelete = { vm.deleteProxyWithUndo(proxy) },
                            onBrowse = {
                                navController.navigate(
                                    com.example.maxx.navigation.routes.Screen.Browser.createRoute(proxy.id)
                                )
                            },
                        )
                    }
                    item { ProTipCard(surface = cs.surface, outline = cs.outline) }
                }
            }
        }   // end main Column

        // ── Undo-delete snackbar (floats above, bottom-anchored) ──────────
        AnimatedVisibility(
            visible  = pendingDelete != null,
            enter    = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec  = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                )
            ) + fadeIn(animationSpec = tween(200)),
            exit     = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(280, easing = FastOutLinearInEasing)
            ) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            pendingDelete?.let { proxy ->
                UndoDeleteSnackbar(
                    proxyName   = proxy.name,
                    secondsLeft = undoSecondsLeft,
                    onUndo      = { vm.undoDelete() },
                    onDismiss   = { vm.dismissUndoSnackbar() },
                )
            }
        }
    }   // end root Box

    // ── Test result bottom sheet ──────────────────────────────
    if (ui.showTestSheet) {
        val isDark = cs.background.luminance() < 0.5f
        ProxyTestResultBottomSheet(
            isDarkMode    = isDark,
            testResult    = testResult,
            isTesting     = proxyState is ProxyViewModel.ProxyState.Testing,
            onDismiss     = {
                ui = ui.copy(showTestSheet = false, testingProxyId = null)
                vm.clearTestResult()
            },
            onTestAgain   = {
                proxies.find { it.id == ui.testingProxyId }?.let { vm.performFullProxyTest(it) }
            },
            onOpenBrowser = {
                val pid = ui.testingProxyId
                ui = ui.copy(showTestSheet = false, testingProxyId = null)
                vm.clearTestResult()
                if (pid != null) {
                    navController.navigate(
                        com.example.maxx.navigation.routes.Screen.Browser.createRoute(pid)
                    )
                }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    isSelectionMode: Boolean,
    selectedCount: Int,
    toggleTheme: () -> Unit,
    onCancelSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onExportSelected: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {
            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "topbar_title"
            ) { inSel ->
                if (inSel) {
                    Text("$selectedCount selected", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = cs.primary)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cs.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, null, tint = cs.onPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("ProxyGenZ", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = cs.onBackground)
                    }
                }
            }
        },
        actions = {
            AnimatedContent(
                targetState = isSelectionMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "topbar_actions"
            ) { inSel ->
                Row {
                    if (inSel) {
                        IconButton(onClick = onExportSelected) { Icon(Icons.Default.IosShare, "Export", tint = cs.onBackground) }
                        IconButton(onClick = onDeleteSelected) { Icon(Icons.Default.Delete,   "Delete", tint = cs.error) }
                        IconButton(onClick = onCancelSelection){ Icon(Icons.Default.Close,    "Cancel", tint = cs.onBackground) }
                    } else {
                        IconButton(onClick = {}) { Icon(Icons.Default.Search, "Search", tint = cs.onBackground) }
                        IconButton(onClick = {}) { Icon(Icons.Default.Tune,   "Filter", tint = cs.onBackground) }
                        IconButton(onClick = toggleTheme) {
                            Icon(Icons.Default.DarkMode, "Theme", tint = cs.onBackground)
                        }
                    }
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  ACTIVE CONNECTION BANNER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActiveConnectionBanner(
    proxy: ProxyProfile?,
    lastTestResult: com.example.maxx.domain.models.ProxyTestResult?,
    extra: com.example.maxx.presentation.theme.AppExtraColors,
    primary: Color,
    onBackground: Color,
    onSpeedTest: () -> Unit,
    onBrowse: () -> Unit,
) {
    val cs   = MaterialTheme.colorScheme
    val dark = cs.background.luminance() < 0.5f
    val shadowColor = if (dark) Color(0xFF9B8FFF).copy(alpha = 0.9f) else Color.Black

    // Status dot colour based on last test
    val statusDotColor = when {
        lastTestResult == null          -> onBackground.copy(alpha = 0.25f)  // never tested — grey
        lastTestResult.success          -> extra.latencyGood                 // green
        else                            -> extra.latencyBad                  // red
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(extra.bannerBackground)
            .border(1.dp, primary.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Status dot ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusDotColor)
        )
        Spacer(Modifier.width(8.dp))

        // ── Left: title + proxy name + ip:port ────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "ACTIVE CONNECTION",
                fontSize      = 10.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color         = extra.mutedLabel,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = proxy?.name ?: "No proxy selected",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    color      = primary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
            }
            // ip:port + latency subtitle
            if (proxy != null) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = "${proxy.ip}:${proxy.port}",
                        fontSize  = 11.sp,
                        color     = extra.mutedLabel,
                        maxLines  = 1,
                        overflow  = TextOverflow.Ellipsis,
                    )
                    if (lastTestResult != null) {
                        Text(
                            text  = "  •  ",
                            fontSize = 11.sp,
                            color = extra.mutedLabel,
                        )
                        Text(
                            text  = if (lastTestResult.success) "${lastTestResult.latencyMs}ms" else "Unreachable",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (lastTestResult.success) extra.latencyGood else extra.latencyBad,
                        )
                    }
                }
            }
        }

        // ── Right: Speed Test + Browse buttons horizontal ────────────────────
        Spacer(Modifier.width(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // Speed Test — NetworkCheck icon + label
            NeoBrutalistButton(
                onClick        = onSpeedTest,
                shadowColor    = shadowColor,
                containerColor = cs.surface,
                contentColor   = onBackground,
                borderColor    = onBackground.copy(alpha = 0.85f),
                cornerRadius   = 10.dp,
            ) {
                Icon(Icons.Default.NetworkCheck, null, modifier = Modifier.size(13.dp), tint = onBackground)
                Spacer(Modifier.width(3.dp))
                Text("Test", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = onBackground)
            }

            // Browse — icon only, minimal size
            if (proxy != null) {
                NeoBrutalistButton(
                    onClick        = onBrowse,
                    shadowColor    = shadowColor,
                    containerColor = cs.surface,
                    contentColor   = onBackground,
                    borderColor    = onBackground.copy(alpha = 0.85f),
                    cornerRadius   = 10.dp,
                    iconOnly       = true,
                ) {
                    Icon(Icons.Default.Public, null, modifier = Modifier.size(15.dp), tint = onBackground)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  NEO-BRUTALIST BUTTON  (shared between banner buttons)
//  ▸ Less rounded corners (8 dp)
//  ▸ Solid thin border all around
//  ▸ 4 dp black/purple offset shadow bottom-right
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NeoBrutalistButton(
    onClick: () -> Unit,
    shadowColor: Color,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 10.dp,
    iconOnly: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val offsetPx   = 3.dp   // shadow offset — equal right & bottom
    val strokePx   = 1.5.dp // thin border on all visible edges

    Box(modifier = modifier.padding(bottom = offsetPx, end = offsetPx)) {
        Box(
            modifier = Modifier.drawBehind {
                val r      = cornerRadius.toPx()
                val off    = offsetPx.toPx()
                val stroke = strokePx.toPx()

                // 1. Offset shadow block — shifted right+bottom (equal offset)
                drawRoundRect(
                    color        = shadowColor,
                    topLeft      = Offset(off, off),
                    size         = Size(size.width, size.height),
                    cornerRadius = CornerRadius(r),
                )

                // 2. Thin border on ALL sides (drawn after shadow so it sits on top)
                drawRoundRect(
                    color        = borderColor,
                    topLeft      = Offset(stroke / 2, stroke / 2),
                    size         = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(r),
                    style        = Stroke(width = stroke),
                )
            },
        ) {
            Button(
                onClick        = onClick,
                shape          = RoundedCornerShape(cornerRadius),
                colors         = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor   = contentColor,
                ),
                // No BorderStroke — border drawn manually above
                border         = null,
                contentPadding = if (iconOnly)
                    PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                else
                    PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                elevation      = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                content        = content,
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
//  PROXY LIST HEADER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProxyListHeader(count: Int, primary: Color, onBg: Color, onTestAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "MY PROXIES ($count)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = onBg.copy(alpha = 0.55f)
        )
        Spacer(Modifier.weight(1f))
        Text(
            "Test All",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary,
            modifier = Modifier.clickable(onClick = onTestAll)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PROXY CARD — Custom drag: swipe left up to 40% to reveal Edit + Delete
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProxyCard(
    proxy: ProxyProfile,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isTesting: Boolean,
    latencyMs: Long?,
    lastTestSuccess: Boolean?,          // null = never tested, true = last pass, false = last fail
    isActive: Boolean = false,
    extra: com.example.maxx.presentation.theme.AppExtraColors,
    cardBg: Color,
    primary: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onTest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBrowse: () -> Unit,
) {
    val haptic   = LocalHapticFeedback.current
    val density  = androidx.compose.ui.platform.LocalDensity.current

    // Raw drag offset (px). Always ≤ 0 (left only).
    var offsetPx by remember { mutableFloatStateOf(0f) }

    // Card size resolved after first layout pass
    var cardWidthPx  by remember { mutableFloatStateOf(0f) }
    var cardHeightPx by remember { mutableFloatStateOf(0f) }

    // Max reveal = 40% of card width (the action strip)
    val maxRevealPx = cardWidthPx * 0.40f

    // Smooth animated version of offsetPx
    val animatedOffset by animateFloatAsState(
        targetValue   = offsetPx,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "card_drag_offset",
    )

    // Fraction 0→1 of how much the action strip is revealed
    val revealFraction = if (maxRevealPx > 0f)
        (-animatedOffset / maxRevealPx).coerceIn(0f, 1f)
    else 0f

    // Consider "open" once past 50% of max reveal
    val isRevealed = offsetPx <= -(maxRevealPx * 0.5f)

    // Reset to closed whenever this card's key changes (delete / re-order)
    LaunchedEffect(proxy.id) { offsetPx = 0f }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                cardWidthPx  = coords.size.width.toFloat()
                cardHeightPx = coords.size.height.toFloat()
            },
    ) {
        if (cardWidthPx > 0f && cardHeightPx > 0f) {
            SwipeActionBackground(
                revealFraction    = revealFraction,
                actionStripWidth  = with(density) { maxRevealPx.toDp() },
                actionStripHeight = with(density) { cardHeightPx.toDp() },
                onEdit   = { offsetPx = 0f; onEdit() },
                onDelete = { offsetPx = 0f; onDelete() },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }

        ProxyCardContent(
            proxy            = proxy,
            isSelected       = isSelected,
            isSelectionMode  = isSelectionMode,
            isTesting        = isTesting,
            latencyMs        = latencyMs,
            lastTestSuccess  = lastTestSuccess,
            isActive         = isActive,
            extra            = extra,
            cardBg           = cardBg,
            primary          = primary,
            onSurface        = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            revealFraction   = revealFraction,
            onTap = {
                if (isRevealed) offsetPx = 0f
                else onTap()
            },
            onLongPress      = onLongPress,
            onTest           = onTest,
            onBrowse         = onBrowse,
            dragOffsetPx     = animatedOffset,
            onDragDelta      = { delta ->
                if (cardWidthPx > 0f) {
                    val next = (offsetPx + delta).coerceIn(-maxRevealPx, 0f)
                    if (next != offsetPx) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    offsetPx = next
                }
            },
            onDragEnd = {
                offsetPx = if (offsetPx < -(maxRevealPx * 0.4f)) -maxRevealPx else 0f
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SWIPE ACTION BACKGROUND
//  ▸ Sits BEHIND the card. The card's own RoundedCornerShape(14dp) slides over
//    it, so the card corners naturally mask the strip's left edge — no custom
//    concave shape needed. Only the right side is rounded to match the card edge.
//  ▸ Fixed width = 40% of card, fixed height = card height
//  ▸ Split 50/50: Edit (dark) | Delete (red)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SwipeActionBackground(
    revealFraction: Float,
    actionStripWidth: androidx.compose.ui.unit.Dp,
    actionStripHeight: androidx.compose.ui.unit.Dp,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconScale by animateFloatAsState(
        targetValue   = if (revealFraction > 0.35f) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "action_icon_scale",
    )
    val iconAlpha by animateFloatAsState(
        targetValue   = if (revealFraction > 0.20f) 1f else 0f,
        animationSpec = tween(100),
        label         = "action_icon_alpha",
    )

    Row(
        modifier = modifier
            .width(actionStripWidth)
            .height(actionStripHeight)
            // Only round the RIGHT side — the card slides over and clips the left
            .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp)),
    ) {
        // ── Edit (dark) ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFF1C1C1E))
                .clickable(onClick = onEdit),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.Edit,
                contentDescription = "Edit",
                tint               = Color.White,
                modifier           = Modifier
                    .size(22.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale; alpha = iconAlpha },
            )
        }

        // ── Delete (red) ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFE53935))
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Delete",
                tint               = Color.White,
                modifier           = Modifier
                    .size(22.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale; alpha = iconAlpha },
            )
        }
    }
}

@Composable
private fun ProxyCardContent(
    proxy: ProxyProfile,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isTesting: Boolean,
    latencyMs: Long?,
    lastTestSuccess: Boolean?,
    isActive: Boolean = false,
    extra: com.example.maxx.presentation.theme.AppExtraColors,
    cardBg: Color,
    primary: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    revealFraction: Float = 0f,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onTest: () -> Unit,
    onBrowse: () -> Unit,
    dragOffsetPx: Float = 0f,
    onDragDelta: (Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
) {
    // Right corners animate from 14dp → 0dp as card is swiped open
    // Left corners always stay at 14dp
    val rightCornerDp by animateDpAsState(
        targetValue   = if (revealFraction > 0.01f) 0.dp else 14.dp,
        animationSpec = tween(durationMillis = 150),
        label         = "card_right_corner",
    )
    val cardShape = RoundedCornerShape(
        topStart    = 14.dp,
        bottomStart = 14.dp,
        topEnd      = rightCornerDp,
        bottomEnd   = rightCornerDp,
    )
    val cs = MaterialTheme.colorScheme
    // Active card gets a solid primary border (2dp); selected = primary; default = faint outline
    val borderColor = when {
        isActive   -> primary
        isSelected -> primary
        else       -> cs.outline.copy(alpha = 0.25f)
    }
    val borderWidth by animateDpAsState(
        targetValue   = if (isActive) 2.dp else 1.dp,
        animationSpec = tween(200),
        label         = "card_border_width",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
            .clip(cardShape)
            .background(cardBg)
            .border(borderWidth, borderColor, cardShape)
            .pointerInput(isSelectionMode, isSelected) {
                detectTapGestures(onTap = { onTap() }, onLongPress = { onLongPress() })
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd   = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        onDragDelta(dragAmount)
                    }
                )
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox (selection mode) — purely visual, row's pointerInput handles the tap
        AnimatedVisibility(visible = isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = primary),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
        }

        // Status dot — 4 states:
        //  • Pulsing amber  = currently testing this proxy
        //  • Green          = last test succeeded
        //  • Red            = last test failed
        //  • Grey           = never tested
        val dotColor = when {
            isTesting                -> extra.statusTesting
            lastTestSuccess == true  -> extra.latencyGood    // green
            lastTestSuccess == false -> extra.latencyBad     // red
            else                     -> onSurfaceVariant.copy(alpha = 0.35f)  // grey, never tested
        }

        // Pulsing animation for the dot while testing
        val dotScale by animateFloatAsState(
            targetValue   = if (isTesting) 1.4f else 1f,
            animationSpec = if (isTesting)
                infiniteRepeatable(tween(600), RepeatMode.Reverse)
            else
                tween(200),
            label = "dot_pulse",
        )

        Box(
            Modifier
                .size(8.dp)
                .graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(10.dp))

        // Flag image
        AsyncImage(
            model = proxy.flagUrl ?: "https://flagcdn.com/w40/un.png",
            contentDescription = null,
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(10.dp))

        // Name + protocol badge + host:port
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    proxy.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(extra.badgeBackground).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(proxy.protocol.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = extra.badgeText)
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = proxy.city?.let { "$it - ${proxy.country ?: ""}" } ?: "${proxy.ip}:${proxy.port}",
                fontSize = 12.sp,
                color = onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // Right column: latency badge
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isTesting) {
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp)).background(extra.statusTesting.copy(0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(10.dp), strokeWidth = 1.5.dp, color = extra.statusTesting)
                        Spacer(Modifier.width(4.dp))
                        Text("Testing", fontSize = 11.sp, color = extra.statusTesting, fontWeight = FontWeight.Medium)
                    }
                }
            } else if (lastTestSuccess == false) {
                // Last test failed — show red "Failed" badge
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(extra.latencyBad.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = "Failed",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = extra.latencyBad,
                    )
                }
            } else {
                // Success (latencyMs set) or never tested (latencyMs null)
                val badgeColor = when {
                    latencyMs == null  -> onSurfaceVariant.copy(0.4f)
                    latencyMs < 100    -> extra.latencyGood
                    latencyMs < 300    -> extra.latencyMedium
                    else               -> extra.latencyBad
                }
                val label = if (latencyMs != null) "${latencyMs}ms" else "—"
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeColor.copy(alpha = if (latencyMs != null) 0.15f else 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = label,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = badgeColor,
                    )
                }
            }
        }

        // ── Test + Browse icon buttons (hidden in selection mode) ─────────
        AnimatedVisibility(visible = !isSelectionMode) {
            Row {
                IconButton(onClick = onTest, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.NetworkCheck,
                        contentDescription = "Test Proxy",
                        tint     = primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(onClick = onBrowse, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = "Open in Browser",
                        tint     = onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PRO TIP CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProTipCard(surface: Color, outline: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, outline, RoundedCornerShape(14.dp))
            .background(surface.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Pro tip: Long-press any proxy to bulk edit or delete.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  UNDO-DELETE SNACKBAR
//  ▸ Modern pill shape, blurred/elevated surface
//  ▸ Animated countdown arc (SVG-style drawBehind)
//  ▸ Undo button + dismiss ✕
//  ▸ secondsLeft drives the arc sweep (0 = full circle = expired)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun UndoDeleteSnackbar(
    proxyName: String,
    secondsLeft: Int,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs    = MaterialTheme.colorScheme

    // Fraction of the 5s window remaining: 1f at start → 0f at end
    val fraction = secondsLeft / 5f

    // Animate the sweep so it moves smoothly even between second ticks
    val animatedFraction by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(durationMillis = 900, easing = LinearEasing),
        label         = "undo_countdown_sweep"
    )

    val trackColor  = cs.onSurface.copy(alpha = 0.12f)
    val sweepColor  = cs.primary

    Surface(
        modifier      = modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(18.dp),
        color         = cs.inverseSurface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier            = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {

            // ── Countdown arc ring ────────────────────────────────────────
            Box(
                modifier          = Modifier.size(36.dp),
                contentAlignment  = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val stroke = 3.dp.toPx()
                    val inset  = stroke / 2f

                    // Track ring
                    drawArc(
                        color       = trackColor,
                        startAngle  = -90f,
                        sweepAngle  = 360f,
                        useCenter   = false,
                        topLeft     = Offset(inset, inset),
                        size        = Size(size.width - stroke, size.height - stroke),
                        style       = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                    // Filled sweep (shrinks to 0 as time runs out)
                    drawArc(
                        color       = sweepColor,
                        startAngle  = -90f,
                        sweepAngle  = 360f * animatedFraction,
                        useCenter   = false,
                        topLeft     = Offset(inset, inset),
                        size        = Size(size.width - stroke, size.height - stroke),
                        style       = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                // Second count label
                Text(
                    text       = "$secondsLeft",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = cs.inverseOnSurface,
                )
            }

            // ── Message ───────────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "\"$proxyName\" deleted",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = cs.inverseOnSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    text     = "Tap UNDO to restore",
                    fontSize = 11.sp,
                    color    = cs.inverseOnSurface.copy(alpha = 0.65f),
                )
            }

            // ── UNDO button ───────────────────────────────────────────────
            TextButton(
                onClick = onUndo,
                colors  = ButtonDefaults.textButtonColors(contentColor = cs.inversePrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    "UNDO",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )
            }

            // ── Dismiss ✕ ─────────────────────────────────────────────────
            IconButton(
                onClick  = onDismiss,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint               = cs.inverseOnSurface.copy(alpha = 0.6f),
                    modifier           = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  EMPTY STATE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyProxyState(hasSearch: Boolean, onBackground: Color, onSurfaceVariant: Color) {
    Box(Modifier.fillMaxSize().padding(horizontal = 32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.CloudOff, null, tint = onSurfaceVariant, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                if (hasSearch) "No proxies match your search." else "No proxies added.",
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (hasSearch) "Try a different keyword." else "Tap + to add one.",
                fontSize = 14.sp, color = onSurfaceVariant
            )
        }
    }
}

// Helper: check if current scheme is light (used to pass isDarkMode to legacy screens)
private val ColorScheme.isLight: Boolean
    get() = background.luminance() > 0.5f
