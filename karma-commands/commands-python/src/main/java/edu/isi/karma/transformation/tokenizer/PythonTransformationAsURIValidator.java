package edu.isi.karma.transformation.tokenizer;

import java.util.List;

public class PythonTransformationAsURIValidator extends PythonTransformationTokenVistor {

	private Boolean isValid = null;
	@Override
	protected
	void visit(PythonTransformationToken pythonTransformationToken) {
		isValid = isValid == null? true : isValid && true;
		
	}

	@Override
	protected void visit(PythonTransformationColumnToken pythonTransformationToken) {
		isValid = isValid == null? true : isValid && true;
		
	}
	
	@Override
	protected void visit(PythonTransformationStringToken pythonTransformationToken) {
		isValid = isValid == null? true : isValid && true;
		
	}
	
	@Override
	protected void visit(PythonTransformationInvalidToken pythonTransformationToken) {
		isValid = false;		
	}

	protected boolean isValid() {
		return isValid == null? false : isValid;
	}

	public boolean validate(String transformationCode)
	{
		List<PythonTransformationToken> tokens = PythonTransformationAsURITokenizer.tokenize(transformationCode);
		return validate(tokens);
	}

	public boolean validate(List<PythonTransformationToken> tokens) {
		this.visit(tokens);
		return isValid();
	}
}
