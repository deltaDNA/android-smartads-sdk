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
    
    private val adapter = mock<MediationAdapter>()
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
    fun loadedAndFailedToLoad() {
        uut.requestPerformed(listener)
        uut.onAdPlayableChanged(true)
        uut.onAdPlayableChanged(false)
        
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
        uut.requestPerformed(listener)
        uut.onAdStart()
        uut.onAdPlayableChanged(false)
        
        verify(listener).onAdShowing(same(adapter))
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun failedToShow() {
        uut.requestPerformed(listener)
        uut.onAdPlayableChanged(true)
        uut.onAdUnavailable("reason")
        
        verify(listener).onAdLoaded(same(adapter))
        verify(listener).onAdFailedToShow(
                same(adapter),
                eq(AdClosedResult.EXPIRED))
    }
    
    @Test
    fun fullCycleWithSuccess() {
        uut.requestPerformed(listener)
        uut.onAdPlayableChanged(true)
        uut.onAdStart()
        uut.onAdEnd(true, false)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun fullCycleWithSuccessAndClick() {
        uut.requestPerformed(listener)
        uut.onAdPlayableChanged(true)
        uut.onAdStart()
        uut.onAdEnd(true, true)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClicked(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
    
    @Test
    fun fullCycleWithoutSuccess() {
        uut.requestPerformed(listener)
        uut.onAdPlayableChanged(true)
        uut.onAdStart()
        uut.onAdEnd(false, false)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun listenerNotifiedAfterCycle() {
        uut.requestPerformed(listener)
        uut.onAdPlayableChanged(true)
        uut.onAdEnd(true, false)
        val nextListener = mock<MediationListener>()
        uut.requestPerformed(nextListener)
        
        inOrder(listener, nextListener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
            verify(nextListener).onAdLoaded(same(adapter))
        }
    }
}
