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
import com.vungle.publisher.VunglePub
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventForwarderTest {
    
    private val vunglePub = mock<VunglePub>()
    private val placement = "placement"
    private val adapter = mock<MediationAdapter>()
    private val listener = mock<MediationListener>()
    
    private var uut = EventForwarder(vunglePub, placement, adapter, listener)
    
    @Before
    fun before() {
        uut = EventForwarder(vunglePub, placement, adapter, listener)
    }
    
    @After
    fun after() {
        reset(vunglePub, adapter, listener)
    }
    
    @Test
    fun loadedAndFailedToLoad() {
        whenever(vunglePub.isAdPlayable(eq(placement))).then { true }
        
        uut.onAdAvailabilityUpdate(placement, true)
        uut.onAdAvailabilityUpdate(placement, false)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.NoFill),
                    any())
        }
    }
    
    @Test
    fun loadedAndFailedToLoadWhileAdShowing() {
        whenever(vunglePub.isAdPlayable(eq(placement))).then { true }
        
        uut.onAdStart(placement)
        uut.onAdAvailabilityUpdate(placement, false)
        
        verify(listener).onAdShowing(same(adapter))
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun failedToShow() {
        whenever(vunglePub.isAdPlayable(eq(placement))).then { true }
        
        uut.onAdAvailabilityUpdate(placement, true)
        uut.onUnableToPlayAd(placement, "reason")
        
        verify(listener).onAdLoaded(same(adapter))
        verify(listener).onAdFailedToShow(
                same(adapter),
                eq(AdClosedResult.EXPIRED))
    }
    
    @Test
    fun fullCycleWithSuccess() {
        whenever(vunglePub.isAdPlayable(eq(placement))).then { true }
        
        uut.onAdAvailabilityUpdate(placement, true)
        uut.onAdStart(placement)
        uut.onAdEnd(placement, true, false)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun fullCycleWithSuccessAndClick() {
        whenever(vunglePub.isAdPlayable(eq(placement))).then { true }
        
        uut.onAdAvailabilityUpdate(placement, true)
        uut.onAdStart(placement)
        uut.onAdEnd(placement, true, true)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClicked(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
    
    @Test
    fun fullCycleWithoutSuccess() {
        whenever(vunglePub.isAdPlayable(eq(placement))).then { true }
        
        uut.onAdAvailabilityUpdate(placement, true)
        uut.onAdStart(placement)
        uut.onAdEnd(placement, false, false)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun listenerNotifiedAfterCycle() {
        whenever(vunglePub.isAdPlayable(eq(placement))).thenReturn(true, true)
        
        uut.onAdAvailabilityUpdate(placement, true)
        uut.onAdEnd(placement, true, false)
        uut.onAdAvailabilityUpdate(placement, true)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
            verify(listener).onAdLoaded(same(adapter))
        }
    }
    
    @Test
    fun listenerNotifiedAfterCycleButNotYetReady() {
        whenever(vunglePub.isAdPlayable(eq(placement))).thenReturn(true, false)
        
        uut.onAdAvailabilityUpdate(placement, true)
        uut.onAdEnd(placement, true, false)
        uut.onAdAvailabilityUpdate(placement, true)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
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
