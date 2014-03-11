package edu.isi.karma.transformation.tokenizer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PythonTransformationAsURIValidatorTest {

	@Test
	public void emptyStringTest() {
		String transformationCode = "";
		PythonTransformationAsURIValidator validator = new PythonTransformationAsURIValidator();
		assertFalse(validator.validate(transformationCode));
	}
	@Test
	public void nullStringTest() {
		
		String transformationCode = null;
		PythonTransformationAsURIValidator validator = new PythonTransformationAsURIValidator();
		assertFalse(validator.validate(transformationCode));
	}
	@Test
	public void basicColumnTest() {
		
		String transformationCode = "return getValue(\"value\")";
		PythonTransformationAsURIValidator validator = new PythonTransformationAsURIValidator();
		assertTrue(validator.validate(transformationCode));
	}

	@Test
	public void basicColumnWithSpacesTest() {
		
		String transformationCode = "return getValue(  	\"value\"   )";
		PythonTransformationAsURIValidator validator = new PythonTransformationAsURIValidator();
		assertTrue(validator.validate(transformationCode));
	}
	
	@Test
	public void basicStringTest() {
		
		String transformationCode = "return \"http://localhost/\" + getValue(\"value\")";
		PythonTransformationAsURIValidator validator = new PythonTransformationAsURIValidator();
		assertTrue(validator.validate(transformationCode));
	}

	@Test
	public void basicVariableTest() {
		
		String transformationCode = "something = \"garbage\"\nreturn something + getValue(\"value\")";
		PythonTransformationAsURIValidator validator = new PythonTransformationAsURIValidator();
		assertFalse(validator.validate(transformationCode));
	}
}
