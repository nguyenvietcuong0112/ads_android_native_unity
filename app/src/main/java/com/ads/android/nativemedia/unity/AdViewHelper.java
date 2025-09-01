package com.ads.android.nativemedia.unity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class AdViewHelper {

    public static void setupNativeAdView(NativeAdView adViewLayout, NativeAd nativeAd) {
        MediaView mediaView = adViewLayout.findViewById(R.id.ad_media);
        adViewLayout.setMediaView(mediaView);

        TextView headline = adViewLayout.findViewById(R.id.ad_headline);
        headline.setText(nativeAd.getHeadline());
        adViewLayout.setHeadlineView(headline);

        ImageView icon = adViewLayout.findViewById(R.id.ad_app_icon);
        if (nativeAd.getIcon() != null) {
            icon.setImageDrawable(nativeAd.getIcon().getDrawable());
        } else {
            icon.setVisibility(View.GONE);
        }
        adViewLayout.setIconView(icon);

        Button cta = adViewLayout.findViewById(R.id.ad_call_to_action);
        if (nativeAd.getCallToAction() != null) {
            cta.setText(nativeAd.getCallToAction());
            cta.setVisibility(View.VISIBLE);
        } else {
            cta.setVisibility(View.GONE);
        }
        adViewLayout.setCallToActionView(cta);

        adViewLayout.setNativeAd(nativeAd);
    }

    public static void setupFullScreenAdView(NativeAdView adViewLayout, NativeAd nativeAd) {
        MediaView mediaView = adViewLayout.findViewById(R.id.ad_media);
        adViewLayout.setMediaView(mediaView);

        TextView headline = adViewLayout.findViewById(R.id.ad_headline);
        if (nativeAd.getHeadline() != null) {
            headline.setText(nativeAd.getHeadline());
            adViewLayout.setHeadlineView(headline);
        }

        TextView body = adViewLayout.findViewById(R.id.ad_body);
        if (nativeAd.getBody() != null) {
            body.setText(nativeAd.getBody());
            adViewLayout.setBodyView(body);
        }

        ImageView icon = adViewLayout.findViewById(R.id.ad_app_icon);
        if (icon != null) {
            if (nativeAd.getIcon() != null) {
                icon.setImageDrawable(nativeAd.getIcon().getDrawable());
            } else {
                icon.setVisibility(View.GONE);
            }
            adViewLayout.setIconView(icon);
        }

        Button cta = adViewLayout.findViewById(R.id.ad_call_to_action);
        if (nativeAd.getCallToAction() != null) {
            cta.setText(nativeAd.getCallToAction());
            cta.setVisibility(View.VISIBLE);
        } else {
            cta.setVisibility(View.GONE);
        }
        adViewLayout.setCallToActionView(cta);

        adViewLayout.setNativeAd(nativeAd);
    }
}