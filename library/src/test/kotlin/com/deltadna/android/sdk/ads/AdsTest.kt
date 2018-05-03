/*
 * Copyright (c) 2018 deltaDNA Ltd. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deltadna.android.sdk.ads

import com.deltadna.android.sdk.DDNA
import com.deltadna.android.sdk.Engagement
import com.deltadna.android.sdk.ads.core.AdService
import com.deltadna.android.sdk.ads.core.EngagementListener
import com.deltadna.android.sdk.ads.listeners.AdRegistrationListener
import com.deltadna.android.sdk.ads.listeners.InterstitialAdsListener
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener
import com.deltadna.android.sdk.listeners.EngageListener
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.lang.Exception

@RunWith(RobolectricTestRunner::class)
class AdsTest {
    
    private val app = RuntimeEnvironment.application
    
    private lateinit var uut: Ads
    
    private lateinit var settings: Settings
    private lateinit var analytics: DDNA
    private lateinit var service: AdService
    
    @Before
    fun before() {
        settings = mock()
        analytics = mock()
        DDNA.initialise(DDNA.Configuration(app, "envKey", "collUrl", "engUrl"))
        DDNA.instance().inject(analytics)
        
        service = mock()
        uut = Ads(settings, app, null).inject(service)
        DDNASmartAds.initialise(DDNASmartAds.Configuration(app))
        DDNASmartAds.instance().inject(uut)
    }
    
    @After
    fun after() {
        DDNASmartAds.instance().scrubAds()
        DDNA.instance().scrub()
    }
    
    @Test
    fun isAdAllowed() {
        uut.isInterstitialAdAllowed(null, true)
        verify(service).isInterstitialAdAllowed(
                isNull(),
                isNull(),
                eq(true))
        
        with(mock<Engagement<*>>()) {
            whenever(getDecisionPoint()).then { "decisionPoint" }
            uut.isInterstitialAdAllowed(this, false)
            verify(service).isInterstitialAdAllowed(
                    eq("decisionPoint"),
                    isNull(),
                    eq(false))
            
            whenever(getDecisionPoint()).then { "decisionPoint" }
            whenever(getJson()).then { JSONObject("{\"parameters\":{\"a\":1}}") }
            uut.isInterstitialAdAllowed(this, true)
            verify(service).isInterstitialAdAllowed(
                    eq("decisionPoint"),
                    argThat { toString() == "{\"a\":1}" },
                    eq(true))
        }
        
        uut.isRewardedAdAllowed(null, true)
        verify(service).isRewardedAdAllowed(
                isNull(),
                isNull(),
                eq(true))
        
        with(mock<Engagement<*>>()) {
            whenever(getDecisionPoint()).then { "decisionPoint" }
            uut.isRewardedAdAllowed(this, false)
            verify(service).isRewardedAdAllowed(
                    eq("decisionPoint"),
                    isNull(),
                    eq(false))
            
            whenever(getDecisionPoint()).then { "decisionPoint" }
            whenever(getJson()).then { JSONObject("{\"parameters\":{\"a\":1}}") }
            uut.isRewardedAdAllowed(this, true)
            verify(service).isRewardedAdAllowed(
                    eq("decisionPoint"),
                    argThat { toString() == "{\"a\":1}" },
                    eq(true))
        }
    }
    
    @Test
    fun timeUntilRewardedAdAllowed() {
        uut.timeUntilRewardedAdAllowed(null)
        verify(service).timeUntilRewardedAdAllowed(isNull(), isNull())
        
        with(mock<Engagement<*>>()) {
            whenever(getDecisionPoint()).then { "decisionPoint" }
            whenever(getJson()).then { JSONObject("{\"parameters\":{\"a\":1}}") }
            uut.timeUntilRewardedAdAllowed(this)
            verify(service).timeUntilRewardedAdAllowed(
                    eq("decisionPoint"),
                    argThat { toString() == "{\"a\":1}" })
        }
    }
    
    @Test
    fun isAdAvailable() {
        uut.hasLoadedInterstitialAd()
        verify(service).hasLoadedInterstitialAd()
        
        uut.hasLoadedRewardedAd()
        verify(service).hasLoadedRewardedAd()
    }
    
    @Test
    fun showAd() {
        uut.showInterstitialAd(null)
        verify(service).showInterstitialAd(isNull(), isNull())
        
        with(mock<Engagement<*>>()) {
            whenever(getDecisionPoint()).then { "decisionPoint" }
            uut.showInterstitialAd(this)
            verify(service).showInterstitialAd(
                    eq("decisionPoint"),
                    isNull())
            
            whenever(getDecisionPoint()).then { "decisionPoint" }
            whenever(getJson()).then { JSONObject("{\"parameters\":{\"a\":1}}") }
            uut.showInterstitialAd(this)
            verify(service).showInterstitialAd(
                    eq("decisionPoint"),
                    argThat { toString() == "{\"a\":1}" })
        }
        
        uut.showRewardedAd(null)
        verify(service).showRewardedAd(isNull(), isNull())
        
        with(mock<Engagement<*>>()) {
            whenever(getDecisionPoint()).then { "decisionPoint" }
            uut.showRewardedAd(this)
            verify(service).showRewardedAd(
                    eq("decisionPoint"),
                    isNull())
            
            whenever(getDecisionPoint()).then { "decisionPoint" }
            whenever(getJson()).then { JSONObject("{\"parameters\":{\"a\":1}}") }
            uut.showRewardedAd(this)
            verify(service).showRewardedAd(
                    eq("decisionPoint"),
                    argThat { toString() == "{\"a\":1}" })
        }
    }
    
    @Test
    fun getLastShownAndSessionCountAndDailyCount() {
        uut.getLastShown("decisionPoint")
        verify(service).getLastShown(eq("decisionPoint"))
        
        uut.getSessionCount("decisionPoint")
        verify(service).getSessionCount(eq("decisionPoint"))
        
        uut.getDailyCount("decisionPoint")
        verify(service).getDailyCount(eq("decisionPoint"))
    }
    
    @Test
    fun registrationCallbacks() {
        mock<AdRegistrationListener>().run {
            uut.setAdRegistrationListener(this)
            
            uut.onRegisteredForInterstitialAds()
            verify(this).onRegisteredForInterstitial()
            
            uut.onFailedToRegisterForInterstitialAds("reason")
            verify(this).onFailedToRegisterForInterstitial(eq("reason"))
            
            uut.onRegisteredForRewardedAds()
            verify(this).onRegisteredForRewarded()
            
            uut.onFailedToRegisterForRewardedAds("reason")
            verify(this).onFailedToRegisterForRewarded(eq("reason"))
        }
    }
    
    @Test
    fun interstitialCallbacks() {
        whenever(service.isInterstitialAdAllowed(isNull(), isNull(), eq(false)))
                .then { true }
        
        var listener = mock<InterstitialAdsListener>()
        InterstitialAd.create(null, listener).run {
            uut.setInterstitialAd(this)
            
            uut.onInterstitialAdOpened()
            verify(listener).onOpened(same(this))
            
            uut.onInterstitialAdFailedToOpen("reason")
            verify(listener).onFailedToOpen(same(this), eq("reason"))
            
            // reference will be cleared now
            uut.onInterstitialAdClosed()
            verifyNoMoreInteractions(listener)
        }
        
        listener = mock<InterstitialAdsListener>()
        InterstitialAd.create(null, listener).run {
            uut.setInterstitialAd(this)
            
            uut.onInterstitialAdOpened()
            verify(listener).onOpened(same(this))
            
            uut.onInterstitialAdClosed()
            verify(listener).onClosed(same(this))
            
            // reference will be cleared now
            uut.onInterstitialAdFailedToOpen("reason")
            verifyNoMoreInteractions(listener)
        }
        
        listener = mock<InterstitialAdsListener>()
        InterstitialAd.create(null, listener).run {
            uut.setInterstitialAd(this)
            uut.setInterstitialAd(null)
            
            uut.onInterstitialAdOpened()
            verifyZeroInteractions(listener)
        }
    }
    
    @Test
    fun rewardedCallbacks() {
        whenever(service.isRewardedAdAllowed(isNull(), isNull(), eq(false)))
                .then { true }
        
        var listener = mock<RewardedAdsListener>()
        RewardedAd.create(null, listener).run {
            uut.setRewardedAd(this)
            
            uut.onRewardedAdOpened("decisionPoint")
            verify(listener).onOpened(same(this))
            
            uut.onRewardedAdFailedToOpen("reason")
            verify(listener).onFailedToOpen(same(this), eq("reason"))
            
            // reference will be cleared now
            uut.onRewardedAdClosed(true)
            verifyNoMoreInteractions(listener)
        }
        
        listener = mock<RewardedAdsListener>()
        RewardedAd.create(null, listener).run {
            uut.setRewardedAd(this)
            
            uut.onRewardedAdOpened("decisionPoint")
            verify(listener).onOpened(same(this))
            
            uut.onRewardedAdClosed(true)
            verify(listener).onClosed(same(this), eq(true))
            
            // reference will be cleared now
            uut.onRewardedAdFailedToOpen("reason")
            verifyNoMoreInteractions(listener)
        }
        
        listener = mock<RewardedAdsListener>()
        RewardedAd.create(null, listener).run {
            uut.setRewardedAd(this)
            uut.setRewardedAd(null)
            
            uut.onRewardedAdOpened("decisionPoint")
            verifyZeroInteractions(listener)
        }
        
        val ads = arrayOf<RewardedAd>(mock(), mock())
        ads.forEach { uut.registerRewardedAd(it) }
        
        uut.onRewardedAdLoaded()
        ads.forEach { verify(it).onLoaded() }
        
        uut.onRewardedAdOpened("decisionPoint")
        ads.forEach { verify(it).onOpened(eq("decisionPoint")) }
    }
    
    @Test
    fun onRequestEngagement() {
        val listener = mock<EngagementListener>()
        uut.onRequestEngagement("decisionPoint", "flavour", "version", listener)
        
        val eng = argumentCaptor<Engagement<*>>()
        val cbk = argumentCaptor<EngageListener<Engagement<*>>>()
        verify(analytics).requestEngagement(eng.capture(), cbk.capture())
        
        assertThat(eng.firstValue.getDecisionPoint()).isEqualTo("decisionPoint")
        
        cbk.firstValue.apply {
            val engagement = mock<Engagement<*>>()
            
            val result = mock<JSONObject>()
            whenever(engagement.isSuccessful()).then { true }
            whenever(engagement.getJson()).then { result }
            onCompleted(engagement)
            verify(listener).onSuccess(same(result))
            
            whenever(engagement.isSuccessful()).then { false }
            whenever(engagement.getError()).then { "reason" }
            onCompleted(engagement)
            verify(listener).onFailure(argThat { message == "reason" })
            
            val exception = mock<Exception>()
            onError(exception)
            verify(listener).onFailure(same(exception))
        }
    }
    
    @Test
    fun onNewSession() {
        whenever(settings.isUserConsent).then { false }
        whenever(settings.isAgeRestrictedUser).then { true }
        
        uut.onNewSession()
        
        verify(service).registerForAds(eq("advertising"), eq(false), eq(true))
        verify(service).onNewSession()
    }
    
    @Test
    fun onLifecycleCallbacks() {
        uut.onResumed()
        verify(service).onResume()
        
        uut.onPaused()
        verify(service).onPause()
        
        uut.onStopped()
        verify(service).onDestroy()
    }
}