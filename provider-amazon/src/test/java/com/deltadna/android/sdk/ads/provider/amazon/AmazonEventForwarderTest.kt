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

package com.deltadna.android.sdk.ads.provider.amazon

import com.amazon.device.ads.AdError
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AmazonEventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    
    private var uut = AmazonEventForwarder(listener, adapter)
    
    @Before
    fun before() {
        uut = AmazonEventForwarder(listener, adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter)
    }
    
    @Test
    fun onAdLoaded() {
        uut.onAdLoaded(mock(), mock())
        
        verify(listener).onAdLoaded(same(adapter))
        assertThat(uut.isExpired).isFalse()
    }
    
    @Test
    fun onAdFailedToLoad() {
        val error = mock<AdError>()
        whenever(error.code).thenReturn(AdError.ErrorCode.NO_FILL)
        whenever(error.message).thenReturn("message")
        uut.onAdFailedToLoad(mock(), error)
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                argThat {
                    contains(AdError.ErrorCode.NO_FILL.toString())
                    && contains("message") })
        assertThat(uut.isExpired).isFalse()
    }
    
    @Test
    fun onAdExpanded() {
        uut.onAdExpanded(mock())
        
        verify(listener).onAdClicked(same(adapter))
        assertThat(uut.isExpired).isFalse()
    }

    @Test
    fun onAdDismissed() {
        uut.onAdDismissed(mock())
        
        verify(listener).onAdClosed(same(adapter), eq(true))
        assertThat(uut.isExpired).isFalse()
    }
    
    @Test
    fun onAdExpired() {
        uut.onAdExpired(mock())
        
        verifyZeroInteractions(listener)
        assertThat(uut.isExpired).isTrue()
    }
}
