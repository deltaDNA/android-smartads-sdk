package com.deltadna.android.sdk.ads.core

import android.app.Activity
import android.os.Build
import com.deltadna.android.sdk.ads.core.network.DummyAdapter
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class,
        sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP))
class AdServiceImplTest {
    
    private var activity = Robolectric.buildActivity(Activity::class.java).create()
    private val listener = mock<AdServiceListener>()
    
    private var uut = AdServiceImpl(activity.get(), listener)
    
    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create()
        
        uut = AdServiceImpl(activity.get(), listener)
    }
    
    @After
    fun after() {
        activity.destroy()
        reset(listener)
    }
    
    @Test
    fun adMinimumInterval() {
        withService(Config(
                adShowSession = true,
                adMinimumInterval = DummyAdapter.DISMISS_AFTER / 1000 * 2,
                adMaxPerSession = Int.MAX_VALUE)) {
            // success
            showInterstitialAd(null)
            // fail; too early
            advance(DummyAdapter.DISMISS_AFTER)
            showInterstitialAd(null)
            // success
            advance(DummyAdapter.DISMISS_AFTER)
            showInterstitialAd(null)
            advance(DummyAdapter.DISMISS_AFTER)
            
            inOrder(listener) {
                // success
                verify(listener).onInterstitialAdOpened()
                verify(listener).onInterstitialAdClosed()
                // fail; too early
                verify(listener).onInterstitialAdFailedToOpen(any())
                // success
                verify(listener).onInterstitialAdOpened()
                verify(listener).onInterstitialAdClosed()
            }
        }
    }
    
    private fun withService(
            config: Config = Config(),
            block: AdServiceImpl.() -> Unit) {
        with(config) {
            doAnswer {
                (it.arguments[2] as EngagementListener).onSuccess(
                        jsonObject("parameters" to jsonObject(
                                "adShowSession" to adShowSession,
                                "adFloorPrice" to adFloorPrice,
                                "onDemoteRequestCode" to onDemoteRequestCode,
                                "maxPerNetwork" to maxPerNetwork,
                                "adMinimumInterval" to adMinimumInterval,
                                "adMaxPerSession" to adMaxPerSession,
                                "adProviders" to jsonArray(
                                        *listOf(AdProvider.DUMMY).map {
                                            jsonObject(
                                                    "adProvider" to it.name,
                                                    "eCPM" to Int.MAX_VALUE)
                                        }.toTypedArray()),
                                "adRewardedProviders" to jsonArray(
                                        *listOf(AdProvider.DUMMY).map {
                                            jsonObject(
                                                    "adProvider" to it.name,
                                                    "eCPM" to Int.MAX_VALUE)
                                        }.toTypedArray())))
                                .convert())
            }.whenever(listener).onRequestEngagement(
                    eq(decisionPoint),
                    eq(EngagementFlavour.INTERNAL.toString()),
                    any())
            
            uut.init(decisionPoint)
        }
        
        block.invoke(uut)
    }
    
    private fun advance(time: Int) {
        with(time.toLong()) {
            Robolectric.getForegroundThreadScheduler().advanceBy(this)
            Thread.sleep(this)
        }
    }
    
    private data class Config(
            val decisionPoint: String = "",
            val adShowSession: Boolean = false,
            val adFloorPrice: Int = 0,
            val onDemoteRequestCode: Int = 0,
            val maxPerNetwork: Int = 0,
            val adMinimumInterval: Int = 0,
            val adMaxPerSession: Int = 0)
}
