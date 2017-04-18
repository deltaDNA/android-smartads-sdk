package com.deltadna.android.sdk.ads.core

import android.app.Activity
import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.core.network.DummyAdapter
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
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
    fun configurationWithoutAds() {
        listOf( Config(adShowSession = false),
                Config( interstitial = emptyList(),
                        rewarded = emptyList())).forEach {
            withService(it) {
                verify(listener).onFailedToRegisterForInterstitialAds(any())
                verify(listener).onFailedToRegisterForRewardedAds(any())
                
                assertThat(isInterstitialAdAllowed(null, null)).isFalse()
                assertThat(isRewardedAdAllowed(null, null)).isFalse()
                
                assertThat(isInterstitialAdAvailable).isFalse()
                assertThat(isRewardedAdAvailable).isFalse()
                
                showInterstitialAd(null)
                verify(listener).onInterstitialAdFailedToOpen(any())
                showRewardedAd(null)
                verify(listener).onRewardedAdFailedToOpen(any())
                
                reset(listener)
            }
        }
    }
    
    @Test
    fun isInterstitialAdAllowed() {
        withService(Config(adShowSession = true)) {
            assertThat(isInterstitialAdAllowed(null, null)).isTrue()
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                with(toJson()) {
                    has("adPoint").not() &&
                    get("adStatus").string == AdShowResult.FULFILLED.status &&
                    get("adType").string == "INTERSTITIAL"
                }
            })
            
            assertThat(isRewardedAdAllowed(null, null)).isTrue()
            verify(listener).onRecordEvent(eq("adShow"), argThat {
                with(toJson()) {
                    has("adPoint").not() &&
                    get("adStatus").string == AdShowResult.FULFILLED.status &&
                    get("adType").string == "REWARDED"
                }
            })
        }
    }
    
    @Test
    fun isInterstitialAdAllowedWithDecisionPoint() {
        withService(Config(adShowSession = true)) {
            assertThat(isInterstitialAdAllowed("point", null)).isTrue()
            assertThat(isRewardedAdAllowed("point", null)).isTrue()
            
            verify(listener, times(2)).onRecordEvent(eq("adShow"), argThat {
                toJson().get("adPoint").string == "point"
            })
        }
    }
    
    @Test
    fun isInterstitialAdAllowedWithEmptyParameters() {
        withService(Config(adShowSession = true)) {
            assertThat(isInterstitialAdAllowed("point", JSONObject())).isTrue()
            assertThat(isRewardedAdAllowed("point", JSONObject())).isTrue()
        }
    }
    
    @Test
    fun isInterstitialAdNotAllowed() {
        withService(Config(adShowSession = true)) {
            assertThat(isInterstitialAdAllowed(
                    "point",
                    jsonObject("adShowPoint" to false).convert())).isFalse()
            assertThat(isRewardedAdAllowed(
                    "point",
                    jsonObject("adShowPoint" to false).convert())).isFalse()
            
            verify(listener, times(2)).onRecordEvent(eq("adShow"), argThat {
                with(toJson()) {
                    get("adPoint").string == "point" &&
                    get("adStatus").string == AdShowResult.AD_SHOW_POINT.status
                }
            })
        }
    }
    
    @Test
    fun showAd() {
        withService(Config(adShowSession = true)) {
            showInterstitialAd(null)
            showRewardedAd(null)
            advance(DummyAdapter.DISMISS_AFTER)
            
            inOrder(listener) {
                verify(listener).onInterstitialAdOpened()
                verify(listener).onRewardedAdOpened()
                verify(listener).onInterstitialAdClosed()
                verify(listener).onRewardedAdClosed(any())
            }
            
            verify(listener, times(2)).onRecordEvent(eq("adClosed"), argThat {
                toJson().get("adStatus").string == AdClosedResult.SUCCESS.status
            })
        }
    }
    
    @Test
    fun showAdLegacy() {
        withService(Config(adShowSession = true)) {
            showInterstitialAd("point")
            showRewardedAd("point")
            
            verify(listener, times(2)).onRequestEngagement(
                    eq("point"),
                    eq(EngagementFlavour.ADVERTISING.toString()),
                    any())
        }
    }
    
    @Test
    fun adMinimumInterval() {
        withService(Config(
                adShowSession = true,
                adMinimumInterval = DummyAdapter.DISMISS_AFTER / 1000 * 2,
                adMaxPerSession = Int.MAX_VALUE)) {
            // success
            assertThat(isInterstitialAdAllowed(null, null)).isTrue()
            assertThat(isRewardedAdAllowed(null, null)).isTrue()
            showInterstitialAd(null)
            showRewardedAd(null)
            
            // fail; too early
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isInterstitialAdAllowed(null, null)).isFalse()
            assertThat(isRewardedAdAllowed(null, null)).isFalse()
            showInterstitialAd(null)
            showRewardedAd(null)
            
            // success
            advance(DummyAdapter.DISMISS_AFTER)
            assertThat(isInterstitialAdAllowed(null, null)).isTrue()
            assertThat(isRewardedAdAllowed(null, null)).isTrue()
            showInterstitialAd(null)
            showRewardedAd(null)
            advance(DummyAdapter.DISMISS_AFTER)
            
            inOrder(listener) {
                // success
                verify(listener).onInterstitialAdOpened()
                verify(listener).onRewardedAdOpened()
                verify(listener).onInterstitialAdClosed()
                verify(listener).onRewardedAdClosed(any())
                
                // fail; too early
                verify(listener).onInterstitialAdFailedToOpen(any())
                verify(listener).onRewardedAdFailedToOpen(any())
                
                // success
                verify(listener).onInterstitialAdOpened()
                verify(listener).onRewardedAdOpened()
                verify(listener).onInterstitialAdClosed()
                verify(listener).onRewardedAdClosed(any())
            }
        }
    }
    
    @Test
    fun adMaxPerSession() {
        withService(Config(adShowSession = true, adMaxPerSession = 1)) {
            showInterstitialAd(null)
            showRewardedAd(null)
            
            showInterstitialAd(null)
            showRewardedAd(null)
            
            verify(listener).onInterstitialAdFailedToOpen(any())
            verify(listener).onRewardedAdFailedToOpen(any())
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
                                "adDemoteOnRequestCode" to onDemoteRequestCode,
                                "adMaxPerNetwork" to maxPerNetwork,
                                "adMinimumInterval" to adMinimumInterval,
                                "adMaxPerSession" to adMaxPerSession,
                                "adProviders" to jsonArray(
                                        *interstitial.map { jsonObject(
                                                "adProvider" to it.name,
                                                "eCPM" to Int.MAX_VALUE)
                                        }.toTypedArray()),
                                "adRewardedProviders" to jsonArray(
                                        *rewarded.map { jsonObject(
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
            Robolectric
                    .getForegroundThreadScheduler()
                    .advanceBy(this, TimeUnit.MILLISECONDS)
            Thread.sleep(this)
        }
    }
    
    private data class Config(
            val decisionPoint: String = "",
            val adShowSession: Boolean = false,
            val adFloorPrice: Int = 0,
            val onDemoteRequestCode: Int = 0,
            val maxPerNetwork: Int = 0,
            val adMinimumInterval: Int = -1,
            val adMaxPerSession: Int = -1,
            val interstitial: List<AdProvider> = listOf(AdProvider.DUMMY),
            val rewarded: List<AdProvider> = listOf(AdProvider.DUMMY))
}
