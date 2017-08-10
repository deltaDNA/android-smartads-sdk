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

package com.deltadna.android.sdk.ads.provider.loopme

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.google.common.truth.Truth.*
import com.loopme.LoopMeInterstitial
import com.loopme.common.LoopMeError
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    
    private var uut = EventForwarder(listener, adapter)
    
    @Before
    fun before() {
        uut = EventForwarder(listener, adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter)
    }
    
    @Test
    fun cycleCompleted() {
        with(mock<LoopMeInterstitial>()) {
            uut.onLoopMeInterstitialLoadSuccess(this)
            uut.onLoopMeInterstitialShow(this)
            uut.onLoopMeInterstitialVideoDidReachEnd(this)
            uut.onLoopMeInterstitialHide(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun cycleNotCompleted() {
        with(mock<LoopMeInterstitial>()) {
            uut.onLoopMeInterstitialLoadSuccess(this)
            uut.onLoopMeInterstitialShow(this)
            uut.onLoopMeInterstitialHide(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun failedToLoad() {
        val error = LoopMeError("message")
        uut.onLoopMeInterstitialLoadFail(mock(), error)
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq(error.message))
    }
    
    @Test
    fun clicked() {
        uut.onLoopMeInterstitialClicked(mock())
        
        verify(listener).onAdClicked(same(adapter))
    }
    
    @Test
    fun leaveApp() {
        uut.onLoopMeInterstitialLeaveApp(mock())
        
        verify(listener).onAdLeftApplication(same(adapter))
    }
    
    @Test
    fun expired() {
        assertThat(uut.hasExpired()).isFalse()
        
        uut.onLoopMeInterstitialExpired(mock())
        
        assertThat(uut.hasExpired()).isTrue()
        verifyZeroInteractions(listener)
    }
}
