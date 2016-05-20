package com.deltadna.android.sdk.ads

import com.deltadna.android.sdk.Engagement
import com.google.gson.JsonObject
import org.json.JSONObject
import kotlin.reflect.jvm.javaField
import kotlin.reflect.memberProperties

internal fun DDNASmartAds.inject(with: Ads?) {
    with(DDNASmartAds::class
            .memberProperties
            .find { it.name == "ads" }!!
            .javaField!!) {
        isAccessible = true
        set(this@inject, with)
    }
}

internal fun DDNASmartAds.scrubAds() {
    inject(null)
}

fun JsonObject.convert(): JSONObject {
    return JSONObject(toString())
}

class KEngagement(dp: String = "decisionPoint") : Engagement<KEngagement>(dp)
