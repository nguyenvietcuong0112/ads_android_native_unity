using UnityEngine;
using UnityEngine.UI;
using System;
using System.Collections.Generic;

public class NativeAdBridge : MonoBehaviour
{
    [Header("Native Ad Settings")]
    public string adUnitId = "ca-app-pub-3940256099942544/2247696110"; // Test ID

    [Header("UI References")]
    public Button showAdButton;
    public Button hideAdButton;
    public Button updatePositionButton;
    public Text statusText;
    public Dropdown positionDropdown;
    public InputField xPositionInput;
    public InputField yPositionInput;

    AndroidJavaObject nativeAdManager;
    private bool isAdLoaded = false;
    private bool isAdShowing = false;

    // Position constants
    private readonly Dictionary<int, string> positionNames = new Dictionary<int, string>
    {
        {0, "Top Center"},
        {1, "Top Left"},
        {2, "Top Right"},
        {3, "Center"},
        {4, "Bottom Center"},
        {5, "Bottom Left"},
        {6, "Bottom Right"},
        {7, "Custom Position"}
    };

    // Events for callback
    public static event Action<string> OnAdLoaded;
    public static event Action<string> OnAdShown;
    public static event Action<string> OnAdImpression;
    public static event Action<string> OnAdOpened;
    public static event Action<string> OnAdClosed;
    public static event Action<string> OnAdClicked;
    public static event Action<string> OnAdFailed;

    void Start()
    {
        InitializeNativeAd();
        SetupUI();
        SetupEventSubscriptions();
    }

    void InitializeNativeAd()
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        try
        {
            using (AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
            {
                AndroidJavaObject activity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
                AndroidJavaClass managerClass = new AndroidJavaClass("com.ads.android.nativemedia.unity.UnityNativeAd");
                nativeAdManager = managerClass.CallStatic<AndroidJavaObject>("getInstance", activity);
                nativeAdManager.Call("setListener", new NativeAdListenerProxy());
                UpdateStatus("Native Ad initialized ✅", Color.green);
            }
        }
        catch (Exception e)
        {
            Debug.LogError("Failed to initialize Native Ad: " + e.Message);
            UpdateStatus("Failed to initialize ❌", Color.red);
        }
#else
        UpdateStatus("Android platform only ⚠️", Color.yellow);
#endif
    }

    void SetupUI()
    {
        if (showAdButton == null || hideAdButton == null)
        {
            CreateDynamicUI();
        }
        else
        {
            // Setup existing UI
            showAdButton.onClick.AddListener(LoadAndShowAd);
            hideAdButton.onClick.AddListener(HideAd);
            if (updatePositionButton != null)
                updatePositionButton.onClick.AddListener(UpdateAdPosition);

            SetupPositionDropdown();
        }

        // Initial UI state
        UpdateUIState(false, false);
    }

    void SetupPositionDropdown()
    {
        if (positionDropdown != null)
        {
            positionDropdown.options.Clear();
            foreach (var position in positionNames)
            {
                positionDropdown.options.Add(new Dropdown.OptionData(position.Value));
            }
            positionDropdown.value = 0; // Default to Top Center

            // Show/hide custom position inputs based on dropdown selection
            positionDropdown.onValueChanged.AddListener(OnPositionChanged);
            OnPositionChanged(0); // Initialize state
        }
    }

    void OnPositionChanged(int value)
    {
        bool isCustomPosition = (value == 7); // Custom Position
        if (xPositionInput != null) xPositionInput.gameObject.SetActive(isCustomPosition);
        if (yPositionInput != null) yPositionInput.gameObject.SetActive(isCustomPosition);
    }

