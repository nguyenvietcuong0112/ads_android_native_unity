package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAdView;

public class NativeAdLandManager extends BaseAdManager {

    private int mode;

    public NativeAdLandManager(Activity activity) {
        super(activity);
    }

    public void loadAd(String adUnitId, AdPosition position) {
        loadAd(adUnitId, position, 1);
    }

    public void loadAd(String adUnitId, AdPosition position, final int mode) {
        this.mode = mode;

        activity.runOnUiThread(() -> {
            // Tạo layout params trước khi load ad
            setupLayoutParams(position);

            AdLoader adLoader = new AdLoader.Builder(activity, adUnitId)
                    .forNativeAd(unifiedNativeAd -> {
                        if (nativeAd != null) nativeAd.destroy();
                        nativeAd = unifiedNativeAd;

                        nativeAd.setOnPaidEventListener(adValue -> {
                            long micros = adValue.getValueMicros();
                            String currency = adValue.getCurrencyCode();
                            Log.i("AdRevenueLand", String.valueOf(micros));
                            notifyAdRevenuePaid("nativeCollab", micros, currency);
                        });

                        notifyAdLoaded("nativeCollab");
                        showAd();

                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError adError) {
                            notifyFail("nativeCollab", adError.getMessage());
                        }

                        @Override
                        public void onAdClicked() {
                            notifyClicked("nativeCollab", "Ad clicked");
                        }

                        @Override
                        public void onAdOpened() {
                            notifyAdOpened("nativeCollab");
                        }

                        @Override
                        public void onAdClosed() {
                            notifyShowSuccess("nativeCollab");
                            hideAd();
                        }
                        @Override
                        public void onAdImpression() {
                            notifyAdImpression("nativeCollab");
                        }
                    })
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        });
    }

    protected void notifyAdRevenuePaid(String adType, long revenue, String currencyCode) {
        if (listener != null) {
            listener.onAdRevenuePaid(adType, revenue, currencyCode);
        }
    }

    private void setupLayoutParams(AdPosition position) {
        adLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        switch (position.getPosition()) {
            case AdPosition.TOP_CENTER:
                adLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case AdPosition.TOP_LEFT:
                adLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                break;
            case AdPosition.TOP_RIGHT:
                adLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case AdPosition.CENTER:
                adLayoutParams.gravity = Gravity.CENTER;
                break;
            case AdPosition.BOTTOM_CENTER:
                adLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                break;
            case AdPosition.BOTTOM_LEFT:
                adLayoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case AdPosition.BOTTOM_RIGHT:
                adLayoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
            case AdPosition.CUSTOM:
                adLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                adLayoutParams.leftMargin = position.getX();
                adLayoutParams.topMargin = position.getY();
                break;
            default:
                adLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        }
    }

    private void showAd() {
        // Kiểm tra null để tránh crash
        if (adLayoutParams == null) {
            setupLayoutParams(new AdPosition(AdPosition.TOP_CENTER, 0, 0));
        }

        adView = LayoutInflater.from(activity).inflate(R.layout.layout_native_ads_land, null);
        NativeAdView adViewLayout = (NativeAdView) adView;

        // Setup ad views (headline, icon, CTA, etc.)
        AdViewHelper.setupNativeAdView(adViewLayout, nativeAd);

        // Setup close button với callback mới
        CloseButtonManager.setupCloseButton(adViewLayout, nativeAd, new CloseButtonManager.CloseButtonCallback() {
            @Override
            public void onAdClosed(String message) {
                notifyClosed("nativeCollab", message);
            }

            @Override
            public void onAdClicked(String message) {
                notifyClicked("nativeCollab", message);
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
                notifyFail("nativeCollab", "Failed to add ad view: " + e.getMessage());
            }
        }

        adViewLayout.setOnClickListener(v -> {
            notifyClicked("nativeCollab", "Ad view clicked");
            notifyAdOpened("nativeCollab");
        });
    }

    @Override
    public void setAdPosition(AdPosition position) {
        activity.runOnUiThread(() -> {
            if (adView != null && adView.getParent() != null) {
                FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

                switch (position.getPosition()) {
                    case AdPosition.TOP_CENTER:
                        newParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                        break;
                    case AdPosition.TOP_LEFT:
                        newParams.gravity = Gravity.TOP | Gravity.LEFT;
                        break;
                    case AdPosition.TOP_RIGHT:
                        newParams.gravity = Gravity.TOP | Gravity.RIGHT;
                        break;
                    case AdPosition.CENTER:
                        newParams.gravity = Gravity.CENTER;
                        break;
                    case AdPosition.BOTTOM_CENTER:
                        newParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                        break;
                    case AdPosition.BOTTOM_LEFT:
                        newParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                        break;
                    case AdPosition.BOTTOM_RIGHT:
                        newParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                        break;
                    case AdPosition.CUSTOM:
                        newParams.gravity = Gravity.TOP | Gravity.LEFT;
                        newParams.leftMargin = position.getX();
                        newParams.topMargin = position.getY();
                        break;
                    default:
                        newParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                }

                adView.setLayoutParams(newParams);
            }
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

    @Override
    public void onDestroy() {
        hideAd();
    }
}