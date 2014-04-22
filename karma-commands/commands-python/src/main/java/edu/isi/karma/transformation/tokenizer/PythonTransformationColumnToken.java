package edu.isi.karma.transformation.tokenizer;

public class PythonTransformationColumnToken extends
		PythonTransformationToken {

	public PythonTransformationColumnToken(String token)
	{
		super(token);
	}

	@Override
	public void accept(PythonTransformationTokenVistor visitor) {
		visitor.visit(this);
	}
}
