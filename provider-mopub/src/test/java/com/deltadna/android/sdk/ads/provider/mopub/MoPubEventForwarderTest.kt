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

package com.deltadna.android.sdk.ads.provider.mopub

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.mopub.mobileads.MoPubErrorCode
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MoPubEventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    
    private var uut = MoPubEventForwarder(listener, adapter)
    
    @Before
    fun before() {
        uut = MoPubEventForwarder(listener, adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter)
    }
    
    @Test
    fun onLoaded() {
        with(uut) {
            onInterstitialLoaded(mock())
            
            verify(listener).onAdLoaded(same(adapter))
        }
    }
    
    @Test
    fun onFailed() {
        with(uut) {
            onInterstitialFailed(mock(), MoPubErrorCode.NO_FILL)
            onInterstitialFailed(mock(), MoPubErrorCode.NETWORK_INVALID_STATE)
            
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.NoFill),
                    any())
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.Error),
                    any())
        }
    }
    
    @Test
    fun onShown() {
        with(uut) {
            onInterstitialShown(mock())
            
            verify(listener).onAdShowing(same(adapter))
        }
    }
    
    @Test
    fun onClicked() {
        with(uut) {
            onInterstitialClicked(mock())
            
            verify(listener).onAdClicked(same(adapter))
        }
    }
    
    @Test
    fun onDismissed() {
        with(uut) {
            onInterstitialDismissed(mock())
            
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
}
