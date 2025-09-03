package com.ads.android.nativemedia.unity;

import android.app.Activity;

public class UnityNativeAd {

    private static UnityNativeAd instance;
    private final Activity activity;
    private NativeAdListener listener;

    private NativeAdManager nativeAdManager;
    private NativeFullScreenManager nativeFullScreenManager;

    private AdType currentAdType = AdType.NONE;

    private enum AdType {
        NONE, REGULAR, FULL_SCREEN
    }

    public UnityNativeAd(Activity activity) {
        this.activity = activity;
        instance = this;

        nativeAdManager = new NativeAdManager(activity);
        nativeFullScreenManager = new NativeFullScreenManager(activity);
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
        nativeFullScreenManager.setListener(listener);
    }

    public void loadAd(final String adUnitId, final int position, final int x, final int y) {
        loadAd(adUnitId, position, x, y, 1);
    }


    public void loadAd(final String adUnitId, final int position, final int x, final int y, final int mode) {
        currentAdType = AdType.REGULAR;
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdManager.loadAd(adUnitId, adPosition, mode);
    }

    public void loadAd(final String adUnitId) {
        loadAd(adUnitId, 0, 0, 0);
    }

    public void loadAd(final String adUnitId, final int mode) {
        loadAd(adUnitId, 0, 0, 0, mode);
    }

    public void loadAdNativeFull(final String adUnitId) {
        loadAdNativeFull(adUnitId, 1);
    }


    public void loadAdNativeFull(final String adUnitId, final int mode) {
        currentAdType = AdType.FULL_SCREEN;
        nativeFullScreenManager.loadAd(adUnitId, mode);
    }

    public void setAdPosition(final int position, final int x, final int y) {
        if (currentAdType == AdType.REGULAR) {
            AdPosition adPosition = new AdPosition(position, x, y);
            nativeAdManager.setAdPosition(adPosition);
        }
    }

    public static void hideAdFromUnity() {
        if (instance != null) {
            instance.hideAd();
        }
    }

    public void hideAd() {
        switch (currentAdType) {
            case REGULAR:
                nativeAdManager.hideAd();
                break;
            case FULL_SCREEN:
                nativeFullScreenManager.hideAd();
                break;
        }
        currentAdType = AdType.NONE;
    }

    public void onDestroy() {
        nativeAdManager.onDestroy();
        nativeFullScreenManager.onDestroy();
        instance = null;
    }
}