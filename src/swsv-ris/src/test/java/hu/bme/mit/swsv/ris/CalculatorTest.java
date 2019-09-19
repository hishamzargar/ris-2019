package hu.bme.mit.swsv.ris;

import static org.junit.Assert.*;

import org.junit.Test;

public class CalculatorTest {
	
	@Test
	public void testAdd() {
		// Arrange
		Calculator sut = new Calculator();
		
		// Act
		int result = sut.add(1, 2);
		
		// Assert
		assertEquals(3, result);
	}

}
