package com.cuentas.cuentashogar;

import java.util.regex.Pattern;

public final class Utils {

    private Utils() {
        // util class
    }

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL.matcher(email).matches();
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static String reverse(String s) {
        if (s == null) throw new IllegalArgumentException("s must not be null");
        return new StringBuilder(s).reverse().toString();
    }

}
