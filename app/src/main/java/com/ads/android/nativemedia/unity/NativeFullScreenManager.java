package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAdView;

public class NativeFullScreenManager extends BaseAdManager {

    private int mode;
    private View loadingView;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private boolean isAdLoaded = false;
    private boolean isLoadingTimeout = false;

    // Timeout (tăng lên 10s cho full screen ads)
    private static final int LOADING_TIMEOUT_MS = 15000;

    public NativeFullScreenManager(Activity activity) {
        super(activity);
        timeoutHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Với full screen ad thì không set position,
     * nhưng vẫn phải override để tránh lỗi abstract.
     */
    @Override
    public void setAdPosition(AdPosition position) {
        activity.runOnUiThread(() -> {
            if (adView != null) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );
                adView.setLayoutParams(params);
            }
        });
    }

    public void loadAd(String adUnitId) {
        loadAd(adUnitId, 1);
    }

    public void loadAd(String adUnitId, int mode) {
        this.mode = mode;
        isAdLoaded = false;
        isLoadingTimeout = false;

        activity.runOnUiThread(() -> {
            // Hiển thị loading screen trước
            showLoadingScreen();

            // Setup timeout handler
            setupTimeoutHandler();

            // Full screen => MATCH_PARENT
            adLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );

            AdLoader adLoader = new AdLoader.Builder(activity, adUnitId)
                    .forNativeAd(unifiedNativeAd -> {
                        cancelTimeout();

                        if (isLoadingTimeout) {
                            if (unifiedNativeAd != null) {
                                nativeAd = unifiedNativeAd;
                                showAdFull();
                            }
                            return;
                        }

                        if (nativeAd != null) nativeAd.destroy();
                        nativeAd = unifiedNativeAd;
                        isAdLoaded = true;

                        nativeAd.setOnPaidEventListener(adValue -> {
                            long micros = adValue.getValueMicros();
                            String currency = adValue.getCurrencyCode();
                            Log.i("AdRevenue", String.valueOf(micros));
                            notifyAdRevenuePaid("nativeFull", micros, currency);
                        });

                        // Ẩn loading và show ad
                        hideLoadingScreen();
                        notifyAdLoaded("nativeFull");

                        showAdFull();

//                        notifyAdImpression("nativeFull");
                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError adError) {
                            cancelTimeout();
                            if (!isLoadingTimeout) {
                                hideLoadingScreen();
                                notifyFail("nativeFull", adError.getMessage());
                            }
                        }

                        @Override
                        public void onAdClicked() {
                            notifyClicked("nativeFull", "Ad clicked native full");
                        }

                        @Override
                        public void onAdOpened() {
                            notifyAdOpened("nativeFull");
                        }

                        @Override
                        public void onAdClosed() {
                            notifyShowSuccess("nativeFull");
                            hideAd();
                        }

                        @Override
                        public void onAdImpression() {
                            // SDK đã ghi impression thực sự
                            notifyAdImpression("nativeFull");
                        }
                    })
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        });
    }

    /** Loading screen overlay */
    private void showLoadingScreen() {
        try {
            loadingView = LayoutInflater.from(activity).inflate(R.layout.loading_screen, null);
            if (loadingView == null) {
                loadingView = createSimpleLoadingView();
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            activity.addContentView(loadingView, params);
        } catch (Exception e) {
            e.printStackTrace();
            loadingView = createSimpleLoadingView();
            if (loadingView != null) {
                try {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    );
                    activity.addContentView(loadingView, params);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /** Fallback loading view */
    private View createSimpleLoadingView() {
        FrameLayout container = new FrameLayout(activity);
        container.setBackgroundColor(0x80000000);

        ProgressBar progressBar = new ProgressBar(activity);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        progressParams.gravity = android.view.Gravity.CENTER;
        progressBar.setLayoutParams(progressParams);

        TextView loadingText = new TextView(activity);
        loadingText.setText("Loading Ad...");
        loadingText.setTextColor(0xFFFFFFFF);
        loadingText.setTextSize(16);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = android.view.Gravity.CENTER;
        textParams.topMargin = 200;
        loadingText.setLayoutParams(textParams);

        container.addView(progressBar);
        container.addView(loadingText);

        return container;
    }

    private void hideLoadingScreen() {
        if (loadingView != null && loadingView.getParent() != null) {
            try {
                ((android.view.ViewGroup) loadingView.getParent()).removeView(loadingView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadingView = null;
    }

    private void setupTimeoutHandler() {
        cancelTimeout();
        timeoutRunnable = () -> {
            isLoadingTimeout = true;
            hideLoadingScreen();
            notifyFail("nativeFull",
                    "Ad loading timeout after " + (LOADING_TIMEOUT_MS / 1000) + " seconds");
        };
        timeoutHandler.postDelayed(timeoutRunnable, LOADING_TIMEOUT_MS);
    }

    private void cancelTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    protected void notifyAdRevenuePaid(String adType, long revenue, String currencyCode) {
        if (listener != null) {
            listener.onAdRevenuePaid(adType, revenue, currencyCode);
        }
    }

    /** Inflate full screen layout and show ad */
    private void showAdFull() {
        if (adLayoutParams == null) {
            adLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
        }

        // Random chọn 1 trong 2 layout
        int layoutResource = new java.util.Random().nextBoolean()
                ? R.layout.native_full_screen2
                : R.layout.native_full_screen;

        adView = LayoutInflater.from(activity).inflate(layoutResource, null);
        NativeAdView adViewLayout = (NativeAdView) adView;

        AdViewHelper.setupFullScreenAdView(adViewLayout, nativeAd);

        CloseButtonManager.setupCloseButtonNativeFull(adViewLayout, nativeAd,
                new CloseButtonManager.CloseButtonCallback() {
                    @Override
                    public void onAdClosed(String message) {
                        notifyClosed("nativeFull", message);
                    }

                    @Override
                    public void onAdClicked(String message) {
                        notifyClicked("nativeFull", message);
                    }

                    @Override
                    public void onHideAdRequested() {
                        hideAd();
                    }
                }, this.mode);

        if (adView != null && adLayoutParams != null) {
            try {
                activity.addContentView(adView, adLayoutParams);
            } catch (Exception e) {
                e.printStackTrace();
                notifyFail("nativeFull",
                        "Failed to add full screen ad view: " + e.getMessage());
            }
        }

        adViewLayout.setOnClickListener(v -> {
            notifyClicked("nativeFull", "Ad view clicked");
            notifyAdOpened("nativeFull");
        });
    }

    @Override
    public void hideAd() {
        activity.runOnUiThread(() -> {
            cancelTimeout();
            hideLoadingScreen();

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

    @Override
    public void onDestroy() {
        cancelTimeout();
        hideLoadingScreen();
        super.onDestroy();
    }
}
