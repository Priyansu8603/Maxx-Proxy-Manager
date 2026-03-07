package com.example.maxx.presentation.screens.addproxy

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.maxx.domain.models.GeoIPResponse
import com.example.maxx.domain.models.ProxyProfile
import com.example.maxx.presentation.theme.extraColors
import com.example.maxx.presentation.theme.extraColors
import com.example.maxx.presentation.theme.isDark
import com.example.maxx.presentation.viewmodel.ProxyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// ─────────────────────────────────────────────────────────────────────────────
//  Network helpers  (kept in same file — no UI dependency)
// ─────────────────────────────────────────────────────────────────────────────
suspend fun getGeoIPInfo(ip: String): GeoIPResponse? = withContext(Dispatchers.IO) {
    try {
        val conn = URL("http://ip-api.com/json/$ip").openConnection() as HttpURLConnection
        conn.requestMethod  = "GET"
        conn.connectTimeout = 5_000
        conn.readTimeout    = 5_000
        val body = (if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream)
            .bufferedReader().use { it.readText() }
        val json = JSONObject(body)
        if (json.getString("status") == "success") {
            GeoIPResponse(
                query       = json.optString("query").ifEmpty { null },
                country     = json.optString("country").ifEmpty { null },
                countryCode = json.optString("countryCode").ifEmpty { null },
                city        = json.optString("city").ifEmpty { null },
                isp         = json.optString("isp").ifEmpty { null },
            )
        } else null
    } catch (e: Exception) {
        Log.e("GeoIP", "Error fetching geo info", e); null
    }
}

fun countryCodeToFlagUrl(code: String?): String =
    if (code.isNullOrBlank()) "https://flagcdn.com/w80/un.png"
    else "https://flagcdn.com/w80/${code.lowercase()}.png"

// ─────────────────────────────────────────────────────────────────────────────
//  Clipboard autofill parser
//  Supports: socks5://user:pass@host:port  |  host:port  |  user:pass@host:port
// ─────────────────────────────────────────────────────────────────────────────
private data class ParsedProxy(
    val protocol: String? = null,
    val host: String      = "",
    val port: String      = "",
    val username: String  = "",
    val password: String  = "",
)

