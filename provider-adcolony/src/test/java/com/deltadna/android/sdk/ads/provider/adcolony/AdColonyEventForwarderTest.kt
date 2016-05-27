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

package com.deltadna.android.sdk.ads.provider.adcolony

import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.jirbo.adcolony.AdColonyAd
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*

@RunWith(MockitoJUnitRunner::class)
class AdColonyEventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    private val ad = mock<AdColonyAd>()
    
    private var uut = AdColonyEventForwarder(listener, adapter)
    
    @Before
    fun before() {
        uut = AdColonyEventForwarder(listener, adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter, ad)
    }
    
    @Test
    fun adStarted() {
        whenever(ad.noFill()).thenReturn(false)
        
        with(uut) {
            onAdColonyAdStarted(ad)
            
            assertThat(isShowing).isTrue()
            verify(listener).onAdShowing(same(adapter))
        }
    }
    
    @Test
    fun adStartedNoFill() {
        whenever(ad.noFill()).thenReturn(true)
        
        with(uut) {
            onAdColonyAdStarted(ad)
            
            assertThat(isShowing).isFalse()
            verify(listener).onAdFailedToLoad(
                    same(adapter),
                    eq(AdRequestResult.NoFill),
                    any())
        }
    }
    
    @Test
    fun adFinished() {
        whenever(ad.shown()).thenReturn(true)
        whenever(ad.canceled()).thenReturn(false)
        whenever(ad.skipped()).thenReturn(false)
        
        with(uut) {
            onAdColonyAdAttemptFinished(ad)
            
            assertThat(isShowing).isFalse()
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
    
    @Test
    fun adFinishedAndCancelled() {
        whenever(ad.shown()).thenReturn(true)
        whenever(ad.canceled()).thenReturn(true)
        
        with(uut) {
            onAdColonyAdAttemptFinished(ad)
            
            assertThat(isShowing).isFalse()
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
    }
    
    @Test
    fun adFinishedAndSkipped() {
        whenever(ad.shown()).thenReturn(true)
        whenever(ad.skipped()).thenReturn(true)
        
        with(uut) {
            onAdColonyAdAttemptFinished(ad)
            
            assertThat(isShowing).isFalse()
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
    }
    
    @Test
    fun adFinishedWithoutShowing() {
        whenever(ad.shown()).thenReturn(false)
        
        with(uut) {
            onAdColonyAdAttemptFinished(ad)
            
            assertThat(isShowing).isFalse()
            verify(listener).onAdFailedToShow(
                    same(adapter),
                    eq(AdClosedResult.ERROR))
        }
    }
}
