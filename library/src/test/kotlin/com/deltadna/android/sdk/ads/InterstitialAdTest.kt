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

import com.deltadna.android.sdk.Engagement
import com.deltadna.android.sdk.ads.listeners.InterstitialAdsListener
import com.github.salomonbrys.kotson.jsonObject
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class InterstitialAdTest {
    
    private var ads = mock<Ads>()
    
    @Before
    fun before() {
        DDNASmartAds.instance().inject(ads)
    }
    
    @After
    fun after() {
        DDNASmartAds.instance().scrubAds()
        reset(ads)
    }
    
    @Test
    fun createChecksIfAllowed() {
        with(KEngagement()) {
            InterstitialAd.create()
            InterstitialAd.create(this)
            
            verify(ads).isInterstitialAdAllowed(isNull())
            verify(ads).isInterstitialAdAllowed(same(this))
        }
    }
    
    @Test
    fun createdWhenAllowed() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull())).then { true }
        
        assertThat(InterstitialAd.create()!!.params).isNull()
        
        with(mock<Engagement<*>>()) {
            whenever(this.getJson()).then { null }
            
            assertThat(InterstitialAd.create(this)!!.params).isNull()
        }
        
        with(mock<Engagement<*>>()) {
            whenever(getJson()).then { JSONObject() }
            
            assertThat(InterstitialAd.create(this)!!.params).isNull()
        }
        
        with(mock<Engagement<*>>()) {
            whenever(getJson()).then {
                jsonObject("parameters" to jsonObject()).convert()
            }
            
            assertThat(InterstitialAd.create(this)!!.params!!.toString())
                    .isEqualTo(JSONObject().toString())
        }
    }
    
    @Test
    fun notCreatedWhenDisallowed() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull())).then { false }
        
        assertThat(InterstitialAd.create()).isNull()
        assertThat(InterstitialAd.create(mock<Engagement<*>>())).isNull()
    }
    
    @Test
    fun create() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull()))
                .thenReturn(true, false, true, false)
        
        assertThat(InterstitialAd.create()).isNotNull()
        assertThat(InterstitialAd.create()).isNull()
        assertThat(InterstitialAd.create(KEngagement())).isNotNull()
        assertThat(InterstitialAd.create(KEngagement())).isNull()
    }
    
    @Test
    fun isReady() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull())).then { true }
        
        InterstitialAd.create()!!.isReady
        
        verify(ads).isInterstitialAdAvailable
    }
    
    @Test
    fun show() {
        whenever(ads.isInterstitialAdAllowed(anyOrNull())).then { true }
        
        with(mock<InterstitialAdsListener>()) {
            InterstitialAd.create(this)!!.show()
            
            verify(ads).setInterstitialAdsListener(same(this))
            verify(ads).showInterstitialAd(isNull())
        }
    }
}