    void SetupEventSubscriptions()
    {
        OnAdLoaded += (msg) => {
            isAdLoaded = true;
            UpdateStatus("Ad Loaded ✅", Color.green);
            UpdateUIState(true, false);
        };

        OnAdShown += (msg) => {
            isAdShowing = true;
            UpdateStatus("Ad Showing 📺", Color.blue);
            UpdateUIState(true, true);
        };

        OnAdClosed += (msg) => {
            isAdShowing = false;
            isAdLoaded = false;
            UpdateStatus("Ad Closed ❌", Color.gray);
            UpdateUIState(false, false);
        };

        OnAdFailed += (msg) => {
            isAdLoaded = false;
            isAdShowing = false;
            UpdateStatus($"Ad Failed ⚠️", Color.red);
            UpdateUIState(false, false);
        };

        OnAdClicked += (msg) => UpdateStatus("Ad Clicked 🖱️", Color.cyan);
        OnAdOpened += (msg) => UpdateStatus("Ad Opened 🚀", Color.magenta);
        OnAdImpression += (msg) => UpdateStatus("Ad Impression 👁️", Color.yellow);
    }

    void CreateDynamicUI()
    {
        // Create Canvas
        GameObject canvas = new GameObject("NativeAdCanvas", typeof(Canvas), typeof(CanvasScaler), typeof(GraphicRaycaster));
        Canvas canvasComp = canvas.GetComponent<Canvas>();
        canvasComp.renderMode = RenderMode.ScreenSpaceOverlay;
        canvasComp.sortingOrder = 1000;

        CanvasScaler scaler = canvas.GetComponent<CanvasScaler>();
        scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
        scaler.referenceResolution = new Vector2(1920, 1080);

        // Create main panel
        GameObject panel = CreatePanel(canvas.transform, "NativeAdPanel", new Vector2(400, 500), new Vector2(0, 0));

        // Create title
        CreateText(panel.transform, "Native Ad Controller", new Vector2(0, 200), 24, FontStyle.Bold);

        // Create status text
        GameObject statusObj = CreateText(panel.transform, "Status: Ready", new Vector2(0, 150), 16);
        statusText = statusObj.GetComponent<Text>();

        // Create position dropdown
        GameObject dropdownObj = CreateDropdown(panel.transform, "Position:", new Vector2(0, 100));
        positionDropdown = dropdownObj.GetComponent<Dropdown>();

        // Create custom position inputs
        xPositionInput = CreateInputField(panel.transform, "X Position", new Vector2(-75, 50)).GetComponent<InputField>();
        yPositionInput = CreateInputField(panel.transform, "Y Position", new Vector2(75, 50)).GetComponent<InputField>();

        // Create buttons
        showAdButton = CreateButton(panel.transform, "Load & Show Ad", new Vector2(-75, 0), Color.green, LoadAndShowAd).GetComponent<Button>();
        hideAdButton = CreateButton(panel.transform, "Hide Ad", new Vector2(75, 0), Color.red, HideAd).GetComponent<Button>();
        updatePositionButton = CreateButton(panel.transform, "Update Position", new Vector2(0, -50), Color.blue, UpdateAdPosition).GetComponent<Button>();

        SetupPositionDropdown();
    }

    GameObject CreatePanel(Transform parent, string name, Vector2 size, Vector2 position)
    {
        GameObject panel = new GameObject(name, typeof(RectTransform), typeof(Image));
        panel.transform.SetParent(parent);

        RectTransform rect = panel.GetComponent<RectTransform>();
        rect.sizeDelta = size;
        rect.anchoredPosition = position;

        Image img = panel.GetComponent<Image>();
        img.color = new Color(0, 0, 0, 0.7f);

        return panel;
    }

    GameObject CreateText(Transform parent, string text, Vector2 pos, int fontSize = 16, FontStyle fontStyle = FontStyle.Normal)
    {
        GameObject txtObj = new GameObject("Text", typeof(Text));
        txtObj.transform.SetParent(parent);

        Text txt = txtObj.GetComponent<Text>();
        txt.text = text;
        txt.alignment = TextAnchor.MiddleCenter;
        txt.font = Resources.GetBuiltinResource<Font>("Arial.ttf");
        txt.color = Color.white;
        txt.fontSize = fontSize;
        txt.fontStyle = fontStyle;

        RectTransform rect = txt.rectTransform;
        rect.sizeDelta = new Vector2(350, 30);
        rect.anchoredPosition = pos;

        return txtObj;
    }

