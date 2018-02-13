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
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener
import com.github.salomonbrys.kotson.jsonObject
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
class RewardedAdTest {
    
    private val app = RuntimeEnvironment.application
    
    private val ads = mock<Ads>()
    
    @Before
    fun before() {
        DDNA.initialise(DDNA.Configuration(app, "envKey", "collUrl", "engUrl"))
        DDNASmartAds.initialise(DDNASmartAds.Configuration(app))
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
            RewardedAd.create()
            RewardedAd.create(this)
            
            verify(ads).isRewardedAdAllowed(isNull())
            verify(ads).isRewardedAdAllowed(same(this))
        }
    }
    
    @Test
    fun createdWhenAllowed() {
        whenever(ads.isRewardedAdAllowed(anyOrNull())).then { true }
        
        assertThat(RewardedAd.create()!!.params).isNull()
        
        with(mock<Engagement<*>>()) {
            whenever(this.getJson()).then { null }
            
            assertThat(RewardedAd.create(this)!!.params).isNull()
        }
        
        with(mock<Engagement<*>>()) {
            whenever(getJson()).then { JSONObject() }
            
            assertThat(RewardedAd.create(this)!!.params).isNull()
        }
        
        with(mock<Engagement<*>>()) {
            whenever(getJson()).then {
                jsonObject("parameters" to jsonObject()).convert()
            }
            
            assertThat(RewardedAd.create(this)!!.params!!.toString())
                    .isEqualTo(JSONObject().toString())
        }
    }
    
    @Test
    fun notCreatedWhenDisallowed() {
        whenever(ads.isRewardedAdAllowed(anyOrNull())).then { false }
        
        assertThat(RewardedAd.create()).isNull()
        assertThat(RewardedAd.create(mock<Engagement<*>>())).isNull()
    }
    
    @Test
    fun create() {
        whenever(ads.isRewardedAdAllowed(anyOrNull()))
                .thenReturn(true, false, true, false)
        
        assertThat(RewardedAd.create()).isNotNull()
        assertThat(RewardedAd.create()).isNull()
        assertThat(RewardedAd.create(KEngagement())).isNotNull()
        assertThat(RewardedAd.create(KEngagement())).isNull()
    }
    
    @Test
    fun isReady() {
        whenever(ads.isRewardedAdAllowed(anyOrNull())).then { true }
        
        RewardedAd.create()!!.isReady
        
        verify(ads).isRewardedAdAvailable
    }
    
    @Test
    fun show() {
        whenever(ads.isRewardedAdAllowed(anyOrNull())).then { true }
        
        with(mock<RewardedAdsListener>()) {
            RewardedAd.create(this)!!.show()
            
            verify(ads).setRewardedAdsListener(same(this))
            verify(ads).showRewardedAd(isNull())
        }
    }
}
