package com.cuentas.cuentashogar;

import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Helpers {

    public static boolean isPalindrome(String s) {
        if (s == null) return false;
        String normalized = s.replaceAll("\\s+", "").toLowerCase();
        return new StringBuilder(normalized).reverse().toString().equals(normalized);
    }

    public static long factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        long res = 1L;
        for (int i = 2; i <= n; i++) {
            res *= i;
        }
        return res;
    }

    public static int safeParseInt(String s, int defaultValue) {
        if (s == null) return defaultValue;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String repeat(String s, int times) {
        if (s == null) throw new IllegalArgumentException("s must not be null");
        if (times < 0) throw new IllegalArgumentException("times must be >= 0");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

}
