package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAdView;

public class NativeFullScreenManager extends BaseAdManager {

    public NativeFullScreenManager(Activity activity) {
        super(activity);
    }

    @Override
    public void setAdPosition(AdPosition position) {

    }

    public void loadAd(String adUnitId) {
        activity.runOnUiThread(() -> {
            // Tạo layout params trước khi load ad
            adLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );

            AdLoader adLoader = new AdLoader.Builder(activity, adUnitId)
                    .forNativeAd(unifiedNativeAd -> {
                        if (nativeAd != null) nativeAd.destroy();
                        nativeAd = unifiedNativeAd;

                        notifyAdLoaded();
                        showAdFull();
                        notifyShowSuccess();
                        notifyAdImpression();
                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError adError) {
                            notifyFail("Load failed: " + adError.getMessage());
                        }

                        @Override
                        public void onAdClicked() {
                            notifyClicked("Ad clicked");
                        }

                        @Override
                        public void onAdOpened() {
                            notifyAdOpened();
                        }

                        @Override
                        public void onAdClosed() {
                            notifyClosed("Ad closed by system");
                            hideAd();
                        }
                    })
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        });
    }

    private void showAdFull() {
        // Kiểm tra null để tránh crash
        if (adLayoutParams == null) {
            adLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
        }

        adView = LayoutInflater.from(activity).inflate(R.layout.native_full_screen, null);
        NativeAdView adViewLayout = (NativeAdView) adView;

        // Setup ad views for full screen layout
        AdViewHelper.setupFullScreenAdView(adViewLayout, nativeAd);

        // Setup close button với callback mới
        CloseButtonManager.setupCloseButtonNativeFull(adViewLayout, nativeAd, new CloseButtonManager.CloseButtonCallback() {
            @Override
            public void onAdClosed(String message) {
                notifyClosed(message);
            }

            @Override
            public void onAdClicked(String message) {
                notifyClicked(message);
            }

            @Override
            public void onHideAdRequested() {
                hideAd();
            }
        });

        // Thêm kiểm tra null trước khi thêm view
        if (adView != null && adLayoutParams != null) {
            try {
                activity.addContentView(adView, adLayoutParams);
            } catch (Exception e) {
                e.printStackTrace();
                notifyFail("Failed to add full screen ad view: " + e.getMessage());
            }
        }

        adViewLayout.setOnClickListener(v -> {
            notifyClicked("Ad view clicked");
            notifyAdOpened();
        });
    }

    @Override
    public void hideAd() {
        activity.runOnUiThread(() -> {
            if (adView != null && adView.getParent() != null) {
                try {
                    ((android.view.ViewGroup) adView.getParent()).removeView(adView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            adView = null;

            if (nativeAd != null) {
                nativeAd.destroy();
                nativeAd = null;
            }
        });
    }
}