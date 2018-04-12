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
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.ironsource.mediationsdk.logger.IronSourceError
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class IronSourceInterstitialEventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    
    private var uut = IronSourceInterstitialEventForwarder(adapter, listener)
    
    @Before
    fun before() {
        uut = IronSourceInterstitialEventForwarder(adapter, listener)
    }
    
    @After
    fun after() {
        reset(listener, adapter)
    }
    
    @Test
    fun onInterstitialAdReady() {
        uut.onInterstitialAdReady()
        
        verify(listener).onAdLoaded(same(adapter))
    }
    
    @Test
    fun onAdLoadFailed() {
        uut.onInterstitialAdLoadFailed(IronSourceError(
                IronSourceError.ERROR_CODE_NO_ADS_TO_SHOW,
                "message"))
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq("message"))
    }
    
    @Test
    fun onInterstitialAdOpened() {
        uut.onInterstitialAdOpened()
        
        verifyZeroInteractions(listener)
    }
    
    @Test
    fun onInterstitialAdShowFailed() {
        uut.onInterstitialAdShowFailed(IronSourceError(
                IronSourceError.ERROR_CODE_GENERIC,
                "message"))
        
        verify(listener).onAdFailedToShow(
                same(adapter),
                eq(AdClosedResult.ERROR))
    }
    
    @Test
    fun onInterstitialAdShowSucceeded() {
        uut.onInterstitialAdShowSucceeded()
        
        verify(listener).onAdShowing(same(adapter))
    }
    
    @Test
    fun onInterstitialAdClicked() {
        uut.onInterstitialAdClicked()
        
        verify(listener).onAdClicked(same(adapter))
    }
    
    @Test
    fun onInterstitialAdClosed() {
        uut.onInterstitialAdClosed()
        
        verify(listener).onAdClosed(same(adapter), eq(true))
    }
}
