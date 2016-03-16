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

package com.deltadna.android.sdk.ads.core

import android.app.Activity
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import org.json.JSONObject

// *

fun inOrder(vararg mocks: Any, block: org.mockito.InOrder.() -> Unit) {
    block.invoke(org.mockito.Mockito.inOrder(*mocks))
}

fun stubbedAdapters(n: Int): List<MediationAdapter> {
    return (0..n-1).map { StubbedAdapter(it) }
}

// classes

open class StubbedAdapter(waterfallIndex: Int) :
        MediationAdapter(0, 0b1001, waterfallIndex) {
    
    override fun requestAd(
            activity: Activity?,
            listener: MediationListener?,
            configuration: JSONObject?) {
        throw UnsupportedOperationException()
    }
    
    override fun showAd() {
        throw UnsupportedOperationException()
    }
    
    override fun getProviderString(): String? {
        throw UnsupportedOperationException()
    }
    
    override fun getProviderVersionString(): String? {
        throw UnsupportedOperationException()
    }
    
    override fun onDestroy() {
        throw UnsupportedOperationException()
    }
    
    override fun onPause() {
        throw UnsupportedOperationException()
    }
    
    override fun onResume() {
        throw UnsupportedOperationException()
    }
}
