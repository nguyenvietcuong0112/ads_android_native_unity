
package com.ads.android.nativemedia.unity;

public interface NativeAdListener {
    void onAdLoaded(String msg);          // Khi ad load thành công
    void onAdShowSuccess(String msg);     // Khi ad hiển thị thành công
    void onAdImpression(String msg);      // Khi ad được impression
    void onAdOpened(String msg);          // Khi ad được mở/click
    void onAdClosed(String msg);          // Khi ad bị đóng
    void onAdClicked(String msg);         // Khi ad được click
    void onAdShowFail(String msg);        // Khi ad load/show thất bại
}