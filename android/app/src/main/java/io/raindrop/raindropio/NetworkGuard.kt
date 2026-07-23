package io.raindrop.raindropio

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
import com.facebook.react.modules.network.CookieJarContainer
import com.facebook.react.modules.network.OkHttpClientProvider
import java.io.File
import java.io.IOException
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Interceptor

/**
 * OkHttp factory for all RN networking. Two responsibilities:
 *
 * 1. App User-Agent. OkHttp's default "okhttp/4.x" UA is a canonical bot
 *    signature: api.raindrop.io is hosted on DO App Platform whose CDN is
 *    Cloudflare (not configurable by us), and its bot scoring intermittently
 *    answers such clients with a 403 challenge depending on IP reputation —
 *    seen by users as "can't connect" while their internet is fine.
 *
 * 2. Cookie safety. okhttp 4.x crashes on malformed (empty-name) cookies that
 *    Chromium WebView occasionally writes to the shared Android cookie store:
 *    JavaNetCookieJar.decodeHeaderAsJavaNetCookies throws
 *    StringIndexOutOfBoundsException on a "Cookie: =value" header, the throw
 *    escapes to the OkHttp Dispatcher worker and the default
 *    uncaught-exception handler kills the process. CookieManager exposes no
 *    API to delete a single empty-name cookie, so:
 *
 *    - [sanitizeDb] — at app startup, before WebView locks the SQLite file,
 *      drop every empty-name row from Chromium's cookies database. Clears
 *      persistent rot from prior sessions so the first request never trips.
 *
 *    - [SafeContainer] — the OkHttp CookieJarContainer for RN networking.
 *      When the jar throws mid-session (SQLite cleanup is too late — the
 *      in-memory cache still holds the bad row), re-reads the Cookie header
 *      straight from CookieManager and parses it pair-by-pair, skipping only
 *      the malformed entries. This keeps the auth cookie on the request —
 *      sending it cookieless would make the server answer auth:false, which
 *      the app treats as a logout and wipes local state. If the cookie store
 *      itself is unreachable (e.g. WebView provider mid-update), the request
 *      fails with IOException — a retryable network error JS backoff handles —
 *      instead of going out unauthenticated.
 *
 * Call [install] once from MainApplication.onCreate before loadReactNative.
 */
object NetworkGuard {
    private const val TAG = "NetworkGuard"

    private val USER_AGENT: String =
        "Raindrop/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})"
            .filter { it.code in 0x20..0x7e }

    fun install(context: Context) {
        sanitizeDb(context)
        OkHttpClientProvider.setOkHttpClientFactory {
            OkHttpClientProvider.createClientBuilder(context)
                .cookieJar(SafeContainer())
                .addInterceptor(Interceptor { chain ->
                    val request = chain.request()
                    chain.proceed(
                        if (request.header("User-Agent") == null)
                            request.newBuilder().header("User-Agent", USER_AGENT).build()
                        else
                            request
                    )
                })
                .build()
        }
    }

    /**
     * Removes malformed cookies (rows with empty name) from Chromium's cookie
     * SQLite database. Best-effort: schema mismatch, lock contention, missing
     * table — ignored. Must run before WebView initializes (which would lock
     * the file and load rows into an in-memory cache we can't reach).
     */
    private fun sanitizeDb(context: Context) {
        val file = listOf(
            File(context.dataDir, "app_webview/Default/Cookies"),
            File(context.dataDir, "app_webview/Cookies"),
        ).firstOrNull { it.isFile } ?: return

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
            val deleted = db.delete("cookies", "name IS NULL OR name = ''", null)
            if (deleted > 0) Log.w(TAG, "Removed $deleted malformed cookies from ${file.path}")
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to sanitize cookies db at ${file.path}", t)
        } finally {
            try { db?.close() } catch (_: Throwable) {}
        }
    }

    /**
     * Defensive replacement for ReactCookieJarContainer. Mirrors the upstream
     * impl (per-cookie validation via Headers.Builder) but survives a throwing
     * underlying jar — see class kdoc for the recovery strategy.
     */
    private class SafeContainer : CookieJarContainer {
        private var jar: CookieJar? = null

        override fun setCookieJar(cookieJar: CookieJar) { jar = cookieJar }
        override fun removeCookieJar() { jar = null }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            try {
                jar?.saveFromResponse(url, cookies)
            } catch (t: Throwable) {
                Log.w(TAG, "saveFromResponse threw, writing cookies directly for $url", t)
                //best-effort direct write so a session cookie is not silently lost
                for (cookie in cookies)
                    try { CookieManager.getInstance().setCookie(url.toString(), cookie.toString()) } catch (_: Throwable) {}
                try { CookieManager.getInstance().flush() } catch (_: Throwable) {}
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val source = try {
                jar?.loadForRequest(url) ?: return emptyList()
            } catch (t: Throwable) {
                Log.w(TAG, "loadForRequest threw, falling back to manual parse for $url", t)
                fallbackLoad(url)
            }

            val out = ArrayList<Cookie>(source.size)
            for (cookie in source) {
                try {
                    Headers.Builder().add(cookie.name, cookie.value)
                    out.add(cookie)
                } catch (_: IllegalArgumentException) {}
            }
            return out
        }

        /**
         * Reads the Cookie header straight from CookieManager and parses it
         * pair-by-pair, dropping malformed entries that JavaNetCookieJar's
         * all-or-nothing parse chokes on.
         */
        private fun fallbackLoad(url: HttpUrl): List<Cookie> {
            val header = try {
                CookieManager.getInstance().getCookie(url.toString())
            } catch (t: Throwable) {
                throw IOException("cookie store unavailable for $url", t)
            } ?: return emptyList()

            val out = ArrayList<Cookie>()
            for (pair in header.split(";")) {
                val idx = pair.indexOf('=')
                if (idx < 0) continue
                val name = pair.substring(0, idx).trim()
                val value = pair.substring(idx + 1).trim()
                if (name.isEmpty()) continue
                try {
                    out.add(Cookie.Builder().name(name).value(value).domain(url.host).build())
                } catch (_: Throwable) {}
            }
            return out
        }
    }
}
