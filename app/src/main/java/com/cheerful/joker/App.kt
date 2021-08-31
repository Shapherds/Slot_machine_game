package com.cheerful.joker

import android.app.Application
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import java.util.*

class App : Application() {
    var isNonOrganic = false
    var campaing = ""
    override fun onCreate() {
        super.onCreate()
        val conversionListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: Map<String, Any>) {
                Log.e("LOG_TAG", "attribute:  = $conversionData")
                for (attrName in conversionData.keys) {
                    if (attrName == "af_status" && Objects.requireNonNull(
                            conversionData[attrName]
                        ).toString() == "Non-organic"
                    ) {
                        isNonOrganic = true
                    }
                    if (attrName == "campaign") {
                       campaing = Objects.requireNonNull(conversionData[attrName]).toString()
                    }
                }
                if (isNonOrganic) {
                    MainActivity.communications(campaing)
                } else {
                    MainActivity.communications("")
                }
            }

            override fun onConversionDataFail(errorMessage: String) {}
            override fun onAppOpenAttribution(attributionData: Map<String, String>) {}
            override fun onAttributionFailure(errorMessage: String) {}
        }
        AppsFlyerLib.getInstance().init(AppConstants.KEY.data, conversionListener, this)
        AppsFlyerLib.getInstance().start(this)
    }
}