package com.dawnestofbread.vehiclemod.utils;

import java.util.List;

public class Curve {
    public List<Double> curvePoints;
    public Curve(List<Double> newCurvePoints) {
        this.curvePoints = newCurvePoints;
    }

    public double lookup(double key) {
        int lookupKey = (int) Math.floor(key / 1000);
        if (lookupKey < (curvePoints.size() - 1)) {
            if (curvePoints.get(lookupKey) != null) {
                if (lookupKey + 1 < curvePoints.size()) {
                    //(y) = y1 + [(x-x1) × (y2-y1)]/ (x2-x1)
                    return curvePoints.get(lookupKey) + ( (key / 1000 - lookupKey) * (curvePoints.get(lookupKey + 1) - curvePoints.get(lookupKey)) ) / (lookupKey + 1 - lookupKey);
                } else {
                    return curvePoints.get(lookupKey);
                }
            }
        } else {
            return curvePoints.get(curvePoints.size() - 1);
        }
        return curvePoints.get(0);
    }
}
