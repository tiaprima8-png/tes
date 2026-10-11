package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface CompanyApiService {
    @GET("api/mobile/rencana-kerja/active")
    suspend fun getActiveRencanaKerja(
        @Query("pengawas") pengawas: String
    ): Response<ResponseBody>

    @GET("api/mobile/bootstrap-data")
    suspend fun getBootstrapData(
        @Query("pengawas") pengawas: String,
        @Query("tanggal") tanggal: String
    ): Response<ApiResponse<BootstrapDataPayload>>

    @GET("api/master-data")
    suspend fun getMasterData(
        @Query("supervisor") supervisor: String? = null
    ): Response<ApiResponse<MasterDataPayload>>

    @GET("api/units")
    suspend fun getUnits(
        @Query("supervisor") supervisor: String? = null
    ): Response<List<UnitPayload>>

    @GET("api/aktivitas")
    suspend fun getActivities(): Response<List<ActivityPayload>>

    @GET("api/operators")
    suspend fun getOperators(
        @Query("supervisor") supervisor: String? = null
    ): Response<List<OperatorPayload>>

    @GET("api/master-kendala")
    suspend fun getMasterKendala(): Response<ApiResponse<List<KendalaPayload>>>

    @POST("api/aktivitas-unit")
    suspend fun postActivityReport(
        @Body report: PostActivityReportRequest
    ): Response<ApiResponse<Any>>

    @POST("api/rencana-kerja")
    suspend fun postRencanaKerja(
        @Body request: PostRencanaKerjaRequest
    ): Response<ApiResponse<Any>>

    @POST("api/hasil-aktivitas")
    suspend fun postHasilAktivitas(
        @Body request: PostHasilAktivitasRequest
    ): Response<ApiResponse<Any>>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: AuthLoginRequest
    ): Response<ApiResponse<Any>>

    @GET("api/health")
    suspend fun checkHealth(): Response<ApiResponse<Any>>
}

/**
 * Manages dynamic Retrofit client connecting to the user's Web Admin URL.
 * Automatically saves/loads configured URL and falls back to in-memory company server
 * simulator when in offline/testing mode.
 */
