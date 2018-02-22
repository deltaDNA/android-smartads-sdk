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
import com.deltadna.android.sdk.Params
import com.deltadna.android.sdk.listeners.EngageListener
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*
import com.deltadna.android.sdk.EngageFactory.Callback as Cbk

@RunWith(RobolectricTestRunner::class)
class EngageFactoryTest {
    
    private val app = RuntimeEnvironment.application
    
    private lateinit var analytics: DDNA
    private lateinit var ads: Ads
    
    private lateinit var uut: EngageFactory
    
    @Before
    fun before() {
        analytics = mock()
        ads = mock()
        
        uut = EngageFactory(analytics, ads)
        
        DDNA.initialise(DDNA.Configuration(app, "envKey", "collUrl", "engUrl"))
        DDNASmartAds.initialise(DDNASmartAds.Configuration(app))
    }
    
    @Test
    fun requestInterstitialAd() {
        val eng = argumentCaptor<Engagement<*>>()
        val cbk = mock<Cbk<InterstitialAd>>()
        val lastShown = Date()
        whenever(ads.getSessionCount("decisionPoint")).then { 1 }
        whenever(ads.getDailyCount("decisionPoint")).then { 2 }
        whenever(ads.getLastShown("decisionPoint")).then { lastShown }
        doAnswer {
            (it.arguments[1] as EngageListener<Engagement<*>>).onCompleted(
                    it.arguments[0] as Engagement<*>)
            analytics
        }.whenever(analytics).requestEngagement(eng.capture(), any())
        
        uut.requestInterstitialAd("decisionPoint", cbk)
        
        verify(cbk).onCompleted(isNotNull())
        verify(ads, never()).isInterstitialAdAllowed(any(), any())
        
        doAnswer {
            (it.arguments[1] as EngageListener<Engagement<*>>).onError(mock())
            analytics
        }.whenever(analytics).requestEngagement(eng.capture(), any())
        
        uut.requestInterstitialAd("decisionPoint", cbk)
        
        verify(cbk, times(2)).onCompleted(isNotNull())
        verify(ads, never()).isInterstitialAdAllowed(any(), any())
    }
    
    @Test
    fun requestInterstitialAdWithParams() {
        uut.requestInterstitialAd("decisionPoint", Params().put("a", 1), mock())
        verify(analytics).requestEngagement(
                argThat<Engagement<*>> { getDecisionPoint() == "decisionPoint" },
                any())
    }
    
    @Test
    fun requestRewardedAd() {
        val eng = argumentCaptor<Engagement<*>>()
        val cbk = mock<Cbk<RewardedAd>>()
        val lastShown = Date()
        whenever(ads.getSessionCount("decisionPoint")).then { 1 }
        whenever(ads.getDailyCount("decisionPoint")).then { 2 }
        whenever(ads.getLastShown("decisionPoint")).then { lastShown }
        doAnswer {
            (it.arguments[1] as EngageListener<Engagement<*>>).onCompleted(
                    it.arguments[0] as Engagement<*>)
            analytics
        }.whenever(analytics).requestEngagement(eng.capture(), any())
        
        uut.requestRewardedAd("decisionPoint", cbk)
        
        verify(cbk).onCompleted(isNotNull())
        verify(ads, never()).isRewardedAdAllowed(any(), any())
        
        doAnswer {
            (it.arguments[1] as EngageListener<Engagement<*>>).onError(mock())
            analytics
        }.whenever(analytics).requestEngagement(eng.capture(), any())
        
        uut.requestRewardedAd("decisionPoint", cbk)
        
        verify(cbk, times(2)).onCompleted(isNotNull())
        verify(ads, never()).isRewardedAdAllowed(any(), any())
    }
    
    @Test
    fun requestRewardedAdWithParams() {
        uut.requestRewardedAd("decisionPoint", Params().put("a", 1), mock())
        verify(analytics).requestEngagement(
                argThat<Engagement<*>> { getDecisionPoint() == "decisionPoint" },
                any())
    }
}