private fun parseProxyString(raw: String): ParsedProxy? {
    if (raw.isBlank()) return null
    return try {
        var s = raw.trim()
        // protocol prefix
        val proto = when {
            s.startsWith("socks5://", ignoreCase = true) -> { s = s.removePrefix("socks5://").removePrefix("SOCKS5://"); "SOCKS5" }
            s.startsWith("socks4://", ignoreCase = true) -> { s = s.removePrefix("socks4://").removePrefix("SOCKS4://"); "SOCKS4" }
            s.startsWith("http://",   ignoreCase = true) -> { s = s.removePrefix("http://").removePrefix("HTTP://");   "HTTP"   }
            s.startsWith("https://",  ignoreCase = true) -> { s = s.removePrefix("https://").removePrefix("HTTPS://"); "HTTPS"  }
            else -> null
        }
        // user:pass@host:port
        var user = ""; var pass = ""
        if (s.contains("@")) {
            val (creds, rest) = s.split("@", limit = 2)
            s = rest
            if (creds.contains(":")) {
                val (u, p) = creds.split(":", limit = 2); user = u; pass = p
            } else user = creds
        }
        // host:port
        val lastColon = s.lastIndexOf(':')
        if (lastColon < 0) return null
        val host = s.substring(0, lastColon)
        val port = s.substring(lastColon + 1)
        if (host.isBlank() || port.toIntOrNull() == null) return null
        ParsedProxy(proto, host, port, user, pass)
    } catch (_: Exception) { null }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Add / Edit Proxy Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProxyScreen(
    navController: NavController,
    proxy: ProxyProfile? = null,
) {
    val vm          = hiltViewModel<ProxyViewModel>()
    val scope       = rememberCoroutineScope()
    val clipboard   = LocalClipboardManager.current
    val isEditMode  = proxy != null

    // ── form state ────────────────────────────────────────────
    var nickname  by rememberSaveable { mutableStateOf(proxy?.name     ?: "") }
    var host      by rememberSaveable { mutableStateOf(proxy?.ip       ?: "") }
    var port      by rememberSaveable { mutableStateOf(proxy?.port?.takeIf { it > 0 }?.toString() ?: "") }
    var protocol  by rememberSaveable { mutableStateOf(proxy?.protocol ?: "SOCKS5") }
    var username  by rememberSaveable { mutableStateOf(proxy?.username ?: "") }
    var password  by rememberSaveable { mutableStateOf(proxy?.password ?: "") }

    // ── validation state ──────────────────────────────────────
    var hostError by remember { mutableStateOf<String?>(null) }
    var portError by remember { mutableStateOf<String?>(null) }

    // ── async state ───────────────────────────────────────────
    var isSaving   by remember { mutableStateOf(false) }
    var isTesting  by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    // ── theme ─────────────────────────────────────────────────
    val cs    = MaterialTheme.colorScheme
    val extra = MaterialTheme.extraColors

    // ── helpers ───────────────────────────────────────────────
    val protocolOptions = listOf("SOCKS5", "HTTP", "HTTPS")

    fun validate(): Boolean {
        hostError = when { host.isBlank() -> "Host is required"; else -> null }
        portError = when {
            port.isBlank()                       -> "Port is required"
            port.toIntOrNull() == null            -> "Must be a number"
            port.toInt() !in 1..65535            -> "Range: 1–65535"
            else                                 -> null
        }
        return hostError == null && portError == null
    }

    fun doSave() {
        if (!validate()) return
        isSaving = true
        scope.launch {
            val geoInfo = getGeoIPInfo(host)
            val profile = proxy?.copy(
                name     = nickname.ifBlank { host },
                ip       = host,
                port     = port.toInt(),
                protocol = protocol,
                username = username.ifBlank { null },
                password = password.ifBlank { null },
                country  = geoInfo?.country,
                city     = geoInfo?.city,
                isp      = geoInfo?.isp,
                flagUrl  = countryCodeToFlagUrl(geoInfo?.countryCode),
            ) ?: ProxyProfile(
                name     = nickname.ifBlank { host },
                ip       = host,
                port     = port.toInt(),
                protocol = protocol,
                username = username.ifBlank { null },
                password = password.ifBlank { null },
                country  = geoInfo?.country,
                city     = geoInfo?.city,
                isp      = geoInfo?.isp,
                flagUrl  = countryCodeToFlagUrl(geoInfo?.countryCode),
            )
            if (isEditMode) vm.updateProxy(profile) else vm.insertProxy(profile)
            isSaving = false
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = cs.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = cs.onBackground,
                        )
                    }
                },
                title = {
                    Text(
                        text       = if (isEditMode) "Edit Proxy" else "Add Proxy",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = cs.onBackground,
                    )
                },
                actions = {
                    TextButton(onClick = { doSave() }, enabled = !isSaving) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = cs.primary)
                        } else {
                            Icon(Icons.Default.Check, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Save", color = cs.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            // ── sticky bottom action bar ──────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Test Connection button
                OutlinedButton(
                    onClick = {
                        if (!validate()) return@OutlinedButton
                        isTesting = true; testResult = null
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    val start = System.currentTimeMillis()
                                    val conn  = URL("http://ip-api.com/json/").openConnection() as HttpURLConnection
                                    conn.connectTimeout = 8_000; conn.readTimeout = 8_000
                                    val ok = conn.responseCode in 200..299
                                    val ms = System.currentTimeMillis() - start
                                    testResult = if (ok) "✅ Connected — ${ms}ms" else "❌ Failed (HTTP ${conn.responseCode})"
                                } catch (e: Exception) {
                                    testResult = "❌ ${e.message ?: "Connection failed"}"
                                }
                            }
                            isTesting = false
                        }
                    },
                    modifier      = Modifier.fillMaxWidth().height(50.dp),
                    shape         = RoundedCornerShape(14.dp),
                    border        = BorderStroke(1.5.dp, cs.primary),
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = cs.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Testing…", color = cs.primary, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.NetworkCheck, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Test Connection", color = cs.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Save Proxy Configuration button
                Button(
                    onClick   = { doSave() },
                    modifier  = Modifier.fillMaxWidth().height(52.dp),
                    shape     = RoundedCornerShape(14.dp),
                    enabled   = !isSaving,
                    colors    = ButtonDefaults.buttonColors(containerColor = cs.primary),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = cs.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Saving…", color = cs.onPrimary, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Save Proxy Configuration", color = cs.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Autofill Banner ──────────────────────────────
            AutofillBanner(
                primary     = cs.primary,
                onPrimary   = cs.onPrimary,
                surfaceVar  = cs.surfaceVariant,
                onSurfaceVar = cs.onSurfaceVariant,
                onClick = {
                    val text = clipboard.getText()?.text ?: return@AutofillBanner
                    val parsed = parseProxyString(text) ?: return@AutofillBanner
                    if (parsed.host.isNotBlank()) host     = parsed.host
                    if (parsed.port.isNotBlank()) port     = parsed.port
                    if (parsed.protocol != null)  protocol = parsed.protocol
                    if (parsed.username.isNotBlank()) username = parsed.username
                    if (parsed.password.isNotBlank()) password = parsed.password
                }
            )

            // ── Test result chip ─────────────────────────────
            AnimatedVisibility(visible = testResult != null, enter = fadeIn(), exit = fadeOut()) {
                testResult?.let { msg ->
                    val isOk = msg.startsWith("✅")
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isOk) extra.latencyGood.copy(.12f) else extra.latencyBad.copy(.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(msg, fontSize = 13.sp, color = if (isOk) extra.latencyGood else extra.latencyBad, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // ── Section: PROXY IDENTITY ───────────────────────
            FormSection(title = "PROXY IDENTITY", icon = Icons.Default.Language, cs = cs) {
                ProxyFormField(
                    label       = "Nickname",
                    value       = nickname,
                    onValueChange = { nickname = it },
                    placeholder = "e.g., Premium NY Residential",
                    cs          = cs,
                )
            }

            // ── Section: CONNECTION DETAILS ───────────────────
            FormSection(title = "CONNECTION DETAILS", icon = Icons.Default.Cable, cs = cs) {
                // Protocol Dropdown
                Column {
                    Text("Protocol Type", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                    ProtocolDropdown(
                        selected  = protocol,
                        options   = protocolOptions,
                        onSelect  = { protocol = it },
                        cs        = cs,
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Host + Port row
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1.8f)) {
                        ProxyFormField(
                            label         = "Host / IP",
                            value         = host,
                            onValueChange = { host = it; hostError = null },
                            placeholder   = "192.168.1.1",
                            error         = hostError,
                            cs            = cs,
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        ProxyFormField(
                            label         = "Port",
                            value         = port,
                            onValueChange = { if (it.all(Char::isDigit) && it.length <= 5) { port = it; portError = null } },
                            placeholder   = "8080",
                            error         = portError,
                            keyboardType  = KeyboardType.Number,
                            cs            = cs,
                        )
                    }
                }
            }

            // ── Section: SECURITY ─────────────────────────────
            FormSection(
                title     = "SECURITY",
                icon      = Icons.Default.Shield,
                cs        = cs,
                badge     = { Badge(containerColor = cs.surfaceVariant) { Text("Optional", color = cs.onSurfaceVariant, fontSize = 10.sp) } }
            ) {
                ProxyFormField(
                    label         = "Username",
                    value         = username,
                    onValueChange = { username = it },
                    placeholder   = "proxy_user_123",
                    leadingIcon   = Icons.Default.Key,
                    cs            = cs,
                )
                ProxyFormField(
                    label         = "Password",
                    value         = password,
                    onValueChange = { password = it },
                    placeholder   = "••••••••",
                    leadingIcon   = Icons.Default.Key,
                    isPassword    = true,
                    cs            = cs,
                )
            }

            // ── Info tip card ────────────────────────────────
            ProxyInfoCard(protocol = protocol, cs = cs)

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  AUTOFILL BANNER
// ─────────────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
//  AUTOFILL BANNER
//  Light → teal/green  (#00C9A7 border, #E8FAF7 bg, #00C9A7 icon+text)
//  Dark  → pink/purple (#C84BED border, #2D1A3D bg, #C84BED icon+text)
//  Dashed border via Canvas PathEffect.dashPathEffect — no third-party lib.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AutofillBanner(
    primary: Color,        // kept for fallback; actual colors are theme-resolved below
    onPrimary: Color,
    surfaceVar: Color,
    onSurfaceVar: Color,
    onClick: () -> Unit,
) {
    val isDark = MaterialTheme.isDark

    // Exact colours extracted from the design images
    val accentColor  = if (isDark) Color(0xFFC84BED)  else Color(0xFF00C9A7)
    val bgColor      = if (isDark) Color(0xFF2D1A3D)  else Color(0xFFE8FAF7)
    val subtitleColor = if (isDark) Color(0xFFB0B0D0) else Color(0xFF4ABFA8)
    val cornerRadius = 14.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .clickable(onClick = onClick)
    ) {
        // ── Dashed border via Canvas ──────────────────────────
        val strokeWidthPx   = with(LocalDensity.current) { 1.5.dp.toPx() }
        val cornerRadiusPx  = with(LocalDensity.current) { cornerRadius.toPx() }
        val dashPathEffect  = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color       = accentColor.copy(alpha = 0.85f),
                size        = Size(size.width, size.height),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                style       = Stroke(width = strokeWidthPx, pathEffect = dashPathEffect),
            )
        }

        // ── Content ───────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = if (isDark) 0.18f else 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint               = accentColor,
                    modifier           = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Autofill from Clipboard",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = accentColor,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Detects host:port, user:pass formats",
                    fontSize = 12.sp,
                    color    = subtitleColor,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FORM SECTION CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FormSection(
    title: String,
    icon: ImageVector,
    cs: ColorScheme,
    badge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = cs.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text       = title,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color      = cs.onSurfaceVariant,
                modifier   = Modifier.weight(1f)
            )
            badge?.invoke()
        }
        HorizontalDivider(color = cs.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  REUSABLE FORM FIELD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProxyFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?           = null,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean      = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    cs: ColorScheme,
) {
    var showPassword by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant)
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, fontSize = 14.sp, color = cs.onSurface.copy(.4f)) },
            singleLine    = true,
            isError       = error != null,
            shape         = RoundedCornerShape(12.dp),
            modifier      = Modifier.fillMaxWidth(),
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            leadingIcon = leadingIcon?.let { ic ->
                { Icon(ic, null, tint = cs.onSurfaceVariant.copy(.6f), modifier = Modifier.size(18.dp)) }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide" else "Show",
                            tint = cs.onSurfaceVariant,
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = cs.primary,
                unfocusedBorderColor = cs.outline,
                errorBorderColor     = cs.error,
                focusedContainerColor   = cs.surface,
                unfocusedContainerColor = cs.surface,
                errorContainerColor     = cs.surface,
            ),
        )
        if (error != null) {
            Text(error, fontSize = 11.sp, color = cs.error)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PROTOCOL DROPDOWN
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProtocolDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    cs: ColorScheme,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value             = selected,
            onValueChange     = {},
            readOnly          = true,
            modifier          = Modifier.fillMaxWidth().menuAnchor(),
            shape             = RoundedCornerShape(12.dp),
            trailingIcon      = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors            = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = cs.primary,
                unfocusedBorderColor    = cs.outline,
                focusedContainerColor   = cs.surface,
                unfocusedContainerColor = cs.surface,
            ),
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            modifier         = Modifier.background(cs.surface),
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text    = { Text(opt, color = cs.onSurface) },
                    onClick = { onSelect(opt); expanded = false },
                    colors  = MenuDefaults.itemColors(textColor = cs.onSurface),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  INFO TIP CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProxyInfoCard(protocol: String, cs: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cs.primaryContainer.copy(alpha = 0.5f))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Default.Info, null, tint = cs.primary, modifier = Modifier.size(18.dp).padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                append("Ensure your proxy provider supports ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(protocol) }
                append(" protocol before saving. Private proxies usually require IP whitelisting or credentials.")
            },
            fontSize   = 12.sp,
            lineHeight = 18.sp,
            color      = cs.onPrimaryContainer,
        )
    }
}

// end of file
