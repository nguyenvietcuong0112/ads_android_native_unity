package com.ads.android.nativemedia.unity;

import android.app.Activity;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Random;

public class UnityNativeAd {

    private static UnityNativeAd instance; // Singleton
    private final Activity activity;
    private NativeAd nativeAd;
    private View adView;
    private NativeAdListener listener; // callback listener
    private FrameLayout.LayoutParams adLayoutParams;

    // Biến quản lý random click
    private int closeClickCount = 0;
    private final int CLICK_CYCLE = 5;
    private int adClickPosition = -1;

    public UnityNativeAd(Activity activity) {
        this.activity = activity;
        instance = this;
    }

    public static UnityNativeAd getInstance(Activity activity) {
        if (instance == null) {
            instance = new UnityNativeAd(activity);
        }
        return instance;
    }

    public void setListener(NativeAdListener listener) {
        this.listener = listener;
    }

    private void notifyAdLoaded() {
        if (listener != null) listener.onAdLoaded("Native ad loaded successfully");
    }

    private void notifyShowSuccess() {
        if (listener != null) listener.onAdShowSuccess("NativeAd shown");
    }

    private void notifyAdImpression() {
        if (listener != null) listener.onAdImpression("Ad impression recorded");
    }

    private void notifyAdOpened() {
        if (listener != null) listener.onAdOpened("Ad opened");
    }

    private void notifyClosed(String msg) {
        if (listener != null) listener.onAdClosed(msg);
    }

    private void notifyClicked(String msg) {
        if (listener != null) listener.onAdClicked(msg);
    }

    private void notifyFail(String msg) {
        if (listener != null) listener.onAdShowFail(msg);
    }

    public static void hideAdFromUnity() {
        if (instance != null) {
            instance.hideAd();
        }
    }

