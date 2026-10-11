package com.example.data.remote

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI

data class RealtimeSpkData(
    val unitKode: String,
    val nomorSpk: String,
    val statusSpk: String = "SPK_TERBIT",
    val rencanaId: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Real-time WebSocket Client using Socket.IO to receive live updates from Web Admin.
 * Listens directly to "SPK_PUBLISHED" events and dispatches them to Room and UI.
 */
class RealtimeSocketManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        const val BASE_URL = "https://1-portalce.ai.studio"
    }

    private var socket: Socket? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _spkPublishedEvents = MutableSharedFlow<RealtimeSpkData>(extraBufferCapacity = 64)
    val spkPublishedEvents = _spkPublishedEvents.asSharedFlow()

    fun connect(serverUrl: String = BASE_URL, onSpkReceived: ((RealtimeSpkData) -> Unit)? = null) {
        disconnect()
        try {
            val cleanUrl = if (serverUrl.isBlank()) BASE_URL else serverUrl.trim().removeSuffix("/")
            val uri = URI.create(cleanUrl)
            val options = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1500
                timeout = 20000
                transports = arrayOf("websocket", "polling")
                forceNew = true
            }
            val newSocket = IO.socket(uri, options)
            socket = newSocket

            newSocket.on(Socket.EVENT_CONNECT) {
                Log.d("RealtimeSocketManager", "WebSocket connected to Web Admin: $cleanUrl")
                _isConnected.value = true
            }

            newSocket.on(Socket.EVENT_DISCONNECT) {
                Log.d("RealtimeSocketManager", "WebSocket disconnected")
                _isConnected.value = false
            }

            newSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.w("RealtimeSocketManager", "WebSocket connect error: ${args.getOrNull(0)}")
                _isConnected.value = false
            }

            // ==============================================================
            // WEBSOCKET LISTENER REAL-TIME (SPK_PUBLISHED)
            // ==============================================================
            newSocket.on("SPK_PUBLISHED") { args ->
                try {
                    val data = args[0] as JSONObject
                    val unitKode = data.optString("unit_kode", data.optString("kode_unit", ""))
                    val nomorSpk = data.optString("nomor_spk", "")
                    val statusSpk = data.optString("status_spk", "SPK_TERBIT")
                    val rencanaId = data.optInt("id_rencana_kerja").toLong()

                    val event = RealtimeSpkData(
                        unitKode = unitKode,
                        nomorSpk = nomorSpk,
                        statusSpk = statusSpk,
                        rencanaId = rencanaId
                    )

                    scope.launch {
                        _spkPublishedEvents.emit(event)
                    }
                    onSpkReceived?.invoke(event)
                } catch (e: Exception) {
                    Log.e("RealtimeSocketManager", "Error in socket.on(SPK_PUBLISHED)", e)
                }
            }

            newSocket.connect()
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Failed to connect WebSocket to $serverUrl", e)
        }
    }

    /**
     * Helper to route an SPK event directly through the socket pipeline
     */
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
        try {
            socket?.off("SPK_PUBLISHED")
            socket?.off()
            socket?.disconnect()
            socket?.close()
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Error while disconnecting socket", e)
        } finally {
            socket = null
            _isConnected.value = false
        }
    }
}
