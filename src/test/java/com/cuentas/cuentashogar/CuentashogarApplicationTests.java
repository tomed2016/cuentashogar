package com.cuentas.cuentashogar;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CuentashogarApplicationTests {

	@Test
	void utilsIntegration() {
		assertEquals(3, Utils.add(1, 2));
		assertTrue(Utils.isValidEmail("a@b.com"));
	}

}
