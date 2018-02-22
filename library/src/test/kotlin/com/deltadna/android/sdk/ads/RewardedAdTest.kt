/*
 * Copyright (c) 2016 deltaDNA Ltd. All rights reserved.
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
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class RewardedAdTest {
    
    private val app = RuntimeEnvironment.application
    
    private lateinit var ads: Ads
    
    @Before
    fun before() {
        ads = mock()
        
        DDNA.initialise(DDNA.Configuration(app, "envKey", "collUrl", "engUrl"))
        DDNASmartAds.initialise(DDNASmartAds.Configuration(app))
        DDNASmartAds.instance().inject(ads)
    }
    
    @After
    fun after() {
        DDNASmartAds.instance().scrubAds()
    }
    
    @Test
    fun createChecksIfAllowed() {
        with(KEngagement()) {
            RewardedAd.create()
            verify(ads).isRewardedAdAllowed(isNull(), eq(false))
            
            RewardedAd.create(this)
            verify(ads).isRewardedAdAllowed(same(this), eq(false))
        }
    }
    
    @Test
    fun createdWhenAllowed() {
        whenever(ads.isRewardedAdAllowed(anyOrNull(), any())).then { true }
        
        var ad = RewardedAd.create()!!
        assertThat(ad).isNotNull()
        assertThat(ad.engagement).isNull()
        
        with(mock<Engagement<*>>()) {
            ad = RewardedAd.create(this)!!
            assertThat(ad).isNotNull()
            assertThat(ad.engagement).isSameAs(this)
        }
    }
    
    @Test
    fun notCreatedWhenDisallowed() {
        whenever(ads.isRewardedAdAllowed(anyOrNull(), any())).then { false }
        
        assertThat(RewardedAd.create()).isNull()
        assertThat(RewardedAd.create(mock<Engagement<*>>())).isNull()
    }
    
    @Test
    fun create() {
        whenever(ads.isRewardedAdAllowed(anyOrNull(), any()))
                .thenReturn(true, false, true, false)
        
        assertThat(RewardedAd.create()).isNotNull()
        assertThat(RewardedAd.create()).isNull()
        assertThat(RewardedAd.create(KEngagement())).isNotNull()
        assertThat(RewardedAd.create(KEngagement())).isNull()
    }
    
    @Test
    fun createUncheckedAndRegistration() {
        val ad = RewardedAd.createUnchecked(
                mock<Engagement<*>>().apply {
                    whenever(getJson()).then { JSONObject() }
                },
                mock())
        
        assertThat(ad).isNotNull()
        assertThat(ad.engagement).isNotNull()
        verify(ads).registerRewardedAd(same(ad))
        verifyNoMoreInteractions(ads)
    }
    
    @Test
    fun createUncheckedWithInvalidEngagement() {
        RewardedAd.createUnchecked(
                mock<Engagement<*>>().apply {
                    whenever(getJson()).then { null }
                },
                mock()).apply {
            assertThat(engagement).isNull()
            assertThat(parameters.length()).isEqualTo(0)
        }
    }
    
    @Test
    fun isReady() {
        whenever(ads.isRewardedAdAllowed(anyOrNull(), any())).then { true }
        
        RewardedAd.create(mock<Engagement<*>>())!!.isReady
        
        verify(ads).hasLoadedRewardedAd()
    }
    
    @Test
    fun show() {
        whenever(ads.isRewardedAdAllowed(anyOrNull(), any())).then { true }
        
        RewardedAd.create()!!.show()
        verify(ads).showRewardedAd(isNull())
        
        with(mock<Engagement<*>>()) {
            RewardedAd.create(this)!!.show()
            
            verify(ads).showRewardedAd(same(this))
        }
    }
}

/**
 * Separate test cases to allow for a different setup/teardown.
 */
@RunWith(RobolectricTestRunner::class)
class RewardedAdConcurrentTest {
    
    private val app = RuntimeEnvironment.application
    
    private var analytics = mock<DDNA>()
    private var service = mock<AdService>()
    private var ads = mock<Ads>() // use mock to avoid ctor exception
    
    @Before
    fun before() {
        analytics = mock()
        DDNA.initialise(DDNA.Configuration(app, "envKey", "collUrl", "engUrl"))
        DDNA.instance().inject(analytics)
        
        service = mock()
        ads = Ads(app, null).inject(service)
        DDNASmartAds.initialise(DDNASmartAds.Configuration(app))
        DDNASmartAds.instance().inject(ads)
    }
    
    @After
    fun after() {
        DDNASmartAds.instance().scrubAds()
        DDNA.instance().scrub()
    }
    
