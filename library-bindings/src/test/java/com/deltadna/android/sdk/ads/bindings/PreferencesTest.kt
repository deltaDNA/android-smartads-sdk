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

package com.deltadna.android.sdk.ads.bindings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.*
import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PreferencesTest {
    
    @Test
    fun construction() {
        with(mock<Context>()) {
            Preferences(this)
            
            verify(this).getSharedPreferences(
                    eq("com.deltadna.android.sdk.ads"),
                    same(Context.MODE_PRIVATE))
        }
    }
    
    @Test
    fun get() {
        val prefs = mock<SharedPreferences>()
        val uut = Preferences(mock<Context>().apply {
            whenever(this.getSharedPreferences(any(), any())).then { prefs }
        })
        
        assertThat(uut.get()).isSameAs(prefs)
    }
    
    @Test
    @SuppressLint("CommitPrefEdits")
    fun edit() {
        val editor = mock<SharedPreferences.Editor>()
        val uut = Preferences(mock<Context>().apply {
            whenever(this.getSharedPreferences(any(), any())).then {
                mock<SharedPreferences>().apply {
                    whenever(this.edit()).then { editor }
                }
            }
        })
        
        assertThat(uut.edit()).isSameAs(editor)
    }
}