    public void loadAd(final String adUnitId ,final int position, final int x, final int y) {
        activity.runOnUiThread(() -> {
            adLayoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );

            switch (position) {
                case 0: adLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL; break;
                case 1: adLayoutParams.gravity = Gravity.TOP | Gravity.LEFT; break;
                case 2: adLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT; break;
                case 3: adLayoutParams.gravity = Gravity.CENTER; break;
                case 4: adLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL; break;
                case 5: adLayoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT; break;
                case 6: adLayoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT; break;
                case 7:
                    adLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                    adLayoutParams.leftMargin = x;
                    adLayoutParams.topMargin = y;
                    break;
                default: adLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            }

            AdLoader adLoader = new AdLoader.Builder(activity, adUnitId)
                    .forNativeAd(unifiedNativeAd -> {
                        if (nativeAd != null) nativeAd.destroy();
                        nativeAd = unifiedNativeAd;

                        notifyAdLoaded();
                        showAd();
                        notifyShowSuccess();
                        notifyAdImpression();
                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError adError) {
                            notifyFail("Load failed: " + adError.getMessage());
                        }

                        @Override
                        public void onAdClicked() { notifyClicked("Ad clicked"); }

                        @Override
                        public void onAdOpened() { notifyAdOpened(); }

                        @Override
                        public void onAdClosed() { notifyClosed("Ad closed by system"); }
                    })
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        });
    }

    public void loadAd(final String adUnitId) {
        loadAd(adUnitId, 0, 0, 0);
    }

    private void showAd() {
        adView = LayoutInflater.from(activity).inflate(R.layout.layout_native_ads, null);
        NativeAdView adViewLayout = (NativeAdView) adView;

        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adViewLayout.setMediaView(mediaView);

        TextView headline = adView.findViewById(R.id.ad_headline);
        headline.setText(nativeAd.getHeadline());
        adViewLayout.setHeadlineView(headline);

        ImageView icon = adView.findViewById(R.id.ad_app_icon);
        if (nativeAd.getIcon() != null) {
            icon.setImageDrawable(nativeAd.getIcon().getDrawable());
        } else {
            icon.setVisibility(View.GONE);
        }
        adViewLayout.setIconView(icon);

        Button cta = adView.findViewById(R.id.ad_call_to_action);
        if (nativeAd.getCallToAction() != null) {
            cta.setText(nativeAd.getCallToAction());
            cta.setVisibility(View.VISIBLE);
        } else {
            cta.setVisibility(View.GONE);
        }
        adViewLayout.setCallToActionView(cta);

        adViewLayout.setNativeAd(nativeAd);

        setupCloseButton(adViewLayout);

        activity.addContentView(adView, adLayoutParams);

        adViewLayout.setOnClickListener(v -> {
            notifyClicked("Ad view clicked");
            notifyAdOpened();
        });
    }

    public void setAdPosition(final int position, final int x, final int y) {
        activity.runOnUiThread(() -> {
            if (adView != null && adView.getParent() != null) {
                FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

                switch (position) {
                    case 0: newParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL; break;
                    case 1: newParams.gravity = Gravity.TOP | Gravity.LEFT; break;
                    case 2: newParams.gravity = Gravity.TOP | Gravity.RIGHT; break;
                    case 3: newParams.gravity = Gravity.CENTER; break;
                    case 4: newParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL; break;
                    case 5: newParams.gravity = Gravity.BOTTOM | Gravity.LEFT; break;
                    case 6: newParams.gravity = Gravity.BOTTOM | Gravity.RIGHT; break;
                    case 7:
                        newParams.gravity = Gravity.TOP | Gravity.LEFT;
                        newParams.leftMargin = x;
                        newParams.topMargin = y;
                        break;
                    default: newParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                }

                adView.setLayoutParams(newParams);
            }
        });
    }

    private void setupCloseButton(NativeAdView adViewLayout) {
        ImageView closeBtn = adViewLayout.findViewById(R.id.close);
        TextView skipText = adViewLayout.findViewById(R.id.skip_text);
        ProgressBar countdown = adViewLayout.findViewById(R.id.skip_progress);
        TextView skipCount = adViewLayout.findViewById(R.id.skip_count);

        if (countdown != null) countdown.setVisibility(View.GONE);
        if (skipCount != null) skipCount.setVisibility(View.GONE);
        if (skipText != null) skipText.setVisibility(View.GONE);

        if (closeBtn == null) return;

        if (adClickPosition == -1) {
            adClickPosition = new Random().nextInt(CLICK_CYCLE);
        }

        int mode = new Random().nextInt(3) + 1;

        switch (mode) {
            case 1: // Close ngay lập tức
                closeBtn.setVisibility(View.VISIBLE);
                closeBtn.setOnClickListener(v -> {
                    hideAd();
                    notifyClosed("Closed by user immediately");
                });
                break;

            case 2: // Khi bấm close, mở link quảng cáo rồi ẩn ad
                closeBtn.setVisibility(View.VISIBLE);
                closeBtn.setOnClickListener(v -> {
                    if (nativeAd != null) {
                        MediaView mediaView = adViewLayout.findViewById(R.id.ad_media);
                        if (mediaView != null) {
                            activity.runOnUiThread(() -> {
                                mediaView.performClick();  // mở link quảng cáo
                                notifyClicked("Clicked via close button performClick");
                                // ẩn ad sau khi click (nhanh hoặc có thể đợi callback nếu muốn)
                                hideAd();
                                notifyClosed("Closed after click");
                            });
                            return; // thoát sớm tránh hideAd() gọi thêm lần nữa
                        }
                    }
                    // Nếu không có mediaView, vẫn ẩn ad
                    hideAd();
                    notifyClosed("Closed by user (no media available for click)");
                });
                break;

            case 3: // Đếm ngược 5s rồi mới hiện close; trong thời gian countdown click mở quảng cáo
                closeBtn.setVisibility(View.GONE);

                if (countdown != null) {
                    countdown.setVisibility(View.VISIBLE);
                    countdown.setMax(5);
                    countdown.setProgress(0);
                }
                if (skipCount != null) {
                    skipCount.setVisibility(View.VISIBLE);
                    skipCount.setText("5");
                }
                if (skipText != null) {
                    skipText.setVisibility(View.VISIBLE);
                }

                View.OnClickListener clickAdListener = v -> {
                    if (nativeAd != null) {
                        MediaView mediaView = adViewLayout.findViewById(R.id.ad_media);
                        if (mediaView != null) {
                            activity.runOnUiThread(() -> {
                                mediaView.performClick();
                                notifyClicked("Clicked via skip/counter");
                            });
                        }
                    }
                    hideAd();
                    notifyClosed("Closed by skip/counter click");
                };

                if (countdown != null) countdown.setOnClickListener(clickAdListener);
                if (skipCount != null) skipCount.setOnClickListener(clickAdListener);

                new CountDownTimer(5000, 1000) {
                    int counter = 5;

                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (skipCount != null) {
                            skipCount.setText(String.valueOf(counter));
                        }
                        if (countdown != null) {
                            countdown.setProgress(5 - counter + 1);
                        }
                        counter--;
                    }

                    @Override
                    public void onFinish() {
                        if (countdown != null) {
                            countdown.setVisibility(View.GONE);
                            countdown.setOnClickListener(null);
                        }
                        if (skipCount != null) {
                            skipCount.setVisibility(View.GONE);
                            skipCount.setOnClickListener(null);
                        }
                        if (skipText != null) {
                            skipText.setVisibility(View.GONE);
                        }

                        closeBtn.setVisibility(View.VISIBLE);
                        closeBtn.setOnClickListener(v -> {
                            hideAd();
                            notifyClosed("Closed after countdown");
                        });
                    }
                }.start();
                break;
        }
    }

    public void hideAd() {
        activity.runOnUiThread(() -> {
            if (adView != null && adView.getParent() != null) {
                ((android.view.ViewGroup) adView.getParent()).removeView(adView);
            }
            adView = null;

            if (nativeAd != null) {
                nativeAd.destroy();
                nativeAd = null;
            }
        });
    }

    public void onDestroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }
    }
}