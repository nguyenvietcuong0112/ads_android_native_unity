package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.util.Log;

public class UnityNativeAd {

    private static UnityNativeAd instance;
    private final Activity activity;
    private NativeAdListener listener;

    private NativeAdManager nativeAdManager;
    private NativeAdLandManager nativeAdLandManager;
    private NativeAdDefaultManager nativeAdDefaultManager;

    private NativeFullScreenManager nativeFullScreenManager;
    private NativeLandFullScreenManager nativeLandFullScreenManager;

    private AdType currentAdType = AdType.NONE;

    private enum AdType {
        NONE, REGULAR, DEFAULT, FULL_SCREEN, LANDSCAPE, FULL_LAND_SCREEN
    }

    public UnityNativeAd(Activity activity) {
        this.activity = activity;
        instance = this;

        nativeAdManager = new NativeAdManager(activity);
        nativeAdDefaultManager = new NativeAdDefaultManager(activity);
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
        nativeAdDefaultManager.setListener(listener);
        nativeFullScreenManager.setListener(listener);
        nativeLandFullScreenManager.setListener(listener);
        Log.d("setListener","Successsssssssssssssssssssss");
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

    public void loadAdDefault(final String adUnitId, final int position, final int x, final int y) {
        currentAdType = AdType.DEFAULT;
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdDefaultManager.loadAd(adUnitId, adPosition);
    }

    public void loadAdDefault(final String adUnitId) {
        loadAdDefault(adUnitId, 0, 0, 0);
    }


    // Thêm các phương thức mới cho landscape
    public void loadAdLand(final String adUnitId, final int position, final int x, final int y) {
        loadAdLand(adUnitId, position, x, y, 1);
    }

    public void loadAdLand(final String adUnitId, final int position, final int x, final int y, final int mode) {
        currentAdType = AdType.LANDSCAPE;
        AdPosition adPosition = new AdPosition(position, x, y);
        nativeAdLandManager.loadAd(adUnitId, adPosition, mode);
    }

    public void loadAdLand(final String adUnitId) {
        loadAdLand(adUnitId, 0, 0, 0);
    }

    public void loadAdLand(final String adUnitId, final int mode) {
        loadAdLand(adUnitId, 0, 0, 0, mode);
    }

    public void loadAdNativeFull(final String adUnitId) {
        loadAdNativeFull(adUnitId, 1);
    }

    public void loadAdNativeFull(final String adUnitId, final int mode) {
        currentAdType = AdType.FULL_SCREEN;
        nativeFullScreenManager.loadAd(adUnitId, mode);
    }

    public void loadAdLandNativeFull(final String adUnitId) {
        loadAdLandNativeFull(adUnitId, 1);
    }

    public void loadAdLandNativeFull(final String adUnitId, final int mode) {
        currentAdType = AdType.FULL_LAND_SCREEN;
        nativeLandFullScreenManager.loadAd(adUnitId, mode);
    }


    public void setAdPosition(final int position, final int x, final int y) {
        if (currentAdType == AdType.REGULAR) {
            AdPosition adPosition = new AdPosition(position, x, y);
            nativeAdManager.setAdPosition(adPosition);
        }
    }

    public void setAdPositionDefault(final int position, final int x, final int y) {
        if (currentAdType == AdType.DEFAULT) {
            AdPosition adPosition = new AdPosition(position, x, y);
            nativeAdDefaultManager.setAdPosition(adPosition);
        }
    }

    // Thêm phương thức set position cho landscape
    public void setAdPositionLand(final int position, final int x, final int y) {
        if (currentAdType == AdType.LANDSCAPE) {
            AdPosition adPosition = new AdPosition(position, x, y);
            nativeAdLandManager.setAdPosition(adPosition);
        }
    }

    public static void hideAdFromUnity() {
        if (instance != null) {
            instance.hideAd();
        }
    }

    // Thêm phương thức hide cho landscape từ Unity
    public static void hideAdLandFromUnity() {
        if (instance != null) {
            instance.hideAdLand();
        }
    }

    public void hideAd() {
        switch (currentAdType) {
            case REGULAR:
                nativeAdManager.hideAd();
                break;
            case DEFAULT:
                nativeAdDefaultManager.hideAd();
                break;
            case LANDSCAPE:
                nativeAdLandManager.hideAd();
                break;
            case FULL_SCREEN:
                nativeFullScreenManager.hideAd();
                break;
            case FULL_LAND_SCREEN:
                nativeLandFullScreenManager.hideAd();
                break;
        }
        currentAdType = AdType.NONE;
    }

    // Thêm phương thức hide cho landscape
    public void hideAdLand() {
        if (currentAdType == AdType.LANDSCAPE) {
            nativeAdLandManager.hideAd();
        }
        currentAdType = AdType.NONE;
    }
    public void hideAdDefault() {
        if (currentAdType == AdType.DEFAULT) {
            nativeAdDefaultManager.hideAd();
        }
        currentAdType = AdType.NONE;
    }


    public void onDestroy() {
        nativeAdManager.onDestroy();
        nativeAdDefaultManager.onDestroy();
        nativeAdLandManager.onDestroy(); // Hủy cho landscape
        nativeFullScreenManager.onDestroy();
        nativeLandFullScreenManager.onDestroy();
        instance = null;
    }
}