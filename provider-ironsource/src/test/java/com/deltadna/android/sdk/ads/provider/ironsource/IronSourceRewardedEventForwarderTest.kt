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

package com.deltadna.android.sdk.ads.provider.ironsource

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.AdShowResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class IronSourceRewardedEventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    
    private var uut = IronSourceRewardedEventForwarder(adapter)
    
    @Before
    fun before() {
        uut = IronSourceRewardedEventForwarder(adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter)
    }
    
    @Test
    fun requestPerformedWithoutAvailability() {
        uut.requestPerformed(listener)
        
        verifyZeroInteractions(listener)
    }
    
    @Test
    fun requestPerformedWithAvailability() {
        uut.onRewardedVideoAvailabilityChanged(true)
        uut.requestPerformed(listener)
        
        verify(listener).onAdLoaded(same(adapter))
    }
    
    @Test
    fun requestPerformedWithDelayedAvailability() {
        uut.requestPerformed(listener)
        uut.onRewardedVideoAvailabilityChanged(true)
        
        verify(listener).onAdLoaded(same(adapter))
    }
    
    @Test
    fun requestPerformedWithNoAvailability() {
        uut.requestPerformed(listener)
        uut.onRewardedVideoAvailabilityChanged(false)
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                any())
    }
    
    @Test
    fun shownAndRewarded() {
        uut.requestPerformed(listener)
        uut.onRewardedVideoAvailabilityChanged(true)
        uut.onRewardedVideoAdOpened()
        uut.onRewardedVideoAdStarted()
        uut.onRewardedVideoAdEnded()
        uut.onRewardedVideoAdRewarded(mock())
        uut.onRewardedVideoAdClosed()
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
    
    @Test
    fun showWithoutReward() {
        uut.requestPerformed(listener)
        uut.onRewardedVideoAvailabilityChanged(true)
        uut.onRewardedVideoAdOpened()
        uut.onRewardedVideoAdStarted()
        uut.onRewardedVideoAdEnded()
        uut.onRewardedVideoAdClosed()
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
    }
    
    @Test
    fun showFailed() {
        uut.requestPerformed(listener)
        uut.onRewardedVideoAvailabilityChanged(true)
        uut.onRewardedVideoAdShowFailed(mock())
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdFailedToShow(
                    same(adapter),
                    eq(AdShowResult.ERROR))
        }
    }
}
