
package com.ads.android.nativemedia.unity;

public interface NativeAdListener {
    void onAdLoaded(String adType,String msg);          // Khi ad load thành công
    void onAdShowSuccess(String adType,String msg);     // Khi ad hiển thị thành công
    void onAdImpression(String adType,String msg);      // Khi ad được impression
    void onAdOpened(String adType,String msg);          // Khi ad được mở/click
    void onAdClosed(String adType,String msg);          // Khi ad bị đóng
    void onAdClicked(String adType,String msg);         // Khi ad được click
    void onAdShowFail(String adType,String msg);        // Khi ad load/show thất bại
    void onAdRevenuePaid(String adType, long revenue,String currencyCode);   // Khi có revenue trả về

}