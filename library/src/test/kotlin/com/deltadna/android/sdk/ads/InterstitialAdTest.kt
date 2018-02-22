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
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class InterstitialAdTest {
    
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
            InterstitialAd.create()
            verify(ads).isInterstitialAdAllowed(isNull(), eq(false))
            
            InterstitialAd.create(this)
            verify(ads).isInterstitialAdAllowed(same(this), eq(false))
        }
    }
    
    @Test
    fun createdWhenAllowed() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull(), any())).then { true }
        
        var ad = InterstitialAd.create()!!
        assertThat(ad).isNotNull()
        assertThat(ad.engagement).isNull()
        
        with(mock<Engagement<*>>()) {
            ad = InterstitialAd.create(this)!!
            assertThat(ad).isNotNull()
            assertThat(ad.engagement).isSameAs(this)
        }
    }
    
    @Test
    fun notCreatedWhenDisallowed() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull(), any())).then { false }
        
        assertThat(InterstitialAd.create()).isNull()
        assertThat(InterstitialAd.create(mock<Engagement<*>>())).isNull()
    }
    
    @Test
    fun create() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull(), any()))
                .thenReturn(true, false, true, false)
        
        assertThat(InterstitialAd.create()).isNotNull()
        assertThat(InterstitialAd.create()).isNull()
        assertThat(InterstitialAd.create(KEngagement())).isNotNull()
        assertThat(InterstitialAd.create(KEngagement())).isNull()
    }
    
    @Test
    fun createUnchecked() {
        val ad = InterstitialAd.createUnchecked(
                mock<Engagement<*>>().apply {
                    whenever(getJson()).then { JSONObject() }
                },
                mock())
        
        assertThat(ad).isNotNull()
        assertThat(ad.engagement).isNotNull()
        verifyZeroInteractions(ads)
    }
    
    @Test
    fun createUncheckedWithInvalidEngagement() {
        InterstitialAd.createUnchecked(
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
        whenever(ads.isInterstitialAdAllowed(anyOrNull(), any())).then { true }
        
        InterstitialAd.create(mock<Engagement<*>>())!!.isReady
        
        verify(ads).hasLoadedInterstitialAd()
    }
    
    @Test
    fun show() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull(), any())).then { true }
        
        InterstitialAd.create()!!.show()
        verify(ads).showInterstitialAd(isNull())
        
        with(mock<Engagement<*>>()) {
            InterstitialAd.create(this)!!.show()
            verify(ads).showInterstitialAd(same(this))
        }
    }
}
