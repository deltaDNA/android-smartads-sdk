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

package com.deltadna.android.sdk.ads.core

import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.util.*

@RunWith(RobolectricTestRunner::class)
class AdMetricsTest {
    
    private var prefs = RuntimeEnvironment.application.getSharedPreferences(
            javaClass.name, Context.MODE_PRIVATE)
    private var uut = AdMetrics(prefs)
    
    @Before
    fun before() {
        uut = AdMetrics(prefs)
    }
    
    @After
    fun after() {
        prefs.edit().clear().apply()
    }
    
    @Test
    fun recordShowingAd() {
        val dp1 = "decisionPoint1"
        
        assertThat(uut.lastShown(dp1)).isNull()
        assertThat(uut.sessionCount(dp1)).isEqualTo(0)
        assertThat(uut.dailyCount(dp1)).isEqualTo(0)
        
        val date1 = Date()
        uut.recordAdShown(dp1, date1)
        
        assertThat(uut.lastShown(dp1)).isEqualTo(date1)
        assertThat(uut.sessionCount(dp1)).isEqualTo(1)
        assertThat(uut.dailyCount(dp1)).isEqualTo(1)
        
        val date1a = Date()
        uut.recordAdShown(dp1, date1a)
        
        assertThat(uut.lastShown(dp1)).isEqualTo(date1a)
        assertThat(uut.sessionCount(dp1)).isEqualTo(2)
        assertThat(uut.dailyCount(dp1)).isEqualTo(2)
        
        val dp2 = "decisionPoint2"
        val date2 = Date()
        
        uut.recordAdShown(dp2, date2)
        
        assertThat(uut.lastShown(dp2)).isEqualTo(date2)
        assertThat(uut.sessionCount(dp2)).isEqualTo(1)
        assertThat(uut.dailyCount(dp2)).isEqualTo(1)
        
        assertThat(uut.lastShown(dp1)).isEqualTo(date1a)
        assertThat(uut.sessionCount(dp1)).isEqualTo(2)
        assertThat(uut.dailyCount(dp1)).isEqualTo(2)
    }
    
    @Test
    fun resetSessionCounts() {
        val dp = "decisionPoint"
        val date1 = Date()
        
        uut.recordAdShown(dp, date1)
        
        assertThat(uut.lastShown(dp)).isEqualTo(date1)
        assertThat(uut.sessionCount(dp)).isEqualTo(1)
        assertThat(uut.dailyCount(dp)).isEqualTo(1)
        
        uut.newSession(Date())
        
        assertThat(uut.lastShown(dp)).isEqualTo(date1)
        assertThat(uut.sessionCount(dp)).isEqualTo(0)
        assertThat(uut.dailyCount(dp)).isEqualTo(1)
        
        uut.recordAdShown(dp, Date())
        val date2 = Date()
        uut.recordAdShown(dp, date2)
        
        assertThat(uut.lastShown(dp)).isEqualTo(date2)
        assertThat(uut.sessionCount(dp)).isEqualTo(2)
        assertThat(uut.dailyCount(dp)).isEqualTo(3)
        
        val date3 = Date()
        uut.newSession(date3)
        
        assertThat(uut.lastShown(dp)).isEqualTo(date2)
        assertThat(uut.sessionCount(dp)).isEqualTo(0)
        assertThat(uut.dailyCount(dp)).isEqualTo(3)
    }
    
    @Test
    fun resetDailyCountsOnNextDayNewSession() {
        val dp = "decisionPoint"
        val date = Date()
        
        uut.recordAdShown(dp, date)
        
        assertThat(uut.lastShown(dp)).isEqualTo(date)
        assertThat(uut.sessionCount(dp)).isEqualTo(1)
        assertThat(uut.dailyCount(dp)).isEqualTo(1)
        
        val nextDay = Date.from(Instant.now().plus(1, DAYS))
        uut.newSession(nextDay)
        
        assertThat(uut.lastShown(dp)).isEqualTo(date)
        assertThat(uut.sessionCount(dp)).isEqualTo(0)
        assertThat(uut.dailyCount(dp)).isEqualTo(0)
        
        uut.recordAdShown(dp, nextDay)
        
        assertThat(uut.lastShown(dp)).isEqualTo(nextDay)
        assertThat(uut.sessionCount(dp)).isEqualTo(1)
        assertThat(uut.dailyCount(dp)).isEqualTo(1)
    }
    
    @Test
    fun resetDailyCountsOnNextDaySameSession() {
        val dp = "decisionPoint"
        val date = Date()
        
        uut.recordAdShown(dp, date)
        
        assertThat(uut.lastShown(dp)).isEqualTo(date)
        assertThat(uut.sessionCount(dp)).isEqualTo(1)
        assertThat(uut.dailyCount(dp)).isEqualTo(1)
        
        val nextDay = Date.from(Instant.now().plus(1, DAYS))
        uut.recordAdShown(dp, nextDay)
        
        assertThat(uut.lastShown(dp)).isEqualTo(nextDay)
        assertThat(uut.sessionCount(dp)).isEqualTo(2)
        assertThat(uut.dailyCount(dp)).isEqualTo(2)
        
        uut.newSession(nextDay)
        
        assertThat(uut.lastShown(dp)).isEqualTo(nextDay)
        assertThat(uut.sessionCount(dp)).isEqualTo(0)
        assertThat(uut.dailyCount(dp)).isEqualTo(0)
        
        uut.recordAdShown(dp, nextDay)
        
        assertThat(uut.lastShown(dp)).isEqualTo(nextDay)
        assertThat(uut.sessionCount(dp)).isEqualTo(1)
        assertThat(uut.dailyCount(dp)).isEqualTo(1)
    }
}
