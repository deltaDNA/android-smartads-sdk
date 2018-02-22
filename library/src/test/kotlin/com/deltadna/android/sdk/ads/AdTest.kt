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
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(RobolectricTestRunner::class)
class AdTest {
    
    private val app = RuntimeEnvironment.application
    
    private lateinit var ads: Ads
    private lateinit var uut: TestAd
    
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
    fun getDecisionPoint() {
        uut = TestAd(null)
        assertThat(uut.decisionPoint).isNull()
        
        uut = TestAd(KEngagement("decisionPoint"))
        assertThat(uut.decisionPoint).isEqualTo("decisionPoint")
    }
    
    @Test
    fun getParameters() {
        uut = TestAd(null)
        assertThat(uut.parameters.length()).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { null }
        })
        assertThat(uut.parameters.length()).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { "{}".json() }
        })
        assertThat(uut.parameters.length()).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { "{\"parameters\":{\"a\":1}}".json() }
        })
        assertThat(uut.parameters.toString()).isEqualTo("{\"a\":1}")
    }
    
    @Test
    fun getLastShown() {
        uut = TestAd(null)
        assertThat(uut.lastShown).isEqualTo(null)
        
        val lastShown = Date()
        whenever(ads.getLastShown(eq("decisionPoint"))).then { lastShown }
        uut = TestAd(KEngagement("decisionPoint"))
        assertThat(uut.lastShown).isSameAs(lastShown)
    }
    
    @Test
    fun getAdShowWaitSecs() {
        uut = TestAd(null)
        assertThat(uut.adShowWaitSecs).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { "{\"parameters\":{}}".json() }
        })
        assertThat(uut.adShowWaitSecs).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { "{\"parameters\":{\"ddnaAdShowWaitSecs\":1}}".json() }
        })
        assertThat(uut.adShowWaitSecs).isEqualTo(1)
    }
    
    @Test
    fun getSessionCount() {
        uut = TestAd(null)
        assertThat(uut.sessionCount).isEqualTo(0)
        
        whenever(ads.getSessionCount(eq("decisionPoint"))).then { 1 }
        uut = TestAd(KEngagement("decisionPoint"))
        assertThat(uut.sessionCount).isEqualTo(1)
    }
    
    @Test
    fun getSessionLimit() {
        uut = TestAd(null)
        assertThat(uut.sessionLimit).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { "{\"parameters\":{}}".json() }
        })
        assertThat(uut.sessionLimit).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { "{\"parameters\":{\"ddnaAdSessionCount\":1}}".json() }
        })
        assertThat(uut.sessionLimit).isEqualTo(1)
    }
    
    @Test
    fun getDailyCount() {
        uut = TestAd(null)
        assertThat(uut.dailyCount).isEqualTo(0)
        
        whenever(ads.getDailyCount(eq("decisionPoint"))).then { 1 }
        uut = TestAd(KEngagement("decisionPoint"))
        assertThat(uut.dailyCount).isEqualTo(1)
    }
    
    @Test
    fun getDailyLimit() {
        uut = TestAd(null)
        assertThat(uut.dailyLimit).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { "{\"parameters\":{}}".json() }
        })
        assertThat(uut.dailyLimit).isEqualTo(0)
        
        uut = TestAd(mock<Engagement<*>>().apply {
            whenever(getJson()).then { "{\"parameters\":{\"ddnaAdDailyCount\":1}}".json() }
        })
        assertThat(uut.dailyLimit).isEqualTo(1)
    }
    
    private class TestAd(engagement: Engagement<*>?) : Ad<TestAd>(engagement) {
        
        override fun isReady() = false
        override fun show() = this
    }
}