    GameObject CreateButton(Transform parent, string text, Vector2 pos, Color color, UnityEngine.Events.UnityAction action)
    {
        GameObject btnObj = new GameObject(text, typeof(RectTransform), typeof(Button), typeof(Image));
        btnObj.transform.SetParent(parent);

        RectTransform rect = btnObj.GetComponent<RectTransform>();
        rect.sizeDelta = new Vector2(140, 40);
        rect.anchoredPosition = pos;

        Button btn = btnObj.GetComponent<Button>();
        btn.onClick.AddListener(action);
        btn.GetComponent<Image>().color = color;

        GameObject txtObj = CreateText(btnObj.transform, text, Vector2.zero, 14);
        txtObj.GetComponent<Text>().color = Color.white;

        return btnObj;
    }

    GameObject CreateDropdown(Transform parent, string labelText, Vector2 pos)
    {
        // Create label
        CreateText(parent, labelText, new Vector2(pos.x - 120, pos.y), 14);

        // Create dropdown
        GameObject dropdownObj = new GameObject("PositionDropdown", typeof(RectTransform), typeof(Dropdown), typeof(Image));
        dropdownObj.transform.SetParent(parent);

        RectTransform rect = dropdownObj.GetComponent<RectTransform>();
        rect.sizeDelta = new Vector2(200, 30);
        rect.anchoredPosition = new Vector2(pos.x + 50, pos.y);

        Dropdown dropdown = dropdownObj.GetComponent<Dropdown>();
        dropdown.GetComponent<Image>().color = new Color(0.2f, 0.2f, 0.2f, 0.8f);

        // Create dropdown template (simplified)
        GameObject template = new GameObject("Template", typeof(RectTransform));
        template.transform.SetParent(dropdownObj.transform);
        template.SetActive(false);

        return dropdownObj;
    }

    GameObject CreateInputField(Transform parent, string placeholder, Vector2 pos)
    {
        GameObject inputObj = new GameObject("InputField", typeof(RectTransform), typeof(InputField), typeof(Image));
        inputObj.transform.SetParent(parent);

        RectTransform rect = inputObj.GetComponent<RectTransform>();
        rect.sizeDelta = new Vector2(120, 30);
        rect.anchoredPosition = pos;

        InputField input = inputObj.GetComponent<InputField>();
        input.GetComponent<Image>().color = new Color(0.2f, 0.2f, 0.2f, 0.8f);

        // Create placeholder text
        GameObject placeholderObj = CreateText(inputObj.transform, placeholder, Vector2.zero, 12);
        Text placeholderText = placeholderObj.GetComponent<Text>();
        placeholderText.color = new Color(1, 1, 1, 0.5f);
        input.placeholder = placeholderText;

        // Create text component for input
        GameObject textObj = CreateText(inputObj.transform, "", Vector2.zero, 12);
        input.textComponent = textObj.GetComponent<Text>();

        return inputObj;
    }

    void LoadAndShowAd()
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (nativeAdManager != null)
        {
            Debug.Log("[Unity] Loading Native Ad...");
            UpdateStatus("Loading Ad... ⏳", Color.yellow);

            int position = GetSelectedPosition();
            int x = GetCustomX();
            int y = GetCustomY();

            nativeAdManager.Call("loadAd", adUnitId, position, x, y);
        }
        else
        {
            Debug.LogError("Native Ad Manager not initialized!");
            UpdateStatus("Error: Manager not initialized ❌", Color.red);
        }
#else
        UpdateStatus("Android platform only ⚠️", Color.yellow);
        // Simulate success for testing in editor
        StartCoroutine(SimulateAdLoad());
#endif
    }

    void UpdateAdPosition()
    {
        if (!isAdShowing)
        {
            UpdateStatus("No ad showing ⚠️", Color.yellow);
            return;
        }

#if UNITY_ANDROID && !UNITY_EDITOR
        if (nativeAdManager != null)
        {
            int position = GetSelectedPosition();
            int x = GetCustomX();
            int y = GetCustomY();

            Debug.Log($"[Unity] Updating ad position to: {position} ({x}, {y})");
            nativeAdManager.Call("setAdPosition", position, x, y);
            UpdateStatus($"Position updated to {GetPositionName(position)} 📍", Color.cyan);
        }
#else
        UpdateStatus($"Position would update to {GetPositionName(GetSelectedPosition())} 📍", Color.cyan);
#endif
    }

    void HideAd()
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        Debug.Log("[Unity] Hiding Native Ad...");
        AndroidJavaClass managerClass = new AndroidJavaClass("com.ads.android.nativemedia.unity.UnityNativeAd");
        managerClass.CallStatic("hideAdFromUnity");
        UpdateStatus("Hiding Ad... 🔄", Color.gray);
