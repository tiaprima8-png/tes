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
            val spkListener: (Array<Any>) -> Unit = { args ->
                try {
                    val rawFirst = args.getOrNull(0)
                    val data: JSONObject = when (rawFirst) {
                        is JSONObject -> rawFirst
                        is String -> try { JSONObject(rawFirst) } catch (_: Exception) { JSONObject() }
                        else -> JSONObject()
                    }

                    val unitKode = data.optString("unit_kode",
                        data.optString("kode_unit",
                            data.optString("unitCode",
                                data.optString("kodeUnit",
                                    data.optString("unit_code", ""))))).trim()

                    val nomorSpk = data.optString("nomor_spk",
                        data.optString("nomorSpk",
                            data.optString("no_spk",
                                data.optString("noSpk",
                                    data.optString("spk",
                                        data.optString("spk_number", "")))))).trim()

                    val statusSpk = data.optString("status_spk",
                        data.optString("statusSpk",
                            data.optString("status", "SPK_TERBIT"))).trim().ifBlank { "SPK_TERBIT" }

                    val rencanaId = when {
                        data.has("id_rencana_kerja") -> data.optLong("id_rencana_kerja")
                        data.has("idRencanaKerja") -> data.optLong("idRencanaKerja")
                        data.has("rencana_id") -> data.optLong("rencana_id")
                        data.has("rencanaId") -> data.optLong("rencanaId")
                        data.has("plan_id") -> data.optLong("plan_id")
                        data.has("planId") -> data.optLong("planId")
                        data.has("id") -> data.optLong("id")
                        else -> 0L
                    }

                    if (nomorSpk.isNotBlank()) {
                        Log.d("RealtimeSocketManager", "SPK Event Received: unit=$unitKode, spk=$nomorSpk, id=$rencanaId")
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
                    }
                } catch (e: Exception) {
                    Log.e("RealtimeSocketManager", "Error in socket SPK listener", e)
                }
            }

            // Listen to standard and alternative event names from Web Admin
            val eventNames = arrayOf(
                "SPK_PUBLISHED",
                "spk_published",
                "SPK_TERBIT",
                "spk_terbit",
                "UPDATE_SPK",
                "update_spk",
                "spk:published",
                "spk:issued",
                "RENCANA_UPDATED",
                "rencana_updated"
            )
            for (eventName in eventNames) {
                newSocket.on(eventName) { args -> spkListener(args) }
            }

            newSocket.connect()
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Failed to connect WebSocket to $serverUrl", e)
        }
    }

    fun disconnect() {
        try {
            val eventNames = arrayOf(
                "SPK_PUBLISHED",
                "spk_published",
                "SPK_TERBIT",
                "spk_terbit",
                "UPDATE_SPK",
                "update_spk",
                "spk:published",
                "spk:issued",
                "RENCANA_UPDATED",
                "rencana_updated"
            )
            for (eventName in eventNames) {
                socket?.off(eventName)
            }
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
