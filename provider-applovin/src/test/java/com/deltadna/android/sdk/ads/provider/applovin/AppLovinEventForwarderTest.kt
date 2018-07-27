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

package com.deltadna.android.sdk.ads.provider.applovin

import com.applovin.sdk.AppLovinAd
import com.applovin.sdk.AppLovinErrorCodes
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AppLovinEventForwarderTest {
    
    private lateinit var listener: MediationListener
    private lateinit var adapter: MediationAdapter
    
    private lateinit var uut: AppLovinEventForwarder
    
    @Before
    fun before() {
        listener = mock()
        adapter = mock()
        
        uut = AppLovinEventForwarder(listener, adapter)
    }
    
    @Test
    fun `ad received notifies listener`() {
        uut.adReceived(mock())
        verify(listener).onAdLoaded(same(adapter))
    }
    
    @Test
    fun `failed to receive ad notifies listener`() {
        uut.failedToReceiveAd(AppLovinErrorCodes.NO_FILL)
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                any())
    }
    
    @Test
    fun `completed ad play cycle notifies listener`() {
        with(mock<AppLovinAd>()) {
            uut.adDisplayed(this)
            uut.videoPlaybackBegan(this)
            uut.videoPlaybackEnded(this, 0.0, true)
            uut.adHidden(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
    
    @Test
    fun `incomplete ad play cycle notifies listener`() {
        with(mock<AppLovinAd>()) {
            uut.adDisplayed(this)
            uut.videoPlaybackBegan(this)
            uut.adHidden(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
    }
    
    @Test
    fun `ad clicked notifies listener`() {
        uut.adClicked(mock())
        verify(listener).onAdClicked(same(adapter))
    }
}
