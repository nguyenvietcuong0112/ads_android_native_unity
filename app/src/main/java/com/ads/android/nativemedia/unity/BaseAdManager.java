package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.ads.nativead.NativeAd;

public abstract class BaseAdManager {
    protected Activity activity;
    protected NativeAd nativeAd;
    protected View adView;
    protected NativeAdListener listener;
    protected FrameLayout.LayoutParams adLayoutParams;

    public BaseAdManager(Activity activity) {
        this.activity = activity;
    }

    public void setListener(NativeAdListener listener) {
        this.listener = listener;
    }

    public abstract void setAdPosition(AdPosition position);
    public abstract void hideAd();

    public void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }
    }

    // Protected helper methods
    protected void notifyAdLoaded(String adType) {
        if (listener != null) listener.onAdLoaded(adType,"Native ad loaded successfully");
    }

    protected void notifyShowSuccess(String adType) {
        if (listener != null) listener.onAdShowSuccess(adType,"NativeAd shown");
    }

    protected void notifyAdImpression(String adType) {
        if (listener != null) listener.onAdImpression(adType,"Ad impression recorded");
    }

    protected void notifyAdOpened(String adType) {
        if (listener != null) listener.onAdOpened(adType,"Ad opened");
    }

    protected void notifyClosed(String adType,String msg) {
        if (listener != null) listener.onAdClosed(adType,msg);
    }

    protected void notifyClicked(String adType,String msg) {
        if (listener != null) listener.onAdClicked(adType,msg);
    }

    protected void notifyFail(String adType,String msg) {
        if (listener != null) listener.onAdShowFail(adType,msg);
    }

    // Thêm phương thức để kiểm tra xem ad có đang hiển thị không
    public boolean isAdShowing() {
        return adView != null && adView.getParent() != null;
    }
}