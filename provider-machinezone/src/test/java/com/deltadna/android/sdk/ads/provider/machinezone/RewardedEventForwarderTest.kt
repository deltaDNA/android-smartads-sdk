/*
 * Copyright (c) 2017 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.machinezone

import com.fractionalmedia.sdk.AdRequest
import com.fractionalmedia.sdk.AdZoneError
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RewardedEventForwarderTest {
    
    private val delegate = mock<EventForwarder>()
    
    private var uut = RewardedEventForwarder(delegate)
    
    @Before
    fun before() {
        uut = RewardedEventForwarder(delegate)
    }
    
    @After
    fun after() {
        reset(delegate)
    }
    
    @Test
    fun callsDelegated() {
        val request = mock<AdRequest>()
        uut.OnLoaded(request)
        uut.OnFailed(request, AdZoneError.E_30000)
        uut.OnExpanded(request)
        uut.OnClicked(request)
        
        verify(delegate).onLoaded(same(request))
        verify(delegate).onFailed(same(request), same(AdZoneError.E_30000))
        verify(delegate).onExpanded(same(request))
        verify(delegate).onClicked(same(request))
    }
    
    @Test
    fun onCollapsedDelegated() {
        val request = mock<AdRequest>()
        uut.OnCollapsed(request)
        
        verify(delegate).onCollapsed(same(request), eq(false))
    }
    
    @Test
    fun onCollapsedWithRewardDelegated() {
        val request = mock<AdRequest>()
        uut.shouldRewardUser()
        uut.OnCollapsed(request)
        
        verify(delegate).onCollapsed(same(request), eq(true))
    }
}
