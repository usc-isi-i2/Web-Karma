package edu.isi.karma.transformation.tokenizer;

public class PythonTransformationStringToken extends PythonTransformationToken {

	public PythonTransformationStringToken(String token) {
		super(token);
	}

	@Override
	public void accept(PythonTransformationTokenVistor visitor) {
		visitor.visit(this);
	}
}
