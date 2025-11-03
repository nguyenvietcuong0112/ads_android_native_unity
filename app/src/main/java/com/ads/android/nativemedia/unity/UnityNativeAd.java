package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.util.Log;

public class UnityNativeAd {

    private static UnityNativeAd instance;
    private final Activity activity;
    private NativeAdListener listener;

    private NativeAdManager nativeAdManager;
    private NativeAdSplashManager nativeAdSplashManager;

    private NativeFullScreenManager nativeFullScreenManager;
    private NativeBannerManager nativeBannerManager;


    public UnityNativeAd(Activity activity) {
        this.activity = activity;
        instance = this;

        nativeAdManager = new NativeAdManager(activity);
        nativeAdSplashManager = new NativeAdSplashManager(activity);
        nativeFullScreenManager = new NativeFullScreenManager(activity);
        nativeBannerManager = new NativeBannerManager(activity);
    }

    public static UnityNativeAd getInstance(Activity activity) {
        if (instance == null) {
            instance = new UnityNativeAd(activity);
        }
        return instance;
    }

    public void setListener(NativeAdListener listener) {
        this.listener = listener;
        nativeAdManager.setListener(listener);
        nativeAdSplashManager.setListener(listener);
        nativeFullScreenManager.setListener(listener);
        nativeBannerManager.setListener(listener);
        Log.d("setListener", "Successsssssssssssssssssssss");
    }

    public void showNativeCollab(final String adUnitId, final int position, final int x, final int y, final int mode,boolean isPortrait) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdManager.loadAd(adUnitId, adPosition, mode,isPortrait);
    }
    public void showNativeSplash(final String adUnitId, final int position, final int x, final int y, final int mode,boolean isPortrait) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdSplashManager.loadAd(adUnitId, adPosition, mode,isPortrait);
    }

    public void showNativeFull(final String adUnitId, final int mode,boolean isPortrait) {
        nativeFullScreenManager.loadAd(adUnitId, mode,isPortrait);
    }
    public void showNativeBanner(final String adUnitId, final int position, final int x, final int y, final int mode,boolean isPortrait) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeBannerManager.loadAd(adUnitId, adPosition, mode,isPortrait);
    }
    public void setAdCollabPosition(final int position, final int x, final int y) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdManager.setAdPosition(adPosition);
    }
    public void setAdSplashPosition(final int position, final int x, final int y) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdSplashManager.setAdPosition(adPosition);
    }
    public void hideNativeBanner(){
        nativeBannerManager.hideAd();
    }

    public void hideAdCollab() {
        nativeAdManager.hideAd();
    }

    public void hideAdSplash() {
        nativeAdSplashManager.hideAd();
    }


    public void hideAdNativeFull() {
        nativeFullScreenManager.hideAd();
    }

    public void onDestroy() {
        nativeAdManager.onDestroy();
        nativeAdSplashManager.onDestroy();
        nativeFullScreenManager.onDestroy();
        nativeBannerManager.onDestroy();
        instance = null;
    }
}