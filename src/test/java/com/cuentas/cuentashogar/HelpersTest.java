package com.cuentas.cuentashogar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HelpersTest {

    @Test
    void isPalindrome_shouldReturnTrueForPalindromes() {
        assertTrue(Helpers.isPalindrome("A man a plan a canal Panama"));
        assertTrue(Helpers.isPalindrome("racecar"));
    }

    @Test
    void isPalindrome_shouldReturnFalseForNonPalindromesAndNull() {
        assertFalse(Helpers.isPalindrome("hello"));
        assertFalse(Helpers.isPalindrome(null));
    }

    @Test
    void factorial_shouldReturnCorrectValues() {
        assertEquals(1L, Helpers.factorial(0));
        assertEquals(1L, Helpers.factorial(1));
        assertEquals(2L, Helpers.factorial(2));
        assertEquals(6L, Helpers.factorial(3));
        assertEquals(24L, Helpers.factorial(4));
    }

    @Test
    void factorial_largeValue() {
        // 20! fits in a signed 64-bit
        assertEquals(2432902008176640000L, Helpers.factorial(20));
    }

    @Test
    void factorial_shouldThrowOnNegative() {
        assertThrows(IllegalArgumentException.class, () -> Helpers.factorial(-1));
    }

    @Test
    void safeParseInt_shouldParseOrReturnDefault() {
        assertEquals(123, Helpers.safeParseInt("123", 0));
        assertEquals(0, Helpers.safeParseInt("notanumber", 0));
        assertEquals(5, Helpers.safeParseInt(null, 5));
    }

    @Test
    void repeat_shouldRepeatAndHandleEdgeCases() {
        assertEquals("aaa", Helpers.repeat("a", 3));
        assertEquals("", Helpers.repeat("x", 0));
        assertThrows(IllegalArgumentException.class, () -> Helpers.repeat(null, 1));
        assertThrows(IllegalArgumentException.class, () -> Helpers.repeat("a", -1));
    }

    @Test
    void isPalindrome_emptyStringIsPalindrome() {
        assertTrue(Helpers.isPalindrome(""));
    }

}
