package com.customwrld.claims.utils;

import java.util.Set;

public class CollectionUtils {
    public static int setGetIndex(Set<? extends Object> set, Object value) {
        int result = 0;
        for (Object entry:set) {
            if (entry.equals(value)) return result;
            result++;
        }
        return -1;
    }

    public static <T> T setFromIndex(Set<? extends T> set, int index) {
        if(index > set.size())
            return null;

        int i = 0;
        for (T entry : set) {
            if (i == index) return entry;
            i++;
        }

        return null;
    }
}
