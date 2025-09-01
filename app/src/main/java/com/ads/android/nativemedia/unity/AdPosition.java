package com.ads.android.nativemedia.unity;

public class AdPosition {
    public static final int TOP_CENTER = 0;
    public static final int TOP_LEFT = 1;
    public static final int TOP_RIGHT = 2;
    public static final int CENTER = 3;
    public static final int BOTTOM_CENTER = 4;
    public static final int BOTTOM_LEFT = 5;
    public static final int BOTTOM_RIGHT = 6;
    public static final int CUSTOM = 7;

    private int position;
    private int x;
    private int y;

    public AdPosition(int position, int x, int y) {
        this.position = position;
        this.x = x;
        this.y = y;
    }

    // Getters and setters
    public int getPosition() { return position; }
    public int getX() { return x; }
    public int getY() { return y; }
}