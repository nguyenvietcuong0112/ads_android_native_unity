package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAdView;

public class NativeFullScreenManager extends BaseAdManager {

    private boolean enableCase1 = true;
    private boolean enableCase2 = true;
    private boolean enableCase3 = true;

    // Helper method để convert int mode thành boolean array
    private boolean[] parseCloseButtonMode(int mode) {
        String modeStr = String.valueOf(mode);
        boolean enableCase1 = modeStr.contains("1");
        boolean enableCase2 = modeStr.contains("2");
        boolean enableCase3 = modeStr.contains("3");
        return new boolean[]{enableCase1, enableCase2, enableCase3};
    }

    public NativeFullScreenManager(Activity activity) {
        super(activity);
    }

    @Override
    public void setAdPosition(AdPosition position) {

    }

    public void loadAd(String adUnitId) {
        loadAd(adUnitId, true, true, true);
    }

    public void loadAd(String adUnitId, int mode) {
        boolean[] modes = parseCloseButtonMode(mode);
        loadAd(adUnitId, modes[0], modes[1], modes[2]);
    }

    public void loadAd(String adUnitId, boolean enableCase1, boolean enableCase2, boolean enableCase3) {
        this.enableCase1 = enableCase1;
        this.enableCase2 = enableCase2;
        this.enableCase3 = enableCase3;

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

        // Random 50/50 giữa 2 layout
        java.util.Random random = new java.util.Random();
        boolean useLayout2 = random.nextBoolean(); // 50% true, 50% false

        int layoutResource = useLayout2 ? R.layout.native_full_screen2 : R.layout.native_full_screen;
        adView = LayoutInflater.from(activity).inflate(layoutResource, null);
        NativeAdView adViewLayout = (NativeAdView) adView;

        // Setup ad views for full screen layout
        AdViewHelper.setupFullScreenAdView(adViewLayout, nativeAd);

        // Setup close button với callback mới, truyền bool
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
        }, enableCase1, enableCase2, enableCase3);

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