#else
        UpdateStatus("Ad hidden (simulation) 🔄", Color.gray);
        isAdShowing = false;
        isAdLoaded = false;
        UpdateUIState(false, false);
#endif
    }

    int GetSelectedPosition()
    {
        return positionDropdown != null ? positionDropdown.value : 0;
    }

    int GetCustomX()
    {
        if (xPositionInput != null && !string.IsNullOrEmpty(xPositionInput.text))
        {
            if (int.TryParse(xPositionInput.text, out int x))
                return Mathf.Clamp(x, 0, Screen.width);
        }
        return 0;
    }

    int GetCustomY()
    {
        if (yPositionInput != null && !string.IsNullOrEmpty(yPositionInput.text))
        {
            if (int.TryParse(yPositionInput.text, out int y))
                return Mathf.Clamp(y, 0, Screen.height);
        }
        return 0;
    }

    string GetPositionName(int position)
    {
        return positionNames.ContainsKey(position) ? positionNames[position] : "Unknown";
    }

    void UpdateUIState(bool adLoaded, bool adShowing)
    {
        if (showAdButton != null)
            showAdButton.interactable = !adShowing;

        if (hideAdButton != null)
            hideAdButton.interactable = adShowing;

        if (updatePositionButton != null)
            updatePositionButton.interactable = adShowing;
    }

    void UpdateStatus(string status, Color color)
    {
        if (statusText != null)
        {
            statusText.text = "Status: " + status;
            statusText.color = color;
        }
        Debug.Log("[NativeAd] " + status);
    }

    // For editor testing
    System.Collections.IEnumerator SimulateAdLoad()
    {
        yield return new WaitForSeconds(1f);
        OnAdLoaded?.Invoke("Simulated ad loaded");
        yield return new WaitForSeconds(0.5f);
        OnAdShown?.Invoke("Simulated ad shown");
        yield return new WaitForSeconds(0.2f);
        OnAdImpression?.Invoke("Simulated impression");
    }

    void OnDestroy()
    {
        // Cleanup events
        OnAdLoaded = null;
        OnAdShown = null;
        OnAdImpression = null;
        OnAdOpened = null;
        OnAdClosed = null;
        OnAdClicked = null;
        OnAdFailed = null;
    }
}



// Enhanced UnityMainThreadDispatcher with better performance
public class UnityMainThreadDispatcher : MonoBehaviour
{
    private static UnityMainThreadDispatcher _instance;
    private readonly Queue<System.Action> _executionQueue = new Queue<System.Action>();
    private readonly object _lock = new object();

    public static UnityMainThreadDispatcher Instance()
    {
        if (_instance == null)
        {
            GameObject go = new GameObject("UnityMainThreadDispatcher");
            _instance = go.AddComponent<UnityMainThreadDispatcher>();
            DontDestroyOnLoad(go);
        }
        return _instance;
    }

    public void Enqueue(System.Action action)
    {
        if (action == null) return;

        lock (_lock)
        {
            _executionQueue.Enqueue(action);
        }
    }

    void Update()
    {
        // Process max 10 actions per frame to avoid frame drops
        int processCount = 0;

        while (processCount < 10)
        {
            System.Action action = null;

            lock (_lock)
            {
                if (_executionQueue.Count > 0)
                    action = _executionQueue.Dequeue();
            }

            if (action == null) break;

            try
            {
                action.Invoke();
            }
            catch (Exception e)
            {
                Debug.LogError("Error executing queued action: " + e.Message);
            }

            processCount++;
        }
    }

    void OnDestroy()
    {
        lock (_lock)
        {
            _executionQueue.Clear();
        }
    }
}