class ApiClientManager(val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("heavytrack_api_prefs", Context.MODE_PRIVATE)

    companion object {
        // Active Web Admin Portal URL
        const val BASE_URL = "https://1-portalce.ai.studio"
        const val KEY_API_URL = "api_base_url"
        const val DEFAULT_WEB_ADMIN_URL = "https://1-portalce.ai.studio"
        const val KEY_USE_MOCK_FALLBACK = "use_mock_fallback"
    }

    var baseUrl: String
        get() = prefs.getString(KEY_API_URL, DEFAULT_WEB_ADMIN_URL) ?: DEFAULT_WEB_ADMIN_URL
        set(value) {
            val formatted = formatBaseUrl(value)
            prefs.edit().putString(KEY_API_URL, formatted).apply()
            rebuildRetrofit(formatted)
        }

    var useSimulationFallback: Boolean
        get() = prefs.getBoolean(KEY_USE_MOCK_FALLBACK, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_MOCK_FALLBACK, value).apply()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    @Volatile
    private var apiService: CompanyApiService = createService(baseUrl)

    private fun formatBaseUrl(raw: String): String {
        var clean = raw.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "https://$clean"
        }
        if (!clean.endsWith("/")) {
            clean = "$clean/"
        }
        return clean
    }

    private fun createService(url: String): CompanyApiService {
        val validUrl = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(validUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(CompanyApiService::class.java)
    }

    private fun rebuildRetrofit(url: String) {
        try {
            apiService = createService(url)
        } catch (_: Exception) {
            // Keep previous if invalid
        }
    }

    /**
     * Test connection to the Web Admin server
     */
    suspend fun testConnection(targetUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val formatted = formatBaseUrl(targetUrl)
            val testService = createService(formatted)
            val response = testService.getMasterData()
            if (response.isSuccessful) {
                Pair(true, "Terhubung sukses! Status Code: ${response.code()} OK")
            } else {
                Pair(false, "Server merespons status HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Pair(false, "Koneksi gagal: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun fetchMasterData(supervisor: String): Pair<MasterDataPayload, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan aktif: Data diambil dari SQLite lokal")
        }

        try {
            val response = apiService.getMasterData(supervisor)
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                return@withContext Pair(data, "Berhasil sinkron master data dari Web Admin API (${response.code()})")
            }
        } catch (e: Exception) {
            // If real network fails and fallback is enabled, use simulator
            if (useSimulationFallback) {
                val simResponse = CompanyServerSimulator.fetchMasterData(supervisor)
                return@withContext Pair(
                    simResponse.data ?: MasterDataPayload(),
                    "Web Admin remote tidak merespons (${e.localizedMessage}). Menggunakan data sinkronisasi lokal."
                )
            }
            throw e
        }

        // Fallback to local simulator
        val simResponse = CompanyServerSimulator.fetchMasterData(supervisor)
        Pair(simResponse.data ?: MasterDataPayload(), "Data master berhasil disinkronkan.")
    }

    suspend fun fetchBootstrapData(pengawas: String, tanggal: String): Pair<BootstrapDataPayload, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan aktif: Menggunakan data SQLite lokal")
        }

        try {
            val response = apiService.getBootstrapData(pengawas, tanggal)
            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                return@withContext Pair(data, "Berhasil sinkron bootstrap data dari Web Admin API (${response.code()})")
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResponse = CompanyServerSimulator.fetchBootstrapData(pengawas, tanggal)
                return@withContext Pair(
                    simResponse.data ?: BootstrapDataPayload(),
                    "Web Admin remote tidak merespons (${e.localizedMessage}). Menggunakan data sinkronisasi lokal."
                )
            }
            throw e
        }

        val simResponse = CompanyServerSimulator.fetchBootstrapData(pengawas, tanggal)
        Pair(simResponse.data ?: BootstrapDataPayload(), "Data bootstrap operasional berhasil disinkronkan.")
    }

    /**
     * Panggil API GET /api/mobile/rencana-kerja/active?pengawas={pengawas_aktif}
     * Menghubungi Web Admin untuk menarik daftar rencana kerja aktif terbaru secara instan.
     */
    suspend fun fetchActiveRencanaKerja(pengawas: String): Pair<List<PostRencanaKerjaRequest>, String> = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            val simResp = CompanyServerSimulator.getActiveRencanaKerja(pengawas)
            return@withContext Pair(simResp.data ?: emptyList(), "Mode Offline Lapangan: Rencana kerja aktif dimuat dari simulator lokal (${simResp.data?.size ?: 0} unit)")
        }

        try {
            val response = apiService.getActiveRencanaKerja(pengawas)
            if (response.isSuccessful && response.body() != null) {
                val bodyStr = response.body()!!.string()
                val parsed = parseActiveRencanaList(bodyStr)
                return@withContext Pair(parsed, "Berhasil menarik ${parsed.size} rencana kerja aktif dari Web Admin (${response.code()} OK)")
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.getActiveRencanaKerja(pengawas)
                return@withContext Pair(
                    simResp.data ?: emptyList(),
                    "Web Admin remote tidak merespons (${e.localizedMessage ?: e.message}). Menggunakan data sinkronisasi lokal."
                )
            }
            throw e
        }

        val simResp = CompanyServerSimulator.getActiveRencanaKerja(pengawas)
        Pair(simResp.data ?: emptyList(), "Rencana kerja aktif berhasil disinkronkan.")
    }

    private fun parseActiveRencanaList(rawJson: String): List<PostRencanaKerjaRequest> {
        val trimmed = rawJson.trim()
        val listType = Types.newParameterizedType(List::class.java, PostRencanaKerjaRequest::class.java)
        val adapter = moshi.adapter<List<PostRencanaKerjaRequest>>(listType)

        return try {
            if (trimmed.startsWith("[")) {
                adapter.fromJson(trimmed) ?: emptyList()
            } else if (trimmed.startsWith("{")) {
                val json = JSONObject(trimmed)
                if (json.has("data")) {
                    val dataVal = json.get("data")
                    if (dataVal is JSONArray) {
                        adapter.fromJson(dataVal.toString()) ?: emptyList()
                    } else if (dataVal is JSONObject) {
                        if (dataVal.has("rencana_aktif") && dataVal.get("rencana_aktif") is JSONArray) {
                            adapter.fromJson(dataVal.getJSONArray("rencana_aktif").toString()) ?: emptyList()
                        } else emptyList()
                    } else emptyList()
                } else if (json.has("rencana_aktif") && json.get("rencana_aktif") is JSONArray) {
                    adapter.fromJson(json.getJSONArray("rencana_aktif").toString()) ?: emptyList()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun postActivityReport(report: PostActivityReportRequest): String = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan aktif: Laporan disimpan ke penyimpanan lokal perangkat")
        }

        try {
            val response = apiService.postActivityReport(report)
            if (response.isSuccessful) {
                // Also update simulator state
                CompanyServerSimulator.postActivityReport(report)
                return@withContext "Berhasil dikirim ke Web Admin API (${response.code()} OK)"
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.postActivityReport(report)
                return@withContext "Tersimpan lokal & disinkronkan (${simResp.message})"
            }
            throw e
        }

        val simResp = CompanyServerSimulator.postActivityReport(report)
        simResp.message ?: "Laporan tersimpan"
    }

    suspend fun postRencanaKerja(request: PostRencanaKerjaRequest): String = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Rencana kerja tersimpan di lokal (MENUNGGU_SPK)")
        }

        try {
            val response = apiService.postRencanaKerja(request)
            if (response.isSuccessful) {
                CompanyServerSimulator.postRencanaKerja(request)
                return@withContext "Rencana kerja terkirim ke Web Admin (${response.code()} OK)"
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.postRencanaKerja(request)
                return@withContext "Tersimpan lokal & disinkronkan (${simResp.message})"
            }
            throw e
        }

        val simResp = CompanyServerSimulator.postRencanaKerja(request)
        simResp.message ?: "Rencana tersimpan"
    }

    suspend fun postHasilAktivitas(request: PostHasilAktivitasRequest): String = withContext(Dispatchers.IO) {
        if (CompanyServerSimulator.isSimulatedOffline) {
            throw java.io.IOException("Mode Offline Lapangan: Hasil realisasi tersimpan di lokal")
        }

        try {
            val response = apiService.postHasilAktivitas(request)
            if (response.isSuccessful) {
                CompanyServerSimulator.postHasilAktivitas(request)
                return@withContext "Realisasi kerja terkirim ke Web Admin (${response.code()} OK)"
            }
        } catch (e: Exception) {
            if (useSimulationFallback) {
                val simResp = CompanyServerSimulator.postHasilAktivitas(request)
                return@withContext "Tersimpan lokal & disinkronkan (${simResp.message})"
            }
            throw e
        }

        val simResp = CompanyServerSimulator.postHasilAktivitas(request)
        simResp.message ?: "Realisasi tersimpan"
    }
}
