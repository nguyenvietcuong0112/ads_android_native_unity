package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.util.Log;

public class UnityNativeAd {

    private static UnityNativeAd instance;
    private final Activity activity;
    private NativeAdListener listener;

    private NativeAdManager nativeAdManager;
    private NativeAdLandManager nativeAdLandManager;
    private NativeAdSplashManager nativeAdSplashManager;

    private NativeFullScreenManager nativeFullScreenManager;
    private NativeLandFullScreenManager nativeLandFullScreenManager;


    public UnityNativeAd(Activity activity) {
        this.activity = activity;
        instance = this;

        nativeAdManager = new NativeAdManager(activity);
        nativeAdSplashManager = new NativeAdSplashManager(activity);
        nativeAdLandManager = new NativeAdLandManager(activity); // Khởi tạo manager cho landscape
        nativeFullScreenManager = new NativeFullScreenManager(activity);
        nativeLandFullScreenManager = new NativeLandFullScreenManager(activity);
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
        nativeAdLandManager.setListener(listener);
        nativeAdSplashManager.setListener(listener);
        nativeFullScreenManager.setListener(listener);
        nativeLandFullScreenManager.setListener(listener);
        Log.d("setListener", "Successsssssssssssssssssssss");
    }

    public void showNativeCollab(final String adUnitId, final int position, final int x, final int y) {
        showNativeCollab(adUnitId, position, x, y, 1);
    }

    public void showNativeCollab(final String adUnitId, final int position, final int x, final int y, final int mode) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdManager.loadAd(adUnitId, adPosition, mode);
    }

    public void showNativeCollab(final String adUnitId) {
        showNativeCollab(adUnitId, 0, 0, 0);
    }

    public void showNativeCollab(final String adUnitId, final int mode) {
        showNativeCollab(adUnitId, 0, 0, 0, mode);
    }

    public void showNativeSplash(final String adUnitId, final int position, final int x, final int y) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdSplashManager.loadAd(adUnitId, adPosition);
    }

    public void showNativeSplash(final String adUnitId) {
        showNativeSplash(adUnitId, 0, 0, 0);
    }


    // Thêm các phương thức mới cho landscape
    public void showNativeCollabLand(final String adUnitId, final int position, final int x, final int y) {
        showNativeCollabLand(adUnitId, position, x, y, 1);
    }

    public void showNativeCollabLand(final String adUnitId, final int position, final int x, final int y, final int mode) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdLandManager.loadAd(adUnitId, adPosition, mode);
    }

    public void showNativeCollabLand(final String adUnitId) {
        showNativeCollabLand(adUnitId, 0, 0, 0);
    }

    public void showNativeCollabLand(final String adUnitId, final int mode) {
        showNativeCollabLand(adUnitId, 0, 0, 0, mode);
    }

    public void showNativeFull(final String adUnitId) {
        showNativeFull(adUnitId, 1);
    }

    public void showNativeFull(final String adUnitId, final int mode) {
        nativeFullScreenManager.loadAd(adUnitId, mode);
    }

    public void showNativeFullLand(final String adUnitId) {
        showNativeFullLand(adUnitId, 1);
    }

    public void showNativeFullLand(final String adUnitId, final int mode) {
        nativeLandFullScreenManager.loadAd(adUnitId, mode);
    }


    public void setAdCollabPosition(final int position, final int x, final int y) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdManager.setAdPosition(adPosition);
    }

    // Thêm phương thức set position cho landscape
    public void setAdCollabLandPosition(final int position, final int x, final int y) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdLandManager.setAdPosition(adPosition);
    }

    public void setAdSplashPosition(final int position, final int x, final int y) {
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdSplashManager.setAdPosition(adPosition);
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

    public void hideAdNativeFullLand() {
        nativeLandFullScreenManager.hideAd();
    }


    public void hideAdCollabLand() {
        nativeAdLandManager.hideAd();
    }


    public void onDestroy() {
        nativeAdManager.onDestroy();
        nativeAdSplashManager.onDestroy();
        nativeAdLandManager.onDestroy(); // Hủy cho landscape
        nativeFullScreenManager.onDestroy();
        nativeLandFullScreenManager.onDestroy();
        instance = null;
    }
}