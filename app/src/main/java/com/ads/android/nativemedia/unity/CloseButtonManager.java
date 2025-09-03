package com.ads.android.nativemedia.unity;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Random;

public class CloseButtonManager {
    // Biến quản lý random click
    private static int closeClickCount = 0;
    private static final int CLICK_CYCLE = 5;
    private static int adClickPosition = -1;
    private static Random random = new Random();

    public interface CloseButtonCallback {
        void onAdClosed(String message);
        void onAdClicked(String message);
        void onHideAdRequested(); // Thêm method mới để yêu cầu ẩn ad
    }

    // Helper method để convert int mode thành boolean array
    private static boolean[] parseCloseButtonMode(int mode) {
        String modeStr = String.valueOf(mode);
        boolean enableCase1 = modeStr.contains("1");
        boolean enableCase2 = modeStr.contains("2");
        boolean enableCase3 = modeStr.contains("3");
        return new boolean[]{enableCase1, enableCase2, enableCase3};
    }


    public static void setupCloseButton(NativeAdView adViewLayout, NativeAd nativeAd, CloseButtonCallback callback) {
        setupCloseButton(adViewLayout, nativeAd, callback, 1);
    }

    public static void setupCloseButton(NativeAdView adViewLayout, NativeAd nativeAd, CloseButtonCallback callback, int mode) {
        ImageView closeBtn = adViewLayout.findViewById(R.id.close);
        TextView skipText = adViewLayout.findViewById(R.id.skip_text);
        ProgressBar countdown = adViewLayout.findViewById(R.id.skip_progress);
        TextView skipCount = adViewLayout.findViewById(R.id.skip_count);

        if (countdown != null) countdown.setVisibility(View.GONE);
        if (skipCount != null) skipCount.setVisibility(View.GONE);
        if (skipText != null) skipText.setVisibility(View.GONE);

        if (closeBtn == null) return;

        if (adClickPosition == -1) {
            adClickPosition = random.nextInt(CLICK_CYCLE);
        }

        switch (mode) {
            case 1: // Close ngay lập tức
                closeBtn.setVisibility(View.VISIBLE);
                closeBtn.setOnClickListener(v -> {
                    callback.onAdClosed("Closed by user immediately");
                    callback.onHideAdRequested(); // Thêm dòng này
                });
                break;

            case 2: // Khi bấm close, mở link quảng cáo rồi ẩn ad
                closeBtn.setVisibility(View.VISIBLE);
                View mediaView2 = adViewLayout.findViewById(R.id.ad_media);
                closeBtn.setOnClickListener(v -> {
                    if (mediaView2 != null) mediaView2.performClick();
                    callback.onAdClicked("Clicked via close button performClick");

                    // Ẩn ad sau khi click (thay vì dùng timer)
                    new CountDownTimer(1000, 1000) { // Giảm thời gian chờ xuống 1 giây
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {
                            callback.onAdClosed("Closed after click");
                            callback.onHideAdRequested(); // Thêm dòng này
                        }
                    }.start();
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

                // Click listener cho skip/counter
                View mediaView3 = adViewLayout.findViewById(R.id.ad_media);

                if (countdown != null) {
                    countdown.setOnClickListener(v -> {
                        if (mediaView3 != null) mediaView3.performClick();
                        callback.onAdClicked("Clicked during countdown");
                    });
                }

                if (skipCount != null) {
                    skipCount.setOnClickListener(v -> {
                        if (mediaView3 != null) mediaView3.performClick();
                        callback.onAdClicked("Clicked during countdown");
                    });
                }

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
                        // Ẩn skip UI
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

                        // Hiện nút close
                        closeBtn.setVisibility(View.VISIBLE);
                        closeBtn.setOnClickListener(v -> {
                            callback.onAdClosed("Closed after countdown");
                            callback.onHideAdRequested(); // Thêm dòng này
                        });
                    }
                }.start();
                break;
        }
    }

    public static void setupCloseButtonNativeFull(NativeAdView adViewLayout, NativeAd nativeAd, CloseButtonCallback callback) {
        setupCloseButtonNativeFull(adViewLayout, nativeAd, callback, 1);
    }



    public static void setupCloseButtonNativeFull(NativeAdView adViewLayout, NativeAd nativeAd, CloseButtonCallback callback, int mode) {
        ImageView closeBtn = adViewLayout.findViewById(R.id.close);
        TextView skipText = adViewLayout.findViewById(R.id.skip_text);
        ProgressBar countdown = adViewLayout.findViewById(R.id.skip_progress);
        TextView skipCount = adViewLayout.findViewById(R.id.skip_count);

        if (countdown != null) countdown.setVisibility(View.GONE);
        if (skipCount != null) skipCount.setVisibility(View.GONE);
        if (skipText != null) skipText.setVisibility(View.GONE);

        if (closeBtn == null) return;



        switch (mode) {
            case 1: // Close ngay lập tức
                closeBtn.setVisibility(View.VISIBLE);
                closeBtn.setOnClickListener(v -> {
                    callback.onAdClosed("Closed by user immediately (NativeFull)");
                    callback.onHideAdRequested();
                });
                break;

            case 2: // Close thực hiện click vào quảng cáo, rồi ẩn ad
                closeBtn.setVisibility(View.VISIBLE);
                View mediaView2 = adViewLayout.findViewById(R.id.ad_media);
                closeBtn.setOnClickListener(v -> {
                    if (mediaView2 != null) mediaView2.performClick();
                    callback.onAdClicked("Clicked via close button performClick (NativeFull)");

                    new CountDownTimer(1000, 1000) {
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {
                            callback.onAdClosed("Closed after click (NativeFull)");
                            callback.onHideAdRequested();
                        }
                    }.start();
                });
                break;

            case 3: // Đếm ngược 5s xuất hiện X; click countdown cũng mở quảng cáo
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

                View mediaView3 = adViewLayout.findViewById(R.id.ad_media);

                if (countdown != null) {
                    countdown.setOnClickListener(v -> {
                        if (mediaView3 != null) mediaView3.performClick();
                        callback.onAdClicked("Clicked during countdown (NativeFull)");
                    });
                }
                if (skipCount != null) {
                    skipCount.setOnClickListener(v -> {
                        if (mediaView3 != null) mediaView3.performClick();
                        callback.onAdClicked("Clicked during countdown (NativeFull)");
                    });
                }

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
                            callback.onAdClosed("Closed after countdown (NativeFull)");
                            callback.onHideAdRequested();
                        });
                    }
                }.start();
                break;
        }
    }
}