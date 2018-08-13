/*
 * Copyright (c) 2018 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.mobfox

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MobFoxEventForwarderTest {
    
    private lateinit var listener: MediationListener
    private lateinit var adapter: MediationAdapter
    
    private lateinit var uut: MobFoxEventForwarder
    
    @Before
    fun before() {
        listener = mock()
        adapter = mock()
        
        uut = MobFoxEventForwarder(listener, adapter)
    }
    
    @Test
    fun `listener notified of loading failures`() {
        uut.onInterstitialFailed("no fill")
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq("no fill"))
        
        uut.onInterstitialFailed("no ads")
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq("no ads"))
        
        uut.onInterstitialFailed("invh not set")
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.Configuration),
                eq("invh not set"))
        
        uut.onInterstitialFailed("undefined error")
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.Error),
                eq("undefined error"))
    }
    
    @Test
    fun `listener notified of completed ad show`() {
        uut.run {
            onInterstitialLoaded(mock())
            onInterstitialShown()
            onInterstitialFinished()
            onInterstitialClosed()
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
    
    @Test
    fun `listener notified of incomplete ad show`() {
        uut.run {
            onInterstitialLoaded(mock())
            onInterstitialShown()
            onInterstitialClosed()
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
    }
    
    @Test
    fun `listener notified of ad click`() {
        uut.onInterstitialClicked()
        
        verify(listener).onAdClicked(same(adapter))
    }
}
