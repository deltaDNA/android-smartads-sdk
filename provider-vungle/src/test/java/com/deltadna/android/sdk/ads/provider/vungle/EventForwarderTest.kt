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

package com.deltadna.android.sdk.ads.provider.vungle

import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventForwarderTest {
    
    private val placement = "placement"
    private val adapter = mock<MediationAdapter>()
    private val listener = mock<MediationListener>()
    
    private var uut = EventForwarder(placement, adapter, listener)
    
    @Before
    fun before() {
        uut = EventForwarder(placement, adapter, listener)
    }
    
    @After
    fun after() {
        reset(adapter, listener)
    }
    
    @Test
    fun firstAvailabilityUpdate() {
        uut.onAdAvailabilityUpdate(placement, true)
        
        verify(listener).onAdLoaded(same(adapter))
    }
    
    @Test
    fun firstAvailabilityUpdateFailure() {
        uut.onAdAvailabilityUpdate(placement, false)
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                any())
    }
    
    @Test
    fun successiveAvailabilityUpdate() {
        uut.onAdAvailabilityUpdate(placement, true)
        uut.onAdAvailabilityUpdate(placement, false)
        uut.requestPerformed(listener)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.NoFill),
                    any())
        }
    }
    
    @Test
    fun unableToPlayAd() {
        uut.onUnableToPlayAd(placement, "reason")
        
        verify(listener).onAdFailedToShow(
                same(adapter),
                eq(AdClosedResult.EXPIRED))
    }
    
    @Test
    fun adStart() {
        uut.onAdStart(placement)
        
        verify(listener).onAdShowing(same(adapter))
    }
    
    @Test
    fun adEndSuccessfulView() {
        uut.onAdEnd(placement, true, false)
        
        verify(listener).onAdClosed(same(adapter), eq(true))
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun adEndUnsuccessfulView() {
        uut.onAdEnd(placement, false, false)
        
        verify(listener).onAdClosed(same(adapter), eq(false))
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun adEndWithClicked() {
        uut.onAdEnd(placement, true, true)
        
        inOrder(listener) {
            verify(listener).onAdClicked(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun ignoresDifferentPlacement() {
        with("different") {
            uut.onAdAvailabilityUpdate(this, true)
            uut.onUnableToPlayAd(this, "reason")
            uut.onAdStart(this)
            uut.onAdEnd(this, true, true)
        }
        
        verifyZeroInteractions(listener)
    }
}