    @Test
    fun multipleAdsCanCoexist() {
        whenever(service.isRewardedAdAllowed(eq("dp1"), isNull(), eq(false)))
                .then { true }
        val eng1 = KEngagement("dp1")
        val cbk1 = mock<RewardedAdsListener>()
        val ad1 = RewardedAd.create(eng1, cbk1)!!
        
        whenever(service.isRewardedAdAllowed(
                eq("dp2"),
                isNull(),
                eq(false)))
                .then { true }
        val eng2 = KEngagement("dp2")
        val cbk2 = mock<RewardedAdsListener>()
        val ad2 = RewardedAd.create(eng2, cbk2)!!
        
        whenever(service.isRewardedAdAllowed(eq("dp1"), isNull(), eq(true)))
                .then { true }
        whenever(service.isRewardedAdAllowed(eq("dp2"), isNull(), eq(true)))
                .then { true }
        ads.onRewardedAdLoaded()
        
        verify(cbk1).onLoaded(same(ad1))
        verify(cbk2).onLoaded(same(ad2))
        
        whenever(service.showRewardedAd(eq("dp1"), isNull())).then {
            ads.onRewardedAdOpened("dp1")
        }
        ad1.show()
        
        inOrder(cbk1, cbk2) {
            verify(cbk1).onOpened(same(ad1))
            verify(cbk2).onExpired(same(ad2))
        }
        verify(cbk1, never()).onExpired(any())
        
        ads.onRewardedAdClosed(true)
        
        verify(cbk1).onClosed(same(ad1), eq(true))
        
        ads.onRewardedAdLoaded()
        
        verify(cbk1, times(2)).onLoaded(same(ad1))
        verify(cbk2, times(2)).onLoaded(same(ad2))
        
        whenever(service.showRewardedAd(eq("dp2"), isNull())).then {
            ads.onRewardedAdOpened("dp2")
        }
        ad2.show()
        
        inOrder(cbk1, cbk2) {
            verify(cbk2).onOpened(same(ad2))
            verify(cbk1).onExpired(same(ad1))
        }
        verify(cbk2, times(1)).onExpired(any())
        
        ads.onRewardedAdClosed(true)
        
        verify(cbk2).onClosed(same(ad2), eq(true))
        
        ads.onRewardedAdLoaded()
        
        verify(cbk1, times(3)).onLoaded(same(ad1))
        verify(cbk2, times(3)).onLoaded(same(ad2))
    }
    
    /**
     * The params returned by Engage would have 'adShowWaitSecs' set in this
     * use cases, however we're not using them here as the AdService which would
     * read them is mocked out.
     */
    @Test
    fun reportsLoadedAfterWaitTime() {
        whenever(service.isRewardedAdAllowed(eq("dp"), isNull(), eq(false)))
                .then { true }
        val eng = KEngagement("dp")
        val cbk = mock<RewardedAdsListener>()
        val ad = RewardedAd.create(eng, cbk)!!
        
        whenever(service.isRewardedAdAllowed(eq("dp"), isNull(), eq(true)))
                .then { false }
        whenever(service.timeUntilRewardedAdAllowed(eq("dp"), isNull()))
                .then { 5 }
        ads.onRewardedAdLoaded()
        
        verify(cbk, never()).onLoaded(same(ad))
        
        whenever(service.hasLoadedRewardedAd()).then { true }
        RuntimeEnvironment.getMasterScheduler().advanceBy(5, TimeUnit.SECONDS)
        
        verify(cbk).onLoaded(same(ad))
    }
    
