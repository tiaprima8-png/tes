package com.example.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RealtimeSpkData(
    val unitKode: String,
    val nomorSpk: String,
    val statusSpk: String = "SPK_TERBIT",
    val rencanaId: Long = 0L,
    val serverPlanId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Real-time Native WebSocket Client connected to Web Admin (/ws).
 * Listens directly to standard WebSocket broadcasts:
 * - SPK_PUBLISHED / SPK_TERBIT / UPDATE_RENCANA / RENCANA_STATUS_UPDATED
 * and dispatches them to Room SQLite and UI.
 */
class RealtimeSocketManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        const val BASE_URL = "https://1-portalce.ai.studio"
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Infinite for persistent WebSocket
        .build()

    private var webSocket: WebSocket? = null
    private var isManuallyClosed = false

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _spkPublishedEvents = MutableSharedFlow<RealtimeSpkData>(extraBufferCapacity = 64)
    val spkPublishedEvents = _spkPublishedEvents.asSharedFlow()

    var onConnectedCallback: (() -> Unit)? = null

    private fun extractFirstNonEmpty(vararg sources: JSONObject?, keys: Array<String>): String {
        for (src in sources) {
            if (src == null) continue
            for (key in keys) {
                if (src.has(key) && !src.isNull(key)) {
                    val str = src.optString(key, "").trim()
                    if (str.isNotBlank() && !str.equals("null", ignoreCase = true)) {
                        return str
                    }
                }
            }
        }
        return ""
    }

    fun connect(serverUrl: String = BASE_URL, onSpkReceived: ((RealtimeSpkData) -> Unit)? = null) {
        disconnect()
        isManuallyClosed = false

        try {
            val cleanUrl = if (serverUrl.isBlank()) BASE_URL else serverUrl.trim().removeSuffix("/")
            val wsUrl = when {
                cleanUrl.startsWith("wss://") -> if (cleanUrl.endsWith("/ws")) cleanUrl else "$cleanUrl/ws"
                cleanUrl.startsWith("ws://") -> if (cleanUrl.endsWith("/ws")) cleanUrl else "$cleanUrl/ws"
                cleanUrl.startsWith("https://") -> cleanUrl.replace("https://", "wss://") + "/ws"
                cleanUrl.startsWith("http://") -> cleanUrl.replace("http://", "ws://") + "/ws"
                else -> "wss://$cleanUrl/ws"
            }

            Log.d("RealtimeSocketManager", "🔌 Connecting native WebSocket to: $wsUrl")

            val request = Request.Builder()
                .url(wsUrl)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("RealtimeSocketManager", "✅ Native WebSocket connected to Web Admin: $wsUrl")
                    _isConnected.value = true
                    try {
                        onConnectedCallback?.invoke()
                    } catch (e: Exception) {
                        Log.e("RealtimeSocketManager", "Error in onConnectedCallback", e)
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d("RealtimeSocketManager", "📩 Received WS message: $text")
                    try {
                        val root = JSONObject(text)
                        val event = root.optString("event", root.optString("type", ""))

                        // Extract data payload (support nested data or payload objects)
                        val dataObj = root.optJSONObject("data")
                        val payloadObj = root.optJSONObject("payload")

                        val nomorSpk = extractFirstNonEmpty(
                            dataObj, payloadObj, root,
                            keys = arrayOf("nomor_spk", "nomorSpk", "no_spk", "spk", "spk_number", "noSpk")
                        )

                        val unitKode = extractFirstNonEmpty(
                            dataObj, payloadObj, root,
                            keys = arrayOf("kode_unit", "unit_kode", "unit", "kodeUnit", "unitKode", "nama_unit")
                        )

                        val statusSpkRaw = extractFirstNonEmpty(
                            dataObj, payloadObj, root,
                            keys = arrayOf("status_spk", "statusSpk", "status", "status_tahap")
                        )
                        val statusSpk = if (statusSpkRaw.isNotBlank() && !statusSpkRaw.equals("MENUNGGU_SPK", ignoreCase = true)) {
                            statusSpkRaw
                        } else {
                            "SPK_TERBIT"
                        }

                        val rawId = extractFirstNonEmpty(
                            dataObj, payloadObj, root,
                            keys = arrayOf("id", "id_rencana_kerja", "rencana_id", "plan_id", "id_rencana")
                        )

                        val numericId = rawId.filter { it.isDigit() }.toLongOrNull() ?: 0L

                        val isSpkRelated = event.equals("SPK_PUBLISHED", ignoreCase = true) ||
                                event.equals("SPK_TERBIT", ignoreCase = true) ||
                                event.equals("UPDATE_RENCANA", ignoreCase = true) ||
                                event.equals("RENCANA_STATUS_UPDATED", ignoreCase = true) ||
                                event.equals("RENCANA_KERJA_UPDATED", ignoreCase = true) ||
                                nomorSpk.isNotBlank()

                        if (isSpkRelated && nomorSpk.isNotBlank()) {
                            Log.d("RealtimeSocketManager", "🎯 SPK Event: unit=$unitKode, spk=$nomorSpk, id=$rawId, event=$event")
                            val spkData = RealtimeSpkData(
                                unitKode = unitKode,
                                nomorSpk = nomorSpk,
                                statusSpk = statusSpk,
                                rencanaId = numericId,
                                serverPlanId = rawId
                            )
                            scope.launch {
                                _spkPublishedEvents.emit(spkData)
                            }
                            onSpkReceived?.invoke(spkData)
                        }
                    } catch (e: Exception) {
                        Log.e("RealtimeSocketManager", "Error parsing WebSocket message", e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("RealtimeSocketManager", "WebSocket closing: $code / $reason")
                    _isConnected.value = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("RealtimeSocketManager", "WebSocket closed: $code / $reason")
                    _isConnected.value = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w("RealtimeSocketManager", "WebSocket connection failure: ${t.localizedMessage}")
                    _isConnected.value = false
                    // Auto-reconnect after 3 seconds
                    if (!isManuallyClosed) {
                        scope.launch {
                            delay(3000)
                            if (!isManuallyClosed) {
                                connect(serverUrl, onSpkReceived)
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Failed to connect WebSocket to $serverUrl", e)
        }
    }

    fun dispatchSpkPublished(unitKode: String, nomorSpk: String, rencanaId: Long) {
        val event = RealtimeSpkData(
            unitKode = unitKode,
            nomorSpk = nomorSpk,
            rencanaId = rencanaId
        )
        scope.launch {
            _spkPublishedEvents.emit(event)
        }
    }

    fun disconnect() {
        isManuallyClosed = true
        try {
            webSocket?.close(1000, "Normal Closure")
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Error while closing WebSocket", e)
        } finally {
            webSocket = null
            _isConnected.value = false
        }
    }
}
