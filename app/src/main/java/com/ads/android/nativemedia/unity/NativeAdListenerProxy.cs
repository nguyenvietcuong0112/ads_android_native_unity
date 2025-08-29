using UnityEngine;

public class NativeAdListenerProxy : AndroidJavaProxy
{
    public NativeAdListenerProxy() : base("com.ads.android.nativemedia.unity.NativeAdListener") {}

    public void onAdLoaded(string msg)
    {
        Debug.Log("📥 Ad loaded: " + msg);
        UnityMainThreadDispatcher.Instance().Enqueue(() => {
            NativeAdBridge.OnAdLoaded?.Invoke(msg);
        });
    }

    public void onAdShowSuccess(string msg)
    {
        Debug.Log("✅ Ad show success: " + msg);
        UnityMainThreadDispatcher.Instance().Enqueue(() => {
            NativeAdBridge.OnAdShown?.Invoke(msg);
        });
    }

    public void onAdImpression(string msg)
    {
        Debug.Log("👁️ Ad impression: " + msg);
        UnityMainThreadDispatcher.Instance().Enqueue(() => {
            NativeAdBridge.OnAdImpression?.Invoke(msg);
        });
    }

    public void onAdOpened(string msg)
    {
        Debug.Log("🚀 Ad opened: " + msg);
        UnityMainThreadDispatcher.Instance().Enqueue(() => {
            NativeAdBridge.OnAdOpened?.Invoke(msg);
        });
    }

    public void onAdClosed(string msg)
    {
        Debug.Log("❌ Ad closed: " + msg);
        UnityMainThreadDispatcher.Instance().Enqueue(() => {
            NativeAdBridge.OnAdClosed?.Invoke(msg);
        });
    }

    public void onAdClicked(string msg)
    {
        Debug.Log("🖱️ Ad clicked: " + msg);
        UnityMainThreadDispatcher.Instance().Enqueue(() => {
            NativeAdBridge.OnAdClicked?.Invoke(msg);
        });
    }

    public void onAdShowFail(string msg)
    {
        Debug.LogError("⚠️ Ad failed: " + msg);
        UnityMainThreadDispatcher.Instance().Enqueue(() => {
            NativeAdBridge.OnAdFailed?.Invoke(msg);
        });
    }
}