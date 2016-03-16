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

package com.deltadna.android.sdk.ads.bindings;

/**
 * N.B. Enum names are not consistent with Java guidelines due to needing them
 * to come through the API as per documentation.
 */
public enum AdRequestResult {
    
    Loaded( 0b000000, 0) {
        @Override
        protected boolean demote(int code) {
            return false;
        }
    },
    NoFill( 0b000001, -1),
    Network(0b000010, -1),
    Timeout(0b000100, -1),
    MaxRequests(
            0b001000, -1),
    Configuration(
            0b000000, 0) {
        @Override
        protected boolean demote(int code) {
            return false;
        }
        
        @Override
        public boolean remove() {
            return true;
        }
    },
    Error(  0b000000, 0) {
        @Override
        protected boolean demote(int code) {
            return false;
        }
        
        @Override
        public boolean remove() {
            return true;
        }
    };
    
    private final int mask;
    private final int penalty;
    
    AdRequestResult(int mask, int penalty) {
        this.mask = mask;
        this.penalty = penalty;
    }
    
    public boolean remove() {
        return false;
    }
    
    public int computeScore(int code) {
        return demote(code) ? penalty : 0;
    }
    
    protected boolean demote(int code) {
        return (code & mask) == mask;
    }
}
