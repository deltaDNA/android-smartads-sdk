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

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.AdShowResult
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.fractionalmedia.sdk.AdRequest
import com.fractionalmedia.sdk.AdZoneError
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EventForwarderTest {
    
    private val adapter = mock<MachineZoneAdapter>()
    private val listener = mock<MediationListener>()
    
    private var uut = EventForwarder(adapter, listener)
    
    @Before
    fun before() {
        uut = EventForwarder(adapter, listener)
    }
    
    @After
    fun after() {
        reset(adapter, listener)
    }
    
    @Test
    fun onLoaded() {
        val request = mock<AdRequest>()
        uut.onLoaded(request)
        
        inOrder(adapter, listener) {
            verify(adapter).onAdLoaded(same(request))
            verify(listener).onAdLoaded(same(adapter))
        }
    }
    
    @Test
    fun onFailed() {
        arrayOf(AdZoneError.E_30500,
                AdZoneError.E_30501,
                AdZoneError.E_30502,
                AdZoneError.E_30000,
                AdZoneError.E_30700).forEach {
            uut.onFailed(mock(), it)
        }
        
        verify(listener, times(2)).onAdFailedToShow(
                same(adapter),
                eq(AdShowResult.ERROR))
        verify(listener).onAdFailedToShow(
                same(adapter),
                eq(AdShowResult.EXPIRED))
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq(AdZoneError.E_30000.toString()))
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.Error),
                eq(AdZoneError.E_30700.toString()))
    }
    
    @Test
    fun onClicked() {
        uut.onClicked(mock())
        
        verify(listener).onAdClicked(same(adapter))
    }
    
    @Test
    fun onCollapsed() {
        uut.onCollapsed(mock(), true)
        uut.onCollapsed(mock(), false)
        
        verify(listener).onAdClosed(same(adapter), eq(true))
        verify(listener).onAdClosed(same(adapter), eq(false))
    }
}
