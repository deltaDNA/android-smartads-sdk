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

package com.deltadna.android.sdk.ads.integrationtester;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter;
import com.deltadna.android.sdk.ads.provider.admob.AdMobAdapter;
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter;
import com.deltadna.android.sdk.ads.provider.applovin.AppLovinRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.facebook.FacebookInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.inmobi.InMobiRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.ironsource.IronSourceInterstitialAdapter;
import com.deltadna.android.sdk.ads.provider.ironsource.IronSourceRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.mobfox.MobFoxAdapter;
import com.deltadna.android.sdk.ads.provider.mopub.MoPubAdapter;
import com.deltadna.android.sdk.ads.provider.thirdpresence.ThirdPresenceRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.unity.UnityRewardedAdapter;
import com.deltadna.android.sdk.ads.provider.vungle.VungleAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class IntegrationActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener {
    
    private static final List<? extends MediationAdapter> PROVIDERS =
            Arrays.asList(
                    new AdColonyAdapter(
                            0,
                            0,
                            0,
                            "appc804f742b8064114a9",
                            "vzbb9fa7accb4e4185b7"),
                    new AdMobAdapter(
                            0,
                            0,
                            0,
                            "ca-app-pub-3117129396855330/6027943007"),
                    new AmazonAdapter(
                            0,
                            0,
                            0,
                            "b156d556c85c4a918be92ee218708d4a",
                            true),
                    new AppLovinRewardedAdapter(
                            0,
                            0,
                            0,
                            "ElG63iTpOQfZvG4kizCGhhXZQiWt37hIszOvfyi3MNdFdh-KeAbKt7vHrQ9uXrBNpZHTV-WtL87-r6IUGvp80h",
                            "Interstitial",
                            true),
                    new FacebookInterstitialAdapter(
                            0,
                            0,
                            0,
                            "1296985967048898_1296988693715292"),
                    new InMobiInterstitialAdapter(
                            0,
                            0,
                            0,
                            "d9518d128a124772b07a750fa98d1bbe",
                            1447292217917L),
                    new InMobiRewardedAdapter(
                            0,
                            0,
                            0,
                            "d9518d128a124772b07a750fa98d1bbe",
                            1464837141675L),
                    new IronSourceInterstitialAdapter(
                            0,
                            0,
                            0,
                            "6114d36d",
                            true),
                    new IronSourceRewardedAdapter(
                            0,
                            0,
                            0,
                            "6114d36d",
                            true),
                    new MobFoxAdapter(
                            0,
                            0,
                            0,
                            "303fc0e182f1e126f276537f2b3d01ee"),
                    new MoPubAdapter(
                            0,
                            0,
                            0,
                            "3ac456d99b6a4cffb58cfba31fab2a21"),
                    new ThirdPresenceRewardedAdapter(
                            0,
                            0,
                            0,
                            "tpr-deltadna",
                            "lkqa0wifqs",
                            true),
                    new UnityRewardedAdapter(
                            0,
                            0,
                            0,
                            "109764",
                            null,
                            true),
                    new VungleAdapter(
                            0,
                            0,
                            0,
                            "5832df18d614b1ab17000251"));
    private static final List<String> PROVIDER_NAMES;
    static {
        final List<String> names = new ArrayList<>(PROVIDERS.size());
        for (final MediationAdapter provider : PROVIDERS) {
            names.add(provider.getClass().getSimpleName());
        }
        PROVIDER_NAMES = names;
    }
    
    private MediationAdapter provider = PROVIDERS.get(0);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_integration);
        final ListView viewProviders = (ListView) findViewById(R.id.integration_providers);
        viewProviders.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_single_choice,
                PROVIDER_NAMES));
        viewProviders.setOnItemClickListener(this);
        viewProviders.setItemChecked(0, true);
    }
    
    @Override
    public void onItemClick(
            AdapterView<?> parent, View view, int position, long id) {
        
        provider = PROVIDERS.get(position);
        
        debug("Using " + provider.getClass().getSimpleName());
    }
    
    public void onRequestAd(View view) {
        provider.requestAd(this, new Listener(), new JSONObject());
    }
    
    public void onShowAd(View view) {
        provider.showAd();
    }
    
    private static void debug(String msg) {
        Log.d(IntegrationActivity.class.getSimpleName(), msg);
    }
    
    private final class Listener implements MediationListener {
        
        @Override
        public void onAdLoaded(MediationAdapter mediationAdapter) {
            debug("Ad loaded");
        }
        
        @Override
        public void onAdFailedToLoad(
                MediationAdapter mediationAdapter,
                AdRequestResult adLoadResult,
                String reason) {
            
            debug("Ad failed to load; result: " + adLoadResult + ' ' + reason);
        }
        
        @Override
        public void onAdShowing(MediationAdapter mediationAdapter) {
            debug("Ad showing");
        }
        
        @Override
        public void onAdFailedToShow(
                MediationAdapter mediationAdapter,
                AdClosedResult adClosedResult) {
            
            debug("Ad failed to show; result: " + adClosedResult);
        }
        
        @Override
        public void onAdClicked(MediationAdapter mediationAdapter) {
            debug("Ad clicked");
        }
        
        @Override
        public void onAdLeftApplication(MediationAdapter mediationAdapter) {
            debug("Ad left application");
        }
        
        @Override
        public void onAdClosed(
                MediationAdapter mediationAdapter,
                boolean complete) {
            
            debug("Ad closed; complete: " + complete);
        }
    }
}
