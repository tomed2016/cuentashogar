package com.cuentas.cuentashogar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void isValidEmail_shouldReturnTrueForValid() {
        assertTrue(Utils.isValidEmail("user@example.com"));
    }

    @Test
    void isValidEmail_shouldReturnFalseForInvalid() {
        assertFalse(Utils.isValidEmail("not-an-email"));
        assertFalse(Utils.isValidEmail(null));
    }

    @Test
    void add_shouldSumNumbers() {
        assertEquals(5, Utils.add(2, 3));
        assertEquals(-1, Utils.add(2, -3));
    }

    @Test
    void reverse_shouldReverseString() {
        assertEquals("cba", Utils.reverse("abc"));
    }

    @Test
    void reverse_shouldHandleEmpty() {
        assertEquals("", Utils.reverse(""));
    }

    @Test
    void isValidEmail_moreCases() {
        assertTrue(Utils.isValidEmail("user.name+tag+sorting@example.com"));
        assertFalse(Utils.isValidEmail("user@"));
        assertFalse(Utils.isValidEmail("@example.com"));
    }

    @Test
    void reverse_shouldThrowOnNull() {
        assertThrows(IllegalArgumentException.class, () -> Utils.reverse(null));
    }

}
