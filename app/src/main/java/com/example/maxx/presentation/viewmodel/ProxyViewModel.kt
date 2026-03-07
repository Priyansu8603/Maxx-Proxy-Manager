package com.example.maxx.presentation.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maxx.data.repository.ProxyRepository
import com.example.maxx.domain.models.ConnectionState
import com.example.maxx.domain.models.ProxyProfile
import com.example.maxx.domain.models.ProxyStats
import com.example.maxx.domain.models.ProxyTestResult
import com.example.maxx.domain.usecase.proxy.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
//  One-way UI events (navigation, snackbar, etc.)
//  Industry pattern: ViewModel never holds NavController reference.
//  Screens collect this channel in LaunchedEffect → navigate.
// ─────────────────────────────────────────────────────────────────────────────
sealed interface UiEvent {
    data class Navigate(val route: String) : UiEvent
    data class ShowSnackbar(val message: String) : UiEvent
    object NavigateBack : UiEvent
}

@HiltViewModel
class ProxyViewModel @Inject constructor(
    private val repository: ProxyRepository,
    private val getAllProxiesUseCase: GetAllProxiesUseCase,
    private val insertProxyUseCase: InsertProxyUseCase,
    private val updateProxyUseCase: UpdateProxyUseCase,
    private val deleteProxyUseCase: DeleteProxyUseCase,
    private val deleteMultipleProxiesUseCase: DeleteMultipleProxiesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val testProxyConnectionUseCase: TestProxyConnectionUseCase,
    private val performProxyTestUseCase: PerformProxyTestUseCase,
    private val exportProxiesUseCase: ExportProxiesUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    private var lastConnectingProxy: ProxyProfile? = null

    private val _connectivityMessage = MutableStateFlow<String?>(null)
    val connectivityMessage: StateFlow<String?> = _connectivityMessage

    private val _testResult = MutableStateFlow<ProxyTestResult?>(null)
    val testResult: StateFlow<ProxyTestResult?> = _testResult

    // ── Per-proxy last test results (survives sheet dismissal) ────────────────
    //  Key = proxyId. Updated every time a test completes.
    //  Screen reads this to show persistent dot colour + latency on each card.
    private val _lastTestResults = MutableStateFlow<Map<Int, ProxyTestResult>>(emptyMap())
    val lastTestResults: StateFlow<Map<Int, ProxyTestResult>> = _lastTestResults.asStateFlow()

    val proxyStats: StateFlow<ProxyStats> = repository.proxyStats
    val proxyLogs: StateFlow<List<String>> = repository.proxyLogs
    val errorMessage: StateFlow<String?> = repository.errorMessage

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _connectionState.value = state
            }
        }
    }

    val allProxies: StateFlow<List<ProxyProfile>> = getAllProxiesUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun insertProxy(profile: ProxyProfile) {
        viewModelScope.launch {
            insertProxyUseCase(profile)
        }
    }

    fun deleteProxy(profile: ProxyProfile) {
        viewModelScope.launch {
            deleteProxyUseCase(profile)
        }
    }

    // ── Undo-delete support ───────────────────────────────────────────────────
    //  Flow carries the proxy that was just soft-deleted (still DB-gone).
    //  Screen shows snackbar; if user taps Undo within 5 s we re-insert.
    //  After 5 s the auto-clear job fires and we null out the state.
    private val _pendingDeleteProxy = MutableStateFlow<ProxyProfile?>(null)
    val pendingDeleteProxy: StateFlow<ProxyProfile?> = _pendingDeleteProxy.asStateFlow()

    /** Remaining seconds of the undo window (0..5). Drives the countdown ring. */
    private val _undoSecondsLeft = MutableStateFlow(0)
    val undoSecondsLeft: StateFlow<Int> = _undoSecondsLeft.asStateFlow()

    private var undoDeleteJob: Job? = null

    /** Deletes the proxy immediately and opens a 5-second undo window. */
    fun deleteProxyWithUndo(profile: ProxyProfile) {
        viewModelScope.launch {
            deleteProxyUseCase(profile)
            _pendingDeleteProxy.value = profile

            // Cancel any running countdown so we start fresh
            undoDeleteJob?.cancel()
            undoDeleteJob = launch {
                for (remaining in 5 downTo 1) {
                    _undoSecondsLeft.value = remaining
                    delay(1_000)
                }
                // Window expired — just clear the pending state
                _pendingDeleteProxy.value = null
                _undoSecondsLeft.value = 0
            }
        }
    }

    /** Re-inserts the proxy that was soft-deleted. Cancels the countdown. */
    fun undoDelete() {
        undoDeleteJob?.cancel()
        undoDeleteJob = null
        val proxy = _pendingDeleteProxy.value ?: return
        viewModelScope.launch {
            // Re-insert with id=0 so Room auto-generates a new id
            insertProxyUseCase(proxy.copy(id = 0))
            _pendingDeleteProxy.value = null
            _undoSecondsLeft.value = 0
        }
    }

    /** Dismiss the snackbar without undoing (countdown cancelled early). */
    fun dismissUndoSnackbar() {
        undoDeleteJob?.cancel()
        undoDeleteJob = null
        _pendingDeleteProxy.value = null
        _undoSecondsLeft.value = 0
    }

    fun deleteMultiple(ids: List<Int>) {
        viewModelScope.launch {
            deleteMultipleProxiesUseCase(ids)
        }
    }

    fun updateProxy(profile: ProxyProfile) {
        viewModelScope.launch {
            updateProxyUseCase(profile)
        }
    }

    val proxiesGroupedByLocation = allProxies.map { proxies ->
        proxies.groupBy { it.name }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    fun getLastConnectedProxy(): ProxyProfile? = lastConnectingProxy

    fun setConnectionState(state: ConnectionState) {
        viewModelScope.launch { _connectionState.emit(state) }
    }

    fun toggleFavorite(proxyId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(proxyId, isFavorite)
        }
    }

    private val _proxyState = MutableStateFlow<ProxyState>(ProxyState.Idle)
    val proxyState: StateFlow<ProxyState> = _proxyState

    sealed class ProxyState {
        object Idle : ProxyState()
        object Testing : ProxyState()
        data class Success(val responseTimeMs: Long) : ProxyState()
        data class Error(val message: String) : ProxyState()
    }

    fun testProxyConnection(proxyHost: String, proxyPort: Int, testUrl: String) {
        viewModelScope.launch {
            _proxyState.value = ProxyState.Testing
            when (val result = testProxyConnectionUseCase(proxyHost, proxyPort, testUrl)) {
                is TestProxyConnectionUseCase.ProxyTestResult.Success ->
                    _proxyState.value = ProxyState.Success(result.responseTimeMs)
                is TestProxyConnectionUseCase.ProxyTestResult.Error ->
                    _proxyState.value = ProxyState.Error(result.message)
            }
        }
    }

    fun performFullProxyTest(proxy: ProxyProfile) {
        viewModelScope.launch {
            _proxyState.value = ProxyState.Testing
            _testResult.value = null
            val result = performProxyTestUseCase(
                proxyId = proxy.id,
                proxyHost = proxy.ip,
                proxyPort = proxy.port,
                proxyType = proxy.protocol
            )
            _testResult.value = result
            // ── Persist last result per proxy (survives sheet close) ──────
            _lastTestResults.value = _lastTestResults.value + (proxy.id to result)
            _proxyState.value = if (result.success)
                ProxyState.Success(result.latencyMs)
            else
                ProxyState.Error(result.errorMessage ?: "Unknown error")
        }
    }

    fun clearTestResult() {
        _testResult.value = null
        _proxyState.value = ProxyState.Idle
    }

    fun exportProxies(proxies: List<ProxyProfile>) {
        viewModelScope.launch {
            when (val result = exportProxiesUseCase(proxies)) {
                is ExportProxiesUseCase.ExportResult.Success -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Exported ${proxies.size} proxies to ${result.file.absolutePath}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                is ExportProxiesUseCase.ExportResult.Error -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Export failed: ${result.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