    @Test
    fun multipleAdsCanCoexistWithWaitTimes() {
        whenever(service.isRewardedAdAllowed(eq("dp1"), isNull(), eq(false)))
                .then { true }
        val eng1 = KEngagement("dp1")
        val cbk1 = mock<RewardedAdsListener>()
        val ad1 = RewardedAd.create(eng1, cbk1)!!
        
        whenever(service.isRewardedAdAllowed(eq("dp2"), isNull(), eq(false)))
                .then { true }
        val eng2 = KEngagement("dp2")
        val cbk2 = mock<RewardedAdsListener>()
        val ad2 = RewardedAd.create(eng2, cbk2)!!
        
        whenever(service.isRewardedAdAllowed(eq("dp1"), isNull(), eq(true)))
                .then { true }
        whenever(service.isRewardedAdAllowed(eq("dp2"), isNull(), eq(true)))
                .then { true }
        ads.onRewardedAdLoaded()
        
        verify(cbk1, times(1)).onLoaded(same(ad1))
        verify(cbk2, times(1)).onLoaded(same(ad2))
        
        // show ad1
        whenever(service.showRewardedAd(eq("dp1"), isNull())).then {
            ads.onRewardedAdOpened("dp1")
            ads.onRewardedAdClosed(true)
        }
        ad1.show()
        
        inOrder(cbk1, cbk2) {
            verify(cbk1, times(1)).onOpened(same(ad1))
            verify(cbk2, times(1)).onExpired(same(ad2))
            verify(cbk1, times(1)).onClosed(same(ad1), eq(true))
        }
        verify(cbk1, never()).onExpired(same(ad1))
        
        // ad1 needs to wait 2 secs
        whenever(service.isRewardedAdAllowed(eq("dp1"), isNull(), eq(true)))
                .then { false }
        whenever(service.timeUntilRewardedAdAllowed(eq("dp1"), isNull()))
                .then { 2 }
        // ad2 can be shown
        whenever(service.isRewardedAdAllowed(eq("dp2"), isNull(), eq(true)))
                .then { true }
        ads.onRewardedAdLoaded()
        
        verify(cbk2, times(2)).onLoaded(same(ad2))
        verify(cbk1, times(1)).onLoaded(same(ad1))
        verify(cbk1, never()).onExpired(same(ad1))
        
        // show ad2
        whenever(service.showRewardedAd(eq("dp2"), isNull())).then {
            ads.onRewardedAdOpened("dp2")
            ads.onRewardedAdClosed(true)
        }
        ad2.show()
        
        inOrder(cbk2) {
            verify(cbk2).onOpened(same(ad2))
            verify(cbk2).onClosed(same(ad2), eq(true))
        }
        verify(cbk1, never()).onExpired(same(ad1))
        verify(cbk2, times(1)).onExpired(same(ad2))
        
        // ad2 needs to wait 4 secs
        whenever(service.isRewardedAdAllowed(eq("dp2"), isNull(), eq(true)))
                .then { false }
        whenever(service.timeUntilRewardedAdAllowed(eq("dp2"), isNull()))
                .then { 4 }
        ads.onRewardedAdLoaded()
        
        verify(cbk1, times(1)).onLoaded(same(ad1))
        verify(cbk2, times(2)).onLoaded(same(ad2))
        
        // ad1 becomes ready after 2 secs
        whenever(service.hasLoadedRewardedAd()).then { true }
        RuntimeEnvironment.getMasterScheduler().advanceBy(2, TimeUnit.SECONDS)
        
        verify(cbk1, times(2)).onLoaded(same(ad1))
        verify(cbk2, times(2)).onLoaded(same(ad2))
        
        // show ad1
        whenever(service.showRewardedAd(eq("dp1"), isNull())).then {
            ads.onRewardedAdOpened("dp1")
            ads.onRewardedAdClosed(true)
        }
        ad1.show()
        
        inOrder(cbk1) {
            verify(cbk1, times(2)).onOpened(same(ad1))
            verify(cbk1).onClosed(same(ad1), eq(true))
        }
        verify(cbk1, never()).onExpired(same(ad1))
        verify(cbk2, times(1)).onExpired(same(ad2))
        
        // ad1 and ad2 are waiting
        ads.onRewardedAdLoaded()
        
        verify(cbk1, times(2)).onLoaded(same(ad1))
        verify(cbk2, times(2)).onLoaded(same(ad2))
        
        // ad1 and ad2 become ready after 2 secs
        whenever(service.hasLoadedRewardedAd()).then { true }
        RuntimeEnvironment.getMasterScheduler().advanceBy(2, TimeUnit.SECONDS)
        
        verify(cbk1, times(3)).onLoaded(same(ad1))
        verify(cbk2, times(3)).onLoaded(same(ad2))
        
        // show ad2
        whenever(service.showRewardedAd(eq("dp2"), isNull())).then {
            ads.onRewardedAdOpened("dp2")
            ads.onRewardedAdClosed(true)
        }
        ad2.show()
        
        inOrder(cbk1, cbk2) {
            verify(cbk2, times(2)).onOpened(same(ad2))
            verify(cbk1, times(1)).onExpired(same(ad1))
            verify(cbk2).onClosed(same(ad2), eq(true))
        }
        verify(cbk2, times(1)).onExpired(same(ad2))
    }
    
    @Test
    fun getRewardType() {
        var uut = RewardedAd.createUnchecked(null, null)
        assertThat(uut!!.rewardType).isNull()
        
        uut = RewardedAd.createUnchecked(mock<Engagement<*>>().apply {
            whenever(getJson()).thenReturn("{\"parameters\":{}}".json())
        }, null)
        assertThat(uut!!.rewardType).isNull()
        
        uut = RewardedAd.createUnchecked(mock<Engagement<*>>().apply {
            whenever(getJson()).thenReturn(
                    "{\"parameters\":{\"ddnaAdRewardType\":\"type\"}}".json())
        }, null)
        assertThat(uut!!.rewardType).isEqualTo("type")
    }
    
    @Test
    fun getRewardAmount() {
        var uut = RewardedAd.createUnchecked(null, null)
        assertThat(uut!!.rewardAmount).isEqualTo(0)
        
        uut = RewardedAd.createUnchecked(mock<Engagement<*>>().apply {
            whenever(getJson()).thenReturn("{\"parameters\":{}}".json())
        }, null)
        assertThat(uut!!.rewardAmount).isEqualTo(0)
        
        uut = RewardedAd.createUnchecked(mock<Engagement<*>>().apply {
            whenever(getJson()).thenReturn(
                    "{\"parameters\":{\"ddnaAdRewardAmount\":1}}".json())
        }, null)
        assertThat(uut!!.rewardAmount).isEqualTo(1)
    }
}
