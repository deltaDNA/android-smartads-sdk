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

package com.deltadna.android.sdk.ads.provider.vungle

import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.google.common.truth.Truth.*
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EventForwarderTest {
    
    private lateinit var adapter: MediationAdapter
    private lateinit var listener: MediationListener
    
    private lateinit var uut: EventForwarder
    
    @Before
    fun before() {
        adapter = mock()
        listener = mock()
        
        uut = EventForwarder(PLACEMENT_ID, adapter, listener)
    }
    
    @Test
    fun `checks whether placement is the same`() {
        assertThat(uut.isSamePlacement(null)).isFalse()
        assertThat(uut.isSamePlacement("")).isFalse()
        
        assertThat(uut.isSamePlacement(PLACEMENT_ID)).isTrue()
        
        assertThat(uut.isSamePlacement("differentPlacementId"))
    }
    
    private companion object {
        
        const val PLACEMENT_ID = "placementId"
    }
}
