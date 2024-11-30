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

    public static float fInterpToExp(float currentValue, float targetValue, float interpSpeed, float deltaTime) {
        // Exponential decay formula
        float t = 1.0f - (float)Math.pow(0.5, interpSpeed * deltaTime);

        // Interpolate towards the target
        return currentValue + (targetValue - currentValue) * t;
    }

}
