package com.ded.macanclient.features;

public class MathUtils {
    public static float interpolate(float prev, float current, float partialTicks) {
        return prev + (current - prev) * partialTicks;
    }

    public static double interpolate(double prev, double current, double partialTicks) {
        return prev + (current - prev) * partialTicks;
    }

    public static float lerp(float value, float to, float pc) {
        return value + pc * (to - value);
    }
}