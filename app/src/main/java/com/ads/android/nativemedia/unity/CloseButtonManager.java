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

    public static void setupCloseButton(NativeAdView adViewLayout, NativeAd nativeAd, CloseButtonCallback callback) {
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

        int mode = random.nextInt(3) + 1;

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
        ImageView closeBtn = adViewLayout.findViewById(R.id.close);
        TextView skipText = adViewLayout.findViewById(R.id.skip_text);
        ProgressBar countdown = adViewLayout.findViewById(R.id.skip_progress);
        TextView skipCount = adViewLayout.findViewById(R.id.skip_count);

        if (countdown != null) countdown.setVisibility(View.GONE);
        if (skipCount != null) skipCount.setVisibility(View.GONE);
        if (skipText != null) skipText.setVisibility(View.GONE);

        if (closeBtn == null) return;

        int mode = random.nextInt(3) + 1;

        switch (mode) {
            case 1: // Nút X hiện luôn -> chỉ tắt ads
                closeBtn.setVisibility(View.VISIBLE);
                closeBtn.setOnClickListener(v -> {
                    callback.onAdClosed("Closed immediately by user");
                    callback.onHideAdRequested(); // Thêm dòng này
                });
                break;

            case 2: // Nút X hiện luôn -> bấm ra chợ
                closeBtn.setVisibility(View.VISIBLE);
                View mediaView2 = adViewLayout.findViewById(R.id.ad_media);
                closeBtn.setOnClickListener(v -> {
                    if (mediaView2 != null) mediaView2.performClick();
                    callback.onAdClicked("Clicked via close button (go to store)");

                    // Ẩn ad ngay sau khi click
                    new CountDownTimer(500, 500) { // Thời gian chờ ngắn
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {
                            callback.onAdClosed("Closed after store redirect");
                            callback.onHideAdRequested(); // Thêm dòng này
                        }
                    }.start();
                });
                break;

            case 3: // Ẩn nút X 5s -> đếm ngược Skip ads -> xuất hiện nút X giả
                closeBtn.setVisibility(View.GONE);
                if (skipText != null) skipText.setVisibility(View.GONE);

// Sau 5s mới hiện skipText và bắt đầu đếm
                new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // 5s chờ đầu tiên -> không làm gì
                    }

                    @Override
                    public void onFinish() {
                        // Hiện skipText và bắt đầu đếm ngược 5s
                        if (skipText != null) {
                            skipText.setVisibility(View.VISIBLE);
                            skipText.setText("Skip in 5s");
                        }

                        // Bắt đầu đếm ngược 5s tiếp theo
                        new CountDownTimer(5000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                int secondsRemaining = (int) (millisUntilFinished / 1000);
                                if (skipText != null) {
                                    skipText.setText("Skip in " + secondsRemaining + "s");
                                }
                            }

                            @Override
                            public void onFinish() {
                                // Ẩn skipText
                                if (skipText != null) {
                                    skipText.setVisibility(View.GONE);
                                }

                                // Hiện nút X giả
                                closeBtn.setVisibility(View.VISIBLE);
                                View mediaView3 = adViewLayout.findViewById(R.id.ad_media);
                                closeBtn.setOnClickListener(v -> {
                                    if (mediaView3 != null) mediaView3.performClick();
                                    callback.onAdClicked("Fake close clicked -> store");
                                });

                                // Sau 2s đổi thành X thật
                                closeBtn.postDelayed(() -> {
                                    closeBtn.setOnClickListener(v -> {
                                        callback.onAdClosed("Closed after fake-close phase");
                                        callback.onHideAdRequested();
                                    });
                                }, 2000);
                            }
                        }.start();
                    }
                }.start();

        }
    }
}