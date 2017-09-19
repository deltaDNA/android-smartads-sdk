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

package com.deltadna.android.sdk.ads.provider.facebook

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.facebook.ads.AdError
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RewardedEventForwarderTest {
    
    private val adapter = mock<MediationAdapter>()
    private val listener = mock<MediationListener>()
    
    private var uut = RewardedEventForwarder(adapter, listener)
    
    @Before
    fun before() {
        uut = RewardedEventForwarder(adapter, listener)
    }
    
    @After
    fun after() {
        reset(adapter, listener)
    }
    
    @Test
    fun onAdLoaded() {
        uut.onAdLoaded(mock())
        
        verify(listener).onAdLoaded(same(adapter))
    }
    
    @Test
    fun onError() {
        uut.onError(mock(), AdError.NO_FILL)
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq(AdError.NO_FILL.errorMessage))
    }
    
    @Test
    fun onLoggingImpression() {
        uut.onLoggingImpression(mock())
        
        verify(listener).onAdShowing(same(adapter))
    }
    
    @Test
    fun onAdClicked() {
        uut.onAdClicked(mock())
        
        verify(listener).onAdClicked(same(adapter))
    }
    
    @Test
    fun onClosedWithoutCompletion() {
        uut.onRewardedVideoClosed()
        
        verify(listener).onAdClosed(same(adapter), eq(false))
    }
    
    @Test
    fun onClosedWithCompletion() {
        uut.onRewardedVideoCompleted()
        uut.onRewardedVideoClosed()
        
        verify(listener).onAdClosed(same(adapter), eq(true))
    }
}
