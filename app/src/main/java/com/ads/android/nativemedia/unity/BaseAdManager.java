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
    protected void notifyAdLoaded() {
        if (listener != null) listener.onAdLoaded("Native ad loaded successfully");
    }

    protected void notifyShowSuccess() {
        if (listener != null) listener.onAdShowSuccess("NativeAd shown");
    }

    protected void notifyAdImpression() {
        if (listener != null) listener.onAdImpression("Ad impression recorded");
    }

    protected void notifyAdOpened() {
        if (listener != null) listener.onAdOpened("Ad opened");
    }

    protected void notifyClosed(String msg) {
        if (listener != null) listener.onAdClosed(msg);
    }

    protected void notifyClicked(String msg) {
        if (listener != null) listener.onAdClicked(msg);
    }

    protected void notifyFail(String msg) {
        if (listener != null) listener.onAdShowFail(msg);
    }

    // Thêm phương thức để kiểm tra xem ad có đang hiển thị không
    public boolean isAdShowing() {
        return adView != null && adView.getParent() != null;
    }
}