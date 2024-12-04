package com.dawnestofbread.vehiclemod.utils;

public class Maths {
    public static float fInterpTo(float currentValue, float targetValue, float interpSpeed, float deltaTime) {
        // Calculate the difference between the current and target values
        float difference = targetValue - currentValue;

        // Calculate the maximum step allowed for this frame
        float maxStep = interpSpeed * deltaTime;

        // Move towards the target value by the step size or the remaining difference
        if (Math.abs(difference) <= maxStep) {
            return targetValue; // We've reached or exceeded the target value
        }

        // Move closer to the target value
        return currentValue + Math.signum(difference) * maxStep;
    }
    public static double dInterpTo(double currentValue, double targetValue, double interpSpeed, double deltaTime) {
        // Calculate the difference between the current and target values
        double difference = targetValue - currentValue;

        // Calculate the maximum step allowed for this frame
        double maxStep = interpSpeed * deltaTime;

        // Move towards the target value by the step size or the remaining difference
        if (Math.abs(difference) <= maxStep) {
            return targetValue; // We've reached or exceeded the target value
        }

        // Move closer to the target value
        return currentValue + Math.signum(difference) * maxStep;
    }

    public static float fInterpToExp(float currentValue, float targetValue, float interpSpeed, float deltaTime) {
        // Exponential decay formula
        float t = 1.0f - (float)Math.pow(0.5, interpSpeed * deltaTime);

        // Interpolate towards the target
        return currentValue + (targetValue - currentValue) * t;
    }

    public static double mapDoubleRangeClamped(double value, double inMin, double inMax, double outMin, double outMax) {
        // Map the value to the target range
        double mappedValue = (value - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;

        // Clamp the value within the output range
        return Math.max(outMin, Math.min(outMax, mappedValue));
    }
}
