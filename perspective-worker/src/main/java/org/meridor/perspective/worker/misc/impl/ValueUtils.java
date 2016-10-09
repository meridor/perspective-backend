package org.meridor.perspective.worker.misc.impl;

public final class ValueUtils {
    
    public static String formatQuota(Integer currentValue, Integer maxValue) {
        if (currentValue == null && maxValue == null) {
            return null;
        }
        return String.format("%s/%s", formatValue(currentValue), formatValue(maxValue));
    }

    private static String formatValue(Integer value){
        if (value == null) {
            return "?";
        }
        if (value == -1) {
            return "inf";
        }
        return String.valueOf(value);
    }
    
}
