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

package com.deltadna.android.sdk.ads.provider.admob

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.google.android.gms.ads.AdRequest
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
    fun notComplete() {
        uut.onRewardedVideoAdLoaded()
        uut.onRewardedVideoAdOpened()
        uut.onRewardedVideoStarted()
        uut.onRewardedVideoAdClosed()
        
        verify(listener).onAdLoaded(same(adapter))
        verify(listener).onAdShowing(same(adapter))
        verify(listener).onAdClosed(same(adapter), eq(false))
    }
    
    @Test
    fun complete() {
        uut.onRewardedVideoAdLoaded()
        uut.onRewardedVideoAdOpened()
        uut.onRewardedVideoStarted()
        uut.onRewarded(mock())
        uut.onRewardedVideoAdClosed()
        
        verify(listener).onAdLoaded(same(adapter))
        verify(listener).onAdShowing(same(adapter))
        verify(listener).onAdClosed(same(adapter), eq(true))
    }
    
    @Test
    fun failedToLoad() {
        uut.onRewardedVideoAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL)
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                any())
    }
    
    @Test
    fun leftApplication() {
        uut.onRewardedVideoAdLeftApplication()
        
        verify(listener).onAdLeftApplication(same(adapter))
    }
}
