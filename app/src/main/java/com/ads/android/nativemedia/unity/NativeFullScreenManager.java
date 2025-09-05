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

    // Timeout 5 giây
    private static final int LOADING_TIMEOUT_MS = 5000;

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
        timeoutHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void setAdPosition(AdPosition position) {

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

            // Tạo layout params trước khi load ad
            adLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );

            AdLoader adLoader = new AdLoader.Builder(activity, adUnitId)
                    .forNativeAd(unifiedNativeAd -> {
                        // Cancel timeout nếu ad load thành công
                        cancelTimeout();

                        if (isLoadingTimeout) {
                            // Nếu đã timeout thì không làm gì cả
                            if (unifiedNativeAd != null) unifiedNativeAd.destroy();
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

                        // Ẩn loading và hiện ad
                        hideLoadingScreen();
                        notifyAdLoaded("nativeFull");
                        showAdFull();
                        notifyShowSuccess("nativeFull");
                        notifyAdImpression("nativeFull");
                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError adError) {
                            // Cancel timeout và ẩn loading
                            cancelTimeout();
                            if (!isLoadingTimeout) {
                                hideLoadingScreen();
                                notifyFail("nativeFull", "Load failed native full: " + adError.getMessage());
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
                            notifyClosed("nativeFull", "Ad closed by system native full");
                            hideAd();
                        }
                    })
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        });
    }

    private void showLoadingScreen() {
        try {
            // Tạo loading view đơn giản
            loadingView = LayoutInflater.from(activity).inflate(R.layout.loading_screen, null);

            // Nếu không có layout loading_screen, tạo programmatically
            if (loadingView == null) {
                loadingView = createSimpleLoadingView();
            }

            FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );

            activity.addContentView(loadingView, loadingParams);
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu không tạo được loading view, tạo đơn giản
            loadingView = createSimpleLoadingView();
            if (loadingView != null) {
                try {
                    FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    );
                    activity.addContentView(loadingView, loadingParams);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private View createSimpleLoadingView() {
        try {
            FrameLayout container = new FrameLayout(activity);
            container.setBackgroundColor(0x80000000); // Semi-transparent black

            // Progress bar
            ProgressBar progressBar = new ProgressBar(activity);
            FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            progressParams.gravity = android.view.Gravity.CENTER;
            progressBar.setLayoutParams(progressParams);

            // Loading text
            TextView loadingText = new TextView(activity);
            loadingText.setText("Loading Ad...");
            loadingText.setTextColor(0xFFFFFFFF); // White text
            loadingText.setTextSize(16);
            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.gravity = android.view.Gravity.CENTER;
            textParams.topMargin = 200; // Margin dưới progress bar
            loadingText.setLayoutParams(textParams);

            container.addView(progressBar);
            container.addView(loadingText);

            return container;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        cancelTimeout(); // Cancel timeout cũ nếu có

        timeoutRunnable = () -> {
            isLoadingTimeout = true;
            hideLoadingScreen();
            notifyFail("nativeFull", "Ad loading timeout after " + (LOADING_TIMEOUT_MS / 1000) + " seconds");
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

        // Thêm kiểm tra null trước khi thêm view
        if (adView != null && adLayoutParams != null) {
            try {
                activity.addContentView(adView, adLayoutParams);
            } catch (Exception e) {
                e.printStackTrace();
                notifyFail("nativeFull", "Failed to add full screen ad view: " + e.getMessage());
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
            // Cancel timeout nếu đang chờ
            cancelTimeout();

            // Ẩn loading screen nếu đang hiển thị
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