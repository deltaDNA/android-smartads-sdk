package com.deltadna.android.sdk.ads

import com.deltadna.android.sdk.DDNA
import com.deltadna.android.sdk.Engagement
import com.deltadna.android.sdk.ads.core.AdService
import org.json.JSONObject
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.staticProperties
import kotlin.reflect.jvm.javaField

private inline fun <reified T: Any> T.inject(what: Any?, where: String): T {
    T::class.memberProperties
            .find { it.name == where }
            ?.javaField
            ?.apply {
                isAccessible = true
                set(this@inject, what)
            }
            ?: T::class
                    .staticProperties
                    .find { it.name == where }
                    ?.javaField
                    ?.apply {
                        isAccessible = true
                        set(this@inject, what)
                    }
    return this
}

internal fun DDNA.inject(with: DDNA?): DDNA = inject(with, "instance")
internal fun DDNA.scrub(): DDNA = inject(null)

internal fun DDNASmartAds.inject(with: Ads?): DDNASmartAds = inject(with, "ads")
internal fun DDNASmartAds.scrubAds(): DDNASmartAds = inject(null)

internal fun Ads.inject(with: AdService?): Ads = inject(with, "service")

internal fun String.json() = JSONObject(this)

class KEngagement(dp: String = "decisionPoint") : Engagement<KEngagement>(dp